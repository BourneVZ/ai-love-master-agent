# ai-love-master-agent 技术实现方案

## 1. 文档目的

本文档基于以下材料整理：

- [spec.md](/Z:/Users/BVZ/WorkSpace/101Doodle/VibeCoding/ai-love-master-agent/specs/spec.md)
- [constitution.md](/Z:/Users/BVZ/WorkSpace/101Doodle/VibeCoding/ai-love-master-agent/constitution.md)
- [AGENT.md](/Z:/Users/BVZ/WorkSpace/101Doodle/VibeCoding/ai-love-master-agent/AGENT.md)

本文档的目标是为 `ai-love-master-agent` 提供一份可执行、可分阶段落地、可验证的技术实现方案，用于指导后续后端重构、Agent 能力演进、工具系统治理，以及未来前端与前后端接口的继续开发。

本文档重点解决三类问题：

- 明确项目当前的技术边界、重构目标和约束条件。
- 将 LoveManus 从针对单一测试场景补丁式演进的实现，重构为可处理泛化情感任务的通用 Agent 内核。
- 保证重构不会破坏原 `spec.md` 中已经定义或未来计划扩展的前端页面、后端接口和 SSE 交互能力。

---

## 2. 设计原则

本方案必须遵守以下原则：

- 简单优先：优先复用现有 Spring Boot、Spring AI、Vue 3、Vite、SSE、ToolRegistration 等已有模式，不为假设中的未来需求过度设计。
- 测试优先：每个阶段的改动必须有自动化测试或明确的手工验收方式。
- 边界清晰：Controller 负责协议，App/Agent Facade 负责用例组织，Runtime 负责执行循环，Policy 负责约束，Skill 负责领域增强，Tool 负责单一能力。
- 可插拔演进：RAG、Tool Calling、MCP、结构化输出、Artifact 生成等能力必须可独立演进，不得耦合在某个测试场景中。
- 安全优先：终端、文件、下载、网页抓取、MCP 等能力视为高风险能力，必须有清晰的白名单、超时、目录限制和失败处理。
- 接口兼容优先：本轮重构主要调整后端内部架构，不得影响后续原有接口和前端页面的继续开发。

---

## 3. 项目定位与重构目标

### 3.1 项目定位

`ai-love-master-agent` 是一个以情感场景为核心的 AI 全栈应用项目，目标不是传统 CRUD，而是围绕以下能力建设：

- AI 情感对话应用
- 具备自主推理与行动能力的 Agent
- Tool Calling
- RAG
- MCP 扩展能力
- SSE 流式交互体验

### 3.2 当前问题

结合现有代码和测试现状，LoveManus 方向存在以下问题：

- `ToolCallAgent` 同时承担执行循环、工具调度、错误兜底、业务修补、测试场景兼容等多种职责。
- 部分实现已经演化出明显的强制流程，例如 `searchWeb -> searchImage -> downloadResource -> generatePDF`。
- 当前完成判定偏向“是否调用过某个工具”，而不是“是否满足用户真实目标”。
- 一些逻辑已明显受到 `LoveManusTest` 影响，存在过拟合单一场景的风险。
- 工具返回大量使用自由文本，导致后端难以稳定做结果校验、前端难以扩展结构化展示。

### 3.3 重构目标

本次方案的总体目标是：

- 将 LoveManus 重构为“任务分类 + 轻计划 + 自由执行 + 结果校验 + Skill 增强”的通用 Agent 内核。
- 将“约会计划 PDF 生成”从内核硬编码能力，降级为一个 Skill 场景实例。
- 保持与现有前后端接口兼容，为后续前端页面开发提供稳定支撑。

---

## 4. 技术栈与硬约束

### 4.1 后端技术栈

- Java 21
- Spring Boot 3
- Spring AI
- Maven
- SSE
- iText PDF
- Hutool

### 4.2 前端技术栈

- Vue 3
- Vite
- Axios
- EventSource / SSE

### 4.3 外部依赖

- DashScope 或兼容的大模型服务
- SearchAPI 或兼容搜索服务
- MCP 服务
- 本地或远程向量检索能力

### 4.4 本轮重构硬约束

- 不修改已有核心接口对外语义：
    - `GET /api/ai/love_app/chat/sse`
    - `GET /api/ai/manus/chat`
    - `GET /api/health`
- 不破坏现有前端两类核心页面的交互模式：
    - `AI 恋爱大师`
    - `AI 超级智能体`
- 不允许将单个测试场景写成核心 Runtime 的固定工具顺序。
- 不允许把后续前端接口需求绑定到某个具体 Tool 名称或某段 Prompt 文本上。
- 允许内部重构类结构、运行时对象和策略层，但必须保证 Controller 层的对外协议兼容。

### 4.5 本轮非目标

- 不在本轮直接实现新前端页面。
- 不在本轮推翻整个工具系统。
- 不在本轮替换掉 Spring AI。
- 不在本轮改变产品层面的“HTTP + SSE + 前后端分离”形态。

---

## 5. 总体架构方案

### 5.1 分层架构

建议后端演进为以下分层：

1. Controller 接口层
2. App / Agent Facade 应用入口层
3. Agent Runtime 执行层
4. Policy / Contract 约束层
5. Skill 领域增强层
6. Tool Registry / Tool Adapter 工具抽象层
7. 外部依赖接入层

### 5.2 各层职责

#### 5.2.1 Controller 接口层

职责：

- 处理 HTTP / SSE 请求
- 解析参数
- 调用应用入口
- 保持前端协议稳定

限制：

- 不得直接承载复杂 Agent 编排逻辑
- 不得直接拼接具体工具调用流程

#### 5.2.2 App / Agent Facade 应用入口层

职责：

- 作为 Controller 对应的稳定后端用例入口
- 处理 `chatId`、会话上下文、SSE 输出适配
- 调用 LoveApp 或 LoveManus 对应编排器

限制：

- 不直接写死业务流程
- 不负责工具顺序控制

#### 5.2.3 Agent Runtime 执行层

职责：

- 任务分类
- 轻量计划生成
- 多轮执行循环
- 工具调用决策
- 状态管理
- 完成判定
- 修复或终止

这是本次重构的核心层。

#### 5.2.4 Policy / Contract 约束层

职责：

- 定义什么才算完成
- 定义哪些行为不允许
- 对任务产物、搜索证据、图片覆盖、本地文件、真实性进行约束

#### 5.2.5 Skill 领域增强层

职责：

- 根据情感场景给出任务引导
- 动态补充 Prompt 片段
- 增强默认 SuccessContract

限制：

- Skill 只能增强，不直接接管主执行循环
- Skill 不得内嵌固定工具顺序

#### 5.2.6 Tool 抽象层

职责：

- 封装单一能力
- 提供统一注册与元数据描述
- 提供结构化结果解释能力

---

## 6. 目标代码结构

建议逐步演进为如下目录结构：

```text
src/main/java/com/bvz/aiagent/
  core/
    runtime/
    policy/
    contract/
    skill/
    tool/
    model/
  domain/
    love/
      skills/
      policy/
      prompt/
  app/
  agent/
  tools/
  controller/
  eval/
```

说明：

- `agent/` 保留现有 LoveManus、ToolCallAgent 等入口类，并逐步瘦身为 Facade / Adapter。
- `tools/` 保留现有工具实现，并逐步增强为可描述、可解释、可结构化返回的形式。
- `core/` 放置通用 Agent 内核。
- `domain/love/` 放置情感领域的 Skill、Policy 和 Prompt 片段。

---

## 7. 核心运行时对象设计

### 7.1 AgentTask

用于表达一次用户任务。

建议字段：

- `taskId`
- `originalUserRequest`
- `normalizedGoal`
- `taskType`
- `riskLevel`
- `artifactRequirement`
- `externalInfoRequirement`
- `conversationContext`
- `userPreferences`

### 7.2 TaskType

建议枚举：

- `CONVERSATION_ONLY`
- `ADVICE`
- `RESEARCH`
- `ARTIFACT`
- `ACTION`
- `HYBRID`

### 7.3 ExecutionBudget

建议字段：

- `maxSteps`
- `maxToolCalls`
- `maxRepairAttempts`
- `maxPlanningRounds`

### 7.4 ExecutionState

建议字段：

- `stepIndex`
- `toolCallCount`
- `planningRound`
- `toolHistory`
- `observations`
- `partialArtifacts`
- `violations`
- `status`

### 7.5 SuccessContract

建议字段：

- `requiresExternalEvidence`
- `requiresArtifact`
- `artifactContract`
- `coverageContract`
- `truthfulnessRequired`
- `localAssetRequired`
- `forbiddenExtraArtifacts`

### 7.6 ArtifactContract

建议字段：

- `artifactType`
- `mustExist`
- `mustBeReadable`
- `mustUseLocalAssets`
- `requiredSections`
- `allowedFormats`

### 7.7 CoverageContract

建议字段：

- `subjectType`
- `expectedCount`
- `minCoverageRatio`
- `perItemImageMinCount`

### 7.8 ValidationResult

建议字段：

- `passed`
- `issues`
- `repairable`
- `summary`

---

## 8. Agent Runtime 方案

### 8.1 主循环模式

Runtime 采用“轻计划 + 自由执行 + 结果校验”的混合模式。

推荐流程：

1. 接收用户任务
2. 任务分类
3. 装配基础 Policy
4. 命中 Skill 并增强 Contract
5. 生成轻量计划
6. 进入执行循环
7. 每轮由模型自由决定是否调用工具、调用哪个工具、是否继续推进
8. 将工具结果回写为 Observation
9. 用 CompletionValidator 根据 SuccessContract 判定是否完成
10. 完成则终止，失败则修复或安全结束

### 8.2 为什么取消强制流程

原因如下：

- 固定工具顺序更接近 Workflow，不适合开放式情感任务。
- 大量情感问题并不需要搜索、图片、下载、PDF。
- LoveManusTest 只是“搜索 + 图片 + 产物”的一个特例，不能反向塑造整个 Agent。
- 真正应约束的是结果，而不是路径。

### 8.3 推荐核心组件

建议新增以下核心类：

- `AgentOrchestrator`
- `TaskClassifier`
- `PlanGenerator`
- `ModelStepExecutor`
- `CompletionValidator`
- `RepairStrategy`
- `TaskPolicyRegistry`
- `SkillRegistry`
- `ToolResultInterpreterRegistry`

---

## 9. 核心类职责说明

### 9.1 AgentOrchestrator

职责：

- 驱动整个执行循环
- 管理状态迁移
- 组织 planner、executor、validator、repair

建议方法：

```java
AgentResult run(AgentTask task);
```

### 9.2 TaskClassifier

职责：

- 将用户请求分类到任务类型
- 推断是否需要联网、是否需要文件、是否需要图片、是否存在高风险动作

建议方法：

```java
TaskProfile classify(String userRequest);
```

### 9.3 PlanGenerator

职责：

- 让模型先输出轻量计划
- 暴露“接下来可能怎么做”，增强可解释性

建议方法：

```java
ExecutionPlan createPlan(AgentTask task, ExecutionState state);
```

### 9.4 ModelStepExecutor

职责：

- 基于当前状态让模型做下一步决策
- 解析工具调用
- 执行工具并写回 Observation

建议方法：

```java
StepResult executeNextStep(AgentTask task, ExecutionState state, ExecutionPlan plan);
```

### 9.5 CompletionValidator

职责：

- 根据 SuccessContract 判断任务是否真正完成

建议方法：

```java
ValidationResult validate(AgentTask task, ExecutionState state, SuccessContract contract);
```

### 9.6 RepairStrategy

职责：

- 在任务未完成但可修复时，生成修复指令
- 将缺口反馈给模型，而不是替模型做业务决策

建议方法：

```java
RepairInstruction buildRepairInstruction(ValidationResult result, ExecutionState state);
```

---

## 10. Policy / Contract 设计

### 10.1 设计目标

Policy 层负责“允许什么、禁止什么、怎样才算完成”，而不是“具体必须先做什么”。

### 10.2 推荐 Policy

#### 10.2.1 TruthfulnessPolicy

规则：

- 未成功调用的工具，不能在最终回答中声称已完成。
- 未成功生成的文件，不能声称已保存。
- 未成功下载的资源，不能声称已下载。

#### 10.2.2 ArtifactPolicy

规则：

- 用户要求 PDF、文件、图片产物时，必须有真实回执。
- 不得生成用户未要求的额外 txt、清洗文本文件、中间缓存文件，除非显式允许。

#### 10.2.3 ExternalEvidencePolicy

规则：

- 涉及时效性地点、网络信息、价格、营业状态、活动安排等，必须先具备外部检索证据。

#### 10.2.4 SafetyBoundaryPolicy

规则：

- 文件、终端、下载、网页抓取等高风险工具必须受目录、命令、URL、超时限制。
- 情感建议不得输出跟踪、操控、侵犯隐私、极端行为等高风险内容。

### 10.3 完成标准从“工具顺序”转为“目标契约”

例如，“生成带图片的约会计划 PDF”不应理解为固定工具链，而应理解为：

- 最终有一个真实存在、可打开的 PDF
- PDF 内容覆盖足够的地点
- 若要求结合网络图片，则必须达到最低图片覆盖率，或明确说明缺口
- 图片应优先使用本地资源，不应伪装为已嵌入

---

## 11. Skill 设计

### 11.1 Skill 目标

Skill 用于表达“某类情感任务通常怎么组织推理”，但不把它写死到 Runtime 中。

### 11.2 Skill 接口

建议定义：

```java
public interface Skill {
    String id();
    boolean matches(AgentTask task);
    SkillGuidance buildGuidance(AgentTask task);
    SuccessContract enrichContract(AgentTask task, SuccessContract baseContract);
}
```

### 11.3 第一批建议 Skill

#### 11.3.1 DatePlanSkill

适用场景：

- 约会地点推荐
- 约会计划制定
- 周年纪念活动安排

职责：

- 强化地点数量、图片覆盖、最终产物等契约
- 推荐搜索、图片、下载、PDF 等能力

#### 11.3.2 RelationshipAdviceSkill

适用场景：

- 情绪安抚
- 关系分析
- 沟通建议

职责：

- 强化共情、结构化建议、风险边界
- 默认以建议推理为主，不强依赖工具

#### 11.3.3 MessageDraftSkill

适用场景：

- 道歉消息
- 表白文案
- 沟通脚本

职责：

- 控制语气、长度、结构
- 默认不需要外部工具

---

## 12. 工具系统重构方案

### 12.1 总体策略

本轮不建议推翻现有所有工具，而是分两步走：

1. 保留现有工具实现
2. 在工具之上增加描述层与解释层

### 12.2 新增 Tool 元数据

建议引入：

- `ToolDescriptor`
- `ToolCapability`
- `ToolResultInterpreter`

### 12.3 ToolDescriptor 建议字段

- `name`
- `capabilityType`
- `sideEffectLevel`
- `inputSchema`
- `outputSchema`
- `supportsRetry`
- `artifactProducing`

### 12.4 返回值结构化演进方向

当前大量工具以字符串返回结果，不利于结果校验和未来前端展示。建议逐步演进为结构化返回。

优先级建议：

1. `searchImage`
2. `downloadResource`
3. `generatePDF`
4. `searchWeb`

### 12.5 推荐返回模型

#### searchWeb

```java
class WebSearchResult {
    List<SearchItem> items;
}
```

#### searchImage

```java
class ImageSearchResult {
    List<ImageItem> items;
}
```

#### downloadResource

```java
class DownloadResult {
    boolean success;
    String localPath;
    String mimeType;
    long size;
}
```

#### generatePDF

```java
class PdfGenerationResult {
    boolean success;
    String localPath;
    boolean readable;
    List<String> embeddedLocalAssets;
}
```

### 12.6 为什么不一次性全部重写

原因：

- 需要兼容 Spring AI 当前 ToolCallback 使用方式
- 需要保留现有测试与接口运行能力
- 需要给未来前端开发保留平滑适配空间

因此建议先做解释层，再逐步重构工具本体。

---

## 13. LoveManus 重构与迁移方案

### 13.1 LoveManus.java

建议演进为：

- `LoveManusAgentFacade`

或者保留现名，但职责收缩为：

- 组装 ChatClient
- 组装 ToolRegistry
- 组装 SkillRegistry
- 组装 TaskPolicyRegistry
- 启动 Orchestrator

不再负责：

- 写死下一步必须调用哪个工具
- 为模型拼装 PDF 正文
- 基于测试特例兜底下载第一张图

### 13.2 ToolCallAgent.java

建议拆分为：

- `ModelStepExecutor`
- `DefaultCompletionValidator`
- `DefaultRepairStrategy`
- `ToolCallAgentAdapter`

需要迁出的逻辑：

- 强制流程控制
- 固定文件名兜底
- 固定首图兜底
- 测试场景特定的伪完成判断

需要保留的逻辑：

- 工具调用日志
- 安全终止
- 工具结果回写
- 基础真实性校验

### 13.3 ToolRegistration.java

建议保留集中注册思想，同时增强为：

- 注册工具实例
- 注册 ToolDescriptor
- 注册 ToolResultInterpreter

这样既不破坏现有结构，又能支撑新的 Runtime。

---

## 14. 前后端接口兼容设计

### 14.1 基本要求

本次重构主要调整后端内部架构，不改变现有前端正在依赖的协议语义。

### 14.2 需兼容的现有接口

- `GET /api/ai/love_app/chat/sse`
- `GET /api/ai/manus/chat`
- `GET /api/health`

### 14.3 对未来前端开发的支持

重构后，后端应能支持后续扩展以下接口，而无需再次推翻内核：

- 结构化报告生成接口
- RAG 问答接口
- MCP 图片 / 地图接口
- Artifact 列表与元数据接口
- 会话历史回放接口

### 14.4 SSE 兼容要求

当前前端依赖纯文本流，因此本轮要求：

- 保持现有 SSE 文本流可继续消费
- 即使内部引入结构化事件模型，也必须通过适配层转换为兼容现有前端的文本流

---

## 15. 数据与状态管理方案

### 15.1 会话上下文

继续保留现有 `chatId` 多轮会话机制，但需要把状态拆成两层：

- 对话上下文：面向聊天连续性
- Agent 执行状态：面向任务推进、工具调用、Artifact 管理

### 15.2 Artifact 元数据

建议新增内部对象：

- `artifactId`
- `artifactType`
- `localPath`
- `createdByTool`
- `createdAt`
- `readable`

---

## 16. 安全设计

### 16.1 工具安全约束

对现有高风险工具增加统一约束：

- 文件工具：目录白名单
- 下载工具：协议白名单、大小限制、超时限制
- 网页抓取：URL 校验、响应大小限制、超时限制
- 终端工具：命令白名单或实验环境隔离

### 16.2 Agent 安全约束

- 模型不得绕过工具回执谎称完成
- 高风险能力无法安全执行时，应明确失败，而不是猜测执行
- RepairStrategy 不得“偷偷帮模型补业务决策”

---

## 17. 测试方案

### 17.1 测试分层

#### 单元测试

- `TaskClassifierTest`
- `CompletionValidatorTest`
- `DatePlanSkillTest`
- `ToolResultInterpreterTest`

#### 集成测试

- `AgentOrchestratorArtifactFlowTest`
- `AgentOrchestratorAdviceFlowTest`
- `ToolRegistryIntegrationTest`

#### 场景测试

- `LoveManusDatePlanScenarioTest`
- `LoveManusRelationshipAdviceScenarioTest`
- `LoveManusMessageDraftScenarioTest`

### 17.2 LoveManusTest 的重新定位

`LoveManusTest` 不再作为主程序设计的唯一中心测试。

它应该被重新定义为：

- 一个场景测试
- 用于验证“搜索增强 + 图片 + PDF 产物”的能力链路

而不是：

- 驱动整个 Agent 内核结构的主导用例

### 17.3 验收方式

每个重构阶段至少满足以下之一：

- 自动化测试通过
- 提供明确的手工验证步骤

---

## 18. 分阶段实施计划

### 阶段 1：建立核心运行时对象

目标：

- 新增 `AgentTask`
- 新增 `ExecutionState`
- 新增 `SuccessContract`
- 新增 `ValidationResult`

### 阶段 2：抽出 CompletionValidator 与基础 Policy

目标：

- 从 `ToolCallAgent` 中迁出真实性检查、Artifact 检查、外部证据检查

### 阶段 3：抽出 TaskClassifier 与 PlanGenerator

目标：

- 将任务分型与轻计划显式化

### 阶段 4：引入 SkillRegistry

目标：

- 第一批 Skill 上线：
    - `DatePlanSkill`
    - `RelationshipAdviceSkill`
    - `MessageDraftSkill`

### 阶段 5：瘦身 LoveManus / ToolCallAgent

目标：

- 删除测试特例强制流程
- 将 `LoveManus` 和 `ToolCallAgent` 退化为 Facade / Adapter

### 阶段 6：工具返回结构化演进

目标：

- 优先改造 `searchImage`、`downloadResource`、`generatePDF`
- 建立 `ToolResultInterpreter`

### 阶段 7：建立评测集

目标：

- 从单测试驱动转为多任务矩阵评估

---

## 19. 需要删除或限制的现有模式

以下模式不应继续扩张：

- 在 Runtime 中写死特定工具顺序
- 在 Runtime 中固定使用第一张图片
- 在 Runtime 中替模型拼装 PDF 正文
- 在 Runtime 中使用具体场景文件名驱动业务逻辑
- 通过大量字符串判断不断追加补丁式逻辑

---

## 20. 需要保留的现有能力

以下能力应作为重构基础保留：

- 现有 Spring Boot + Spring AI 技术栈
- 现有 SSE 输出模式
- 现有 `ToolRegistration` 集中注册思想
- 现有 PDF 本地图片嵌入能力
- 现有失败后清理坏文件逻辑
- 现有健康检查接口
- 现有会话记忆链路

---

## 21. 对后续前端开发的保障声明

为了满足 `spec.md` 中“前端与前后端交互尚未完整实现”的事实，本方案明确保证：

- 本次重构不改变现有前端正在依赖的接口入口。
- 本次重构优先稳定后端内核，为后续新增页面能力提供统一后端支撑。
- 后续若新增“RAG 页面”“报告页”“Artifact 页”“MCP 结果展示页”，应直接基于本方案的 Runtime / Policy / Skill / Tool 架构扩展，而不是再次推翻核心设计。

---

## 22. 结论

本方案建议将 `ai-love-master-agent` 的后端核心，从“围绕 LoveManusTest 逐步长出的强制流程式 Agent”，重构为“任务分类 + 轻计划 + 自由执行 + 结果契约校验 + Skill 增强”的通用 Agent 内核。

该方案同时满足：

- 不破坏现有前后端接口开发边界
- 不让单一测试场景过拟合主程序
- 保留自主推理与行动能力
- 为未来 RAG、MCP、结构化输出、Artifact 页面化以及多场景情感任务扩展提供稳定基础

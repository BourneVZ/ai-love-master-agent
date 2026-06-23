# ai-love-master-agent 重构任务列表

> **Tasks ID:** 002-core-refactoring-tasks
> **Version:** 1.0
> **Status:** Ready for Implementation
> **Created:** 2026-06-19
> **Based on:** `specs/001-core-functionality/spec.md`, `specs/002-core-refactoring/plan.md`, `constitution.md`

---

## 任务说明

### 标记约定

| 标记 | 含义 |
|------|------|
| `[T]` | 测试任务 Test，必须先于对应实现任务执行 |
| `[I]` | 实现任务 Implementation，对应单文件修改或单文件创建 |
| `[P]` | 可并行执行的任务，与同阶段其他任务无直接依赖 |
| `A -> B` | 依赖关系，表示 A 完成后才能开始 B |

### TDD 规则

每个功能模块必须遵循 **Red-Green-Refactor**：

1. 先写失败测试 `[T]`
2. 再完成最小实现 `[I]`
3. 再进行必要整理 `[I]`

### 任务拆分原则

- 每个任务只允许修改一个主要文件，或只创建一个新文件
- 保持现有对外协议兼容，不改变 `GET /api/ai/love_app/chat/sse`、`GET /api/ai/manus/chat`、`GET /api/health` 的语义
- 优先重构后端内核，不在本轮扩展新前端页面

---

## Phase 1: Foundation Baseline（回归基线与核心契约对象）

> 目标：先锁定接口兼容基线，再建立 Runtime 所需的核心数据对象。

### 1.1 接口兼容基线

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 1.1.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/controller/HealthControllerTest.java` | 回归验证 `GET /api/health` 返回 `ok` |
| 1.1.2 | `[I]` | `src/main/java/com/bvz/aiagent/controller/HealthController.java` | 将健康检查路径修正为 `/api/health` 并保持返回值为 `ok` |

**依赖关系**
`1.1.1 -> 1.1.2`

### 1.2 核心任务与执行态模型

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 1.2.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/model/AgentTaskTest.java` | 定义 `AgentTask` 字段装配、默认值和不可缺失约束 |
| 1.2.2 | `[I]` | `src/main/java/com/bvz/aiagent/core/model/AgentTask.java` | 实现用户任务运行时对象 |
| 1.2.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/model/TaskTypeTest.java` | 定义任务类型枚举覆盖面 |
| 1.2.4 | `[I]` | `src/main/java/com/bvz/aiagent/core/model/TaskType.java` | 实现任务类型枚举 |
| 1.2.5 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/model/ExecutionBudgetTest.java` | 定义最大步数、工具调用数、修复次数和计划轮次约束 |
| 1.2.6 | `[I]` | `src/main/java/com/bvz/aiagent/core/model/ExecutionBudget.java` | 实现执行预算对象 |
| 1.2.7 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/model/ExecutionStateTest.java` | 定义步骤索引、工具历史、观察记录、产物记录和状态迁移 |
| 1.2.8 | `[I]` | `src/main/java/com/bvz/aiagent/core/model/ExecutionState.java` | 实现执行态对象 |
| 1.2.9 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/model/AgentResultTest.java` | 定义最终输出、状态、摘要和产物元数据承载方式 |
| 1.2.10 | `[I]` | `src/main/java/com/bvz/aiagent/core/model/AgentResult.java` | 实现运行结果对象 |

**依赖关系**
`1.2.1 -> 1.2.2`
`1.2.3 -> 1.2.4`
`1.2.5 -> 1.2.6`
`1.2.7 -> 1.2.8`
`1.2.9 -> 1.2.10`

### 1.3 成功契约模型

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 1.3.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/contract/ArtifactContractTest.java` | 定义产物类型、可读性、本地资源和必需章节约束 |
| 1.3.2 | `[I]` | `src/main/java/com/bvz/aiagent/core/contract/ArtifactContract.java` | 实现产物契约对象 |
| 1.3.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/contract/CoverageContractTest.java` | 定义覆盖数量、覆盖率和单项图片数约束 |
| 1.3.4 | `[I]` | `src/main/java/com/bvz/aiagent/core/contract/CoverageContract.java` | 实现覆盖契约对象 |
| 1.3.5 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/contract/SuccessContractTest.java` | 定义外部证据、产物要求、真实性和禁止额外产物组合契约 |
| 1.3.6 | `[I]` | `src/main/java/com/bvz/aiagent/core/contract/SuccessContract.java` | 实现成功契约对象 |
| 1.3.7 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/contract/ValidationResultTest.java` | 定义校验通过、问题列表、可修复标记和摘要行为 |
| 1.3.8 | `[I]` | `src/main/java/com/bvz/aiagent/core/contract/ValidationResult.java` | 实现校验结果对象 |

**依赖关系**
`1.3.1 -> 1.3.2`
`1.3.3 -> 1.3.4`
`1.3.5 -> 1.3.6`
`1.3.7 -> 1.3.8`
`1.3.2, 1.3.4, 1.3.5 -> 1.3.6`

---

## Phase 2: Policy & Completion Validation（策略层与完成校验）

> 目标：把真实性、产物、外部证据和安全边界从旧 Agent 中抽离出来。

### 2.1 Policy 注册与策略实现

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 2.1.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/policy/TaskPolicyRegistryTest.java` | 定义基础策略装配、顺序和按任务返回策略集合行为 |
| 2.1.2 | `[I]` | `src/main/java/com/bvz/aiagent/core/policy/TaskPolicyRegistry.java` | 实现任务策略注册表 |
| 2.1.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/policy/TruthfulnessPolicyTest.java` | 定义未成功工具、文件、下载不得声称已完成 |
| 2.1.4 | `[I]` | `src/main/java/com/bvz/aiagent/core/policy/TruthfulnessPolicy.java` | 实现真实性策略 |
| 2.1.5 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/policy/ArtifactPolicyTest.java` | 定义必须存在真实产物且不得擅自生成额外文本文件 |
| 2.1.6 | `[I]` | `src/main/java/com/bvz/aiagent/core/policy/ArtifactPolicy.java` | 实现产物策略 |
| 2.1.7 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/policy/ExternalEvidencePolicyTest.java` | 定义时效性信息必须先具备外部证据 |
| 2.1.8 | `[I]` | `src/main/java/com/bvz/aiagent/core/policy/ExternalEvidencePolicy.java` | 实现外部证据策略 |
| 2.1.9 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/policy/SafetyBoundaryPolicyTest.java` | 定义高风险工具的目录、命令、URL 和超时约束 |
| 2.1.10 | `[I]` | `src/main/java/com/bvz/aiagent/core/policy/SafetyBoundaryPolicy.java` | 实现安全边界策略 |

**依赖关系**
`2.1.1 -> 2.1.2`
`2.1.3 -> 2.1.4`
`2.1.5 -> 2.1.6`
`2.1.7 -> 2.1.8`
`2.1.9 -> 2.1.10`

### 2.2 完成校验器

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 2.2.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/runtime/DefaultCompletionValidatorTest.java` | 定义完成校验聚合多策略结果并区分可修复与不可修复失败 |
| 2.2.2 | `[I]` | `src/main/java/com/bvz/aiagent/core/runtime/DefaultCompletionValidator.java` | 实现默认完成校验器 |

**依赖关系**
`2.1.2, 2.1.4, 2.1.6, 2.1.8, 2.1.10 -> 2.2.1 -> 2.2.2`

---

## Phase 3: Classification & Planning（任务分类与轻计划）

> 目标：显式表达任务分型和轻计划，替代旧逻辑中的隐式字符串判断。

### 3.1 任务分类模型

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 3.1.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/model/TaskProfileTest.java` | 定义任务分类结果中的任务类型、风险、外部信息需求和产物需求 |
| 3.1.2 | `[I]` | `src/main/java/com/bvz/aiagent/core/model/TaskProfile.java` | 实现任务分类结果对象 |

**依赖关系**
`3.1.1 -> 3.1.2`

### 3.2 分类器与计划器

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 3.2.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/runtime/TaskClassifierTest.java` | 覆盖建议类、检索类、产物类和高风险动作类请求的分类行为 |
| 3.2.2 | `[I]` | `src/main/java/com/bvz/aiagent/core/runtime/TaskClassifier.java` | 实现任务分类器 |
| 3.2.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/model/ExecutionPlanTest.java` | 定义轻计划步骤、目标摘要和计划轮次承载方式 |
| 3.2.4 | `[I]` | `src/main/java/com/bvz/aiagent/core/model/ExecutionPlan.java` | 实现轻计划对象 |
| 3.2.5 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/runtime/PlanGeneratorTest.java` | 定义根据任务和执行态生成轻量计划且不写死工具顺序 |
| 3.2.6 | `[I]` | `src/main/java/com/bvz/aiagent/core/runtime/PlanGenerator.java` | 实现轻计划生成器 |

**依赖关系**
`3.1.2 -> 3.2.1 -> 3.2.2`
`3.2.3 -> 3.2.4`
`3.2.4 -> 3.2.5 -> 3.2.6`

---

## Phase 4: Skills（Skill 注册与情感领域增强）

> 目标：把“约会计划 PDF”从硬编码流程降为 Skill 场景增强。

### 4.1 Skill 基础设施

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 4.1.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/skill/SkillGuidanceTest.java` | 定义 Skill 输出的提示片段、推荐能力和契约增强内容 |
| 4.1.2 | `[I]` | `src/main/java/com/bvz/aiagent/core/skill/SkillGuidance.java` | 实现 Skill 引导对象 |
| 4.1.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/skill/SkillRegistryTest.java` | 定义按任务匹配 Skill、合并引导和增强契约行为 |
| 4.1.4 | `[I]` | `src/main/java/com/bvz/aiagent/core/skill/SkillRegistry.java` | 实现 Skill 注册表 |

**依赖关系**
`4.1.1 -> 4.1.2`
`4.1.2 -> 4.1.3 -> 4.1.4`

### 4.2 情感领域 Skill

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 4.2.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/domain/love/skills/DatePlanSkillTest.java` | 覆盖约会地点推荐、约会计划和周年活动场景的匹配与契约增强 |
| 4.2.2 | `[I]` | `src/main/java/com/bvz/aiagent/domain/love/skills/DatePlanSkill.java` | 实现约会规划 Skill |
| 4.2.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/domain/love/skills/RelationshipAdviceSkillTest.java` | 覆盖情绪安抚、关系分析和沟通建议场景的匹配与安全边界增强 |
| 4.2.4 | `[I]` | `src/main/java/com/bvz/aiagent/domain/love/skills/RelationshipAdviceSkill.java` | 实现关系建议 Skill |
| 4.2.5 | `[T] [P]` | `src/test/java/com/bvz/aiagent/domain/love/skills/MessageDraftSkillTest.java` | 覆盖道歉消息、表白文案和沟通脚本场景的匹配与语气约束 |
| 4.2.6 | `[I]` | `src/main/java/com/bvz/aiagent/domain/love/skills/MessageDraftSkill.java` | 实现消息起草 Skill |

**依赖关系**
`4.1.2 -> 4.2.1 -> 4.2.2`
`4.1.2 -> 4.2.3 -> 4.2.4`
`4.1.2 -> 4.2.5 -> 4.2.6`

---

## Phase 5: Tool Metadata & Interpretation（工具元数据与结果解释层）

> 目标：不推翻现有工具实现，先补描述层和解释层。

### 5.1 工具元数据基础设施

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 5.1.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/tool/ToolDescriptorTest.java` | 定义工具名、能力类型、副作用级别、输入输出模式和产物标识 |
| 5.1.2 | `[I]` | `src/main/java/com/bvz/aiagent/core/tool/ToolDescriptor.java` | 实现工具描述对象 |
| 5.1.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/tool/ToolCapabilityTest.java` | 定义能力类型枚举与高风险级别标识 |
| 5.1.4 | `[I]` | `src/main/java/com/bvz/aiagent/core/tool/ToolCapability.java` | 实现工具能力枚举 |
| 5.1.5 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/tool/ToolResultInterpreterTest.java` | 定义解释器接口契约 |
| 5.1.6 | `[I]` | `src/main/java/com/bvz/aiagent/core/tool/ToolResultInterpreter.java` | 实现解释器接口 |
| 5.1.7 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/tool/ToolResultInterpreterRegistryTest.java` | 定义按工具名注册和获取解释器的行为 |
| 5.1.8 | `[I]` | `src/main/java/com/bvz/aiagent/core/tool/ToolResultInterpreterRegistry.java` | 实现解释器注册表 |

**依赖关系**
`5.1.1 -> 5.1.2`
`5.1.3 -> 5.1.4`
`5.1.5 -> 5.1.6`
`5.1.6 -> 5.1.7 -> 5.1.8`

### 5.2 结构化结果模型

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 5.2.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/tool/model/ImageSearchResultTest.java` | 定义图片搜索结果结构和条目列表承载方式 |
| 5.2.2 | `[I]` | `src/main/java/com/bvz/aiagent/core/tool/model/ImageSearchResult.java` | 实现图片搜索结果模型 |
| 5.2.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/tool/model/DownloadResultTest.java` | 定义下载成功标记、本地路径、MIME 类型和大小字段 |
| 5.2.4 | `[I]` | `src/main/java/com/bvz/aiagent/core/tool/model/DownloadResult.java` | 实现下载结果模型 |
| 5.2.5 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/tool/model/PdfGenerationResultTest.java` | 定义 PDF 成功标记、可读性和嵌入本地资源列表 |
| 5.2.6 | `[I]` | `src/main/java/com/bvz/aiagent/core/tool/model/PdfGenerationResult.java` | 实现 PDF 结果模型 |
| 5.2.7 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/tool/model/WebSearchResultTest.java` | 定义 Web 搜索结果结构和前 5 条结果承载方式 |
| 5.2.8 | `[I]` | `src/main/java/com/bvz/aiagent/core/tool/model/WebSearchResult.java` | 实现 Web 搜索结果模型 |

**依赖关系**
`5.2.1 -> 5.2.2`
`5.2.3 -> 5.2.4`
`5.2.5 -> 5.2.6`
`5.2.7 -> 5.2.8`

### 5.3 工具结果解释器

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 5.3.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/tool/interpreter/ImageSearchResultInterpreterTest.java` | 定义 `searchImage` 原始返回的结构化解析行为 |
| 5.3.2 | `[I]` | `src/main/java/com/bvz/aiagent/core/tool/interpreter/ImageSearchResultInterpreter.java` | 实现图片搜索结果解释器 |
| 5.3.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/tool/interpreter/DownloadResultInterpreterTest.java` | 定义 `downloadResource` 原始返回的结构化解析行为 |
| 5.3.4 | `[I]` | `src/main/java/com/bvz/aiagent/core/tool/interpreter/DownloadResultInterpreter.java` | 实现下载结果解释器 |
| 5.3.5 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/tool/interpreter/PdfGenerationResultInterpreterTest.java` | 定义 `generatePDF` 原始返回的结构化解析行为 |
| 5.3.6 | `[I]` | `src/main/java/com/bvz/aiagent/core/tool/interpreter/PdfGenerationResultInterpreter.java` | 实现 PDF 结果解释器 |
| 5.3.7 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/tool/interpreter/WebSearchResultInterpreterTest.java` | 定义 `searchWeb` 原始返回的结构化解析行为 |
| 5.3.8 | `[I]` | `src/main/java/com/bvz/aiagent/core/tool/interpreter/WebSearchResultInterpreter.java` | 实现 Web 搜索结果解释器 |

**依赖关系**
`5.1.6, 5.2.2 -> 5.3.1 -> 5.3.2`
`5.1.6, 5.2.4 -> 5.3.3 -> 5.3.4`
`5.1.6, 5.2.6 -> 5.3.5 -> 5.3.6`
`5.1.6, 5.2.8 -> 5.3.7 -> 5.3.8`

### 5.4 工具统一注册回归

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 5.4.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/tools/ToolRegistrationIntegrationTest.java` | 回归验证工具实例、ToolDescriptor 和 ToolResultInterpreter 的统一注册行为 |
| 5.4.2 | `[I]` | `src/main/java/com/bvz/aiagent/tools/ToolRegistration.java` | 保留集中注册方式并补充元数据与解释器注册 |

**依赖关系**
`5.1.2, 5.1.4, 5.1.8, 5.3.2, 5.3.4, 5.3.6, 5.3.8 -> 5.4.1 -> 5.4.2`

---

## Phase 6: Runtime Core（主执行循环与适配层）

> 目标：建立新的通用 Agent Runtime，并给旧 Agent 留出兼容适配层。

### 6.1 修复与单步执行模型

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 6.1.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/model/RepairInstructionTest.java` | 定义修复提示、修复原因和是否允许重试字段 |
| 6.1.2 | `[I]` | `src/main/java/com/bvz/aiagent/core/model/RepairInstruction.java` | 实现修复指令对象 |
| 6.1.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/model/StepResultTest.java` | 定义单步执行的消息追加、工具调用、状态变化和终止标记 |
| 6.1.4 | `[I]` | `src/main/java/com/bvz/aiagent/core/model/StepResult.java` | 实现单步执行结果对象 |

**依赖关系**
`6.1.1 -> 6.1.2`
`6.1.3 -> 6.1.4`

### 6.2 执行器与修复策略

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 6.2.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/runtime/ModelStepExecutorTest.java` | 定义模型下一步决策、工具调用解析、工具结果回写和 terminate 处理行为 |
| 6.2.2 | `[I]` | `src/main/java/com/bvz/aiagent/core/runtime/ModelStepExecutor.java` | 实现模型单步执行器 |
| 6.2.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/runtime/DefaultRepairStrategyTest.java` | 定义可修复失败时如何生成修复指令且不替模型做业务决策 |
| 6.2.4 | `[I]` | `src/main/java/com/bvz/aiagent/core/runtime/DefaultRepairStrategy.java` | 实现默认修复策略 |

**依赖关系**
`5.1.8, 6.1.4 -> 6.2.1 -> 6.2.2`
`6.1.2 -> 6.2.3 -> 6.2.4`

### 6.3 主执行循环与旧 Agent 适配

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 6.3.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/runtime/AgentOrchestratorArtifactFlowTest.java` | 集成验证产物型任务链路 |
| 6.3.2 | `[T] [P]` | `src/test/java/com/bvz/aiagent/core/runtime/AgentOrchestratorAdviceFlowTest.java` | 集成验证建议型任务可自然结束 |
| 6.3.3 | `[I]` | `src/main/java/com/bvz/aiagent/core/runtime/AgentOrchestrator.java` | 实现主执行循环 |
| 6.3.4 | `[T] [P]` | `src/test/java/com/bvz/aiagent/agent/ToolCallAgentAdapterTest.java` | 定义旧 Agent 与新 Runtime 的适配边界 |
| 6.3.5 | `[I]` | `src/main/java/com/bvz/aiagent/agent/ToolCallAgentAdapter.java` | 实现旧 Agent 到新 Runtime 的适配器 |

**依赖关系**
`2.2.2, 3.2.2, 3.2.6, 4.1.4, 6.2.2, 6.2.4 -> 6.3.1`
`2.2.2, 3.2.2, 3.2.6, 4.1.4, 6.2.2, 6.2.4 -> 6.3.2`
`6.3.1, 6.3.2 -> 6.3.3`
`6.2.2, 6.3.3 -> 6.3.4 -> 6.3.5`

---

## Phase 7: Manus Migration & API Compatibility（LoveManus 迁移与接口兼容）

> 目标：把旧实现退化为 Facade/Adapter，同时补齐当前规格要求的控制器层。

### 7.1 LoveManus 场景回归

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 7.1.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/agent/LoveManusDatePlanScenarioTest.java` | 验证约会计划 + 图片 + PDF 场景在新 Runtime 下仍可完成 |
| 7.1.2 | `[T] [P]` | `src/test/java/com/bvz/aiagent/agent/LoveManusRelationshipAdviceScenarioTest.java` | 验证关系建议场景不被工具链硬编码绑架 |
| 7.1.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/agent/LoveManusMessageDraftScenarioTest.java` | 验证消息起草场景默认无需外部工具 |
| 7.1.4 | `[I]` | `src/test/java/com/bvz/aiagent/agent/LoveManusTest.java` | 将现有单一大场景测试重定位为总入口回归测试 |

**依赖关系**
`6.3.3, 6.3.5 -> 7.1.1`
`6.3.3, 6.3.5 -> 7.1.2`
`6.3.3, 6.3.5 -> 7.1.3`
`7.1.1, 7.1.2, 7.1.3 -> 7.1.4`

### 7.2 LoveManus 与 ToolCallAgent 迁移

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 7.2.1 | `[I]` | `src/main/java/com/bvz/aiagent/agent/ToolCallAgent.java` | 移除强制工具顺序、首图兜底、固定文件名和伪完成判断，改为委托 `ToolCallAgentAdapter` |
| 7.2.2 | `[I]` | `src/main/java/com/bvz/aiagent/agent/LoveManus.java` | 收缩为 Facade，仅负责组装 ChatClient、工具注册、SkillRegistry、TaskPolicyRegistry 和 Orchestrator |

**依赖关系**
`6.3.5, 7.1.4 -> 7.2.1`
`4.1.4, 2.1.2, 6.3.3, 7.2.1 -> 7.2.2`

### 7.3 控制器兼容层

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 7.3.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/controller/LoveAppControllerTest.java` | 定义恋爱大师 `GET /api/ai/love_app/chat/sse` 的 SSE 协议和参数透传行为 |
| 7.3.2 | `[I]` | `src/main/java/com/bvz/aiagent/controller/LoveAppController.java` | 提供恋爱大师 SSE 接口并将请求委托给 `LoveApp` |
| 7.3.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/controller/LoveManusControllerTest.java` | 定义超级智能体 `GET /api/ai/manus/chat` 的 SSE 协议和文本流兼容行为 |
| 7.3.4 | `[I]` | `src/main/java/com/bvz/aiagent/controller/LoveManusController.java` | 提供 LoveManus SSE 接口并保持现有文本流消费协议 |

**依赖关系**
`7.3.1 -> 7.3.2`
`7.2.2 -> 7.3.3 -> 7.3.4`

### 7.4 LoveApp Facade 回归

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 7.4.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/app/LoveAppTest.java` | 补充 chatId 记忆、报告、RAG、工具调用和 MCP 的回归断言 |
| 7.4.2 | `[I]` | `src/main/java/com/bvz/aiagent/app/LoveApp.java` | 保持其为聊天应用 Facade，不承载新的 Runtime 主循环细节 |

**依赖关系**
`7.4.1 -> 7.4.2`

---

## Phase 8: Structured Tool Output & Evaluation（重点工具结构化输出与评测集）

> 目标：优先让关键工具输出可稳定解释，再建立多任务矩阵评测入口。

### 8.1 重点工具输出稳定化

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 8.1.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/tools/ResourceDownloadToolTest.java` | 补充下载成功与失败时可被 `DownloadResultInterpreter` 稳定解析的断言 |
| 8.1.2 | `[I]` | `src/main/java/com/bvz/aiagent/tools/ResourceDownloadTool.java` | 将下载结果输出稳定到结构化解释层可消费格式 |
| 8.1.3 | `[T] [P]` | `src/test/java/com/bvz/aiagent/tools/PDFGenerationToolTest.java` | 补充 PDF 成功、可读和坏文件清理行为的结构化断言 |
| 8.1.4 | `[I]` | `src/main/java/com/bvz/aiagent/tools/PDFGenerationTool.java` | 将 PDF 生成结果输出稳定到结构化解释层可消费格式 |
| 8.1.5 | `[T] [P]` | `src/test/java/com/bvz/aiagent/tools/WebSearchToolTest.java` | 补充前 5 条结果和异常路径可被 `WebSearchResultInterpreter` 稳定解析的断言 |
| 8.1.6 | `[I]` | `src/main/java/com/bvz/aiagent/tools/WebSearchTool.java` | 将 Web 搜索结果输出稳定到结构化解释层可消费格式 |

**依赖关系**
`5.3.4 -> 8.1.1 -> 8.1.2`
`5.3.6 -> 8.1.3 -> 8.1.4`
`5.3.8 -> 8.1.5 -> 8.1.6`

### 8.2 多任务矩阵评测

| ID | 类型 | 文件 | 说明 |
|----|------|------|------|
| 8.2.1 | `[T] [P]` | `src/test/java/com/bvz/aiagent/eval/LoveManusEvaluationMatrixTest.java` | 建立多任务矩阵评测入口，覆盖约会计划、关系建议和消息起草三类场景 |
| 8.2.2 | `[I]` | `src/main/java/com/bvz/aiagent/eval/LoveTaskScenarioCatalog.java` | 沉淀评测场景定义、输入样例和期望契约 |

**依赖关系**
`7.1.1, 7.1.2, 7.1.3, 7.2.2 -> 8.2.1 -> 8.2.2`

---

## 总结

### 任务统计

| Phase | 任务数 | 测试任务 | 实现任务 |
|-------|--------|----------|----------|
| Phase 1: Foundation Baseline | 20 | 10 | 10 |
| Phase 2: Policy & Completion Validation | 12 | 6 | 6 |
| Phase 3: Classification & Planning | 6 | 3 | 3 |
| Phase 4: Skills | 10 | 5 | 5 |
| Phase 5: Tool Metadata & Interpretation | 26 | 13 | 13 |
| Phase 6: Runtime Core | 10 | 5 | 5 |
| Phase 7: Manus Migration & API Compatibility | 12 | 5 | 7 |
| Phase 8: Structured Tool Output & Evaluation | 8 | 4 | 4 |
| **总计** | **104** | **51** | **53** |

### 推荐执行顺序

1. 先完成 Phase 1 和 Phase 2，建立稳定契约和校验基础
2. 再完成 Phase 3 和 Phase 4，显式化任务分类、计划和 Skill
3. 然后推进 Phase 5 和 Phase 6，补足工具解释层与 Runtime 内核
4. 最后执行 Phase 7 和 Phase 8，完成迁移、接口兼容和评测沉淀

### 可并行开发建议

- Developer A：Phase 1、Phase 2
- Developer B：Phase 3、Phase 4
- Developer C：Phase 5
- Developer D：在 Phase 6 依赖满足后推进 Runtime 和迁移

---

> **文档版本历史**
> - v1.0 (2026-06-19): 按参考 `tasks.md` 的组织格式重排重构任务列表

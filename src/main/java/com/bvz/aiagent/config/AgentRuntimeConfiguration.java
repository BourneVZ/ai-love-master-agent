package com.bvz.aiagent.config;

import com.bvz.aiagent.agent.ToolCallAgentAdapter;
import com.bvz.aiagent.core.policy.ArtifactPolicy;
import com.bvz.aiagent.core.policy.ExternalEvidencePolicy;
import com.bvz.aiagent.core.policy.SafetyBoundaryPolicy;
import com.bvz.aiagent.core.policy.TaskPolicy;
import com.bvz.aiagent.core.policy.TaskPolicyRegistry;
import com.bvz.aiagent.core.policy.TruthfulnessPolicy;
import com.bvz.aiagent.core.runtime.AgentOrchestrator;
import com.bvz.aiagent.core.runtime.DefaultCompletionValidator;
import com.bvz.aiagent.core.runtime.DefaultRepairStrategy;
import com.bvz.aiagent.core.runtime.ModelStepExecutor;
import com.bvz.aiagent.core.runtime.PlanGenerator;
import com.bvz.aiagent.core.runtime.TaskClassifier;
import com.bvz.aiagent.core.skill.Skill;
import com.bvz.aiagent.core.skill.SkillRegistry;
import com.bvz.aiagent.core.tool.ToolResultInterpreterRegistry;
import com.bvz.aiagent.domain.love.skills.DatePlanSkill;
import com.bvz.aiagent.domain.love.skills.MessageDraftSkill;
import com.bvz.aiagent.domain.love.skills.RelationshipAdviceSkill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AgentRuntimeConfiguration {

    @Bean
    public TaskClassifier taskClassifier() {
        return new TaskClassifier();
    }

    @Bean
    public PlanGenerator planGenerator() {
        return new PlanGenerator();
    }

    @Bean
    public ModelStepExecutor modelStepExecutor(ToolResultInterpreterRegistry interpreterRegistry) {
        return new ModelStepExecutor(interpreterRegistry);
    }

    @Bean
    public DefaultRepairStrategy defaultRepairStrategy() {
        return new DefaultRepairStrategy();
    }

    @Bean
    public TruthfulnessPolicy truthfulnessPolicy() {
        return new TruthfulnessPolicy();
    }

    @Bean
    public ArtifactPolicy artifactPolicy() {
        return new ArtifactPolicy();
    }

    @Bean
    public ExternalEvidencePolicy externalEvidencePolicy() {
        return new ExternalEvidencePolicy();
    }

    @Bean
    public SafetyBoundaryPolicy safetyBoundaryPolicy() {
        return new SafetyBoundaryPolicy();
    }

    @Bean
    public TaskPolicyRegistry taskPolicyRegistry(List<TaskPolicy> policies) {
        return new TaskPolicyRegistry(policies);
    }

    @Bean
    public DefaultCompletionValidator defaultCompletionValidator(TaskPolicyRegistry taskPolicyRegistry) {
        return new DefaultCompletionValidator(taskPolicyRegistry);
    }

    @Bean
    public DatePlanSkill datePlanSkill() {
        return new DatePlanSkill();
    }

    @Bean
    public RelationshipAdviceSkill relationshipAdviceSkill() {
        return new RelationshipAdviceSkill();
    }

    @Bean
    public MessageDraftSkill messageDraftSkill() {
        return new MessageDraftSkill();
    }

    @Bean
    public SkillRegistry skillRegistry(List<Skill> skills) {
        return new SkillRegistry(skills);
    }

    @Bean
    public AgentOrchestrator agentOrchestrator(
            TaskClassifier taskClassifier,
            PlanGenerator planGenerator,
            ModelStepExecutor modelStepExecutor,
            DefaultCompletionValidator defaultCompletionValidator,
            DefaultRepairStrategy defaultRepairStrategy,
            SkillRegistry skillRegistry
    ) {
        return new AgentOrchestrator(
                taskClassifier,
                planGenerator,
                modelStepExecutor,
                defaultCompletionValidator,
                defaultRepairStrategy,
                skillRegistry
        );
    }

    @Bean
    public ToolCallAgentAdapter toolCallAgentAdapter(AgentOrchestrator agentOrchestrator) {
        return new ToolCallAgentAdapter(agentOrchestrator);
    }
}

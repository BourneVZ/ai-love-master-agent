package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.model.TaskProfile;
import com.bvz.aiagent.core.model.TaskType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskClassifierTest {

    private final TaskClassifier classifier = new TaskClassifier();

    @Test
    void shouldClassifyAdviceRequestWithoutExternalInfoOrArtifact() {
        TaskProfile profile = classifier.classify("我和对象最近总是冷战，给我一些沟通建议");

        assertEquals(TaskType.ADVICE, profile.taskType());
        assertEquals("LOW", profile.riskLevel());
        assertFalse(profile.externalInfoRequired());
        assertFalse(profile.artifactRequired());
    }

    @Test
    void shouldClassifyResearchRequestAsRequiringExternalEvidence() {
        TaskProfile profile = classifier.classify("今天上海有哪些适合情侣去的小众展览");

        assertEquals(TaskType.RESEARCH, profile.taskType());
        assertTrue(profile.externalInfoRequired());
        assertFalse(profile.artifactRequired());
    }

    @Test
    void shouldClassifyArtifactRequestAsArtifactTask() {
        TaskProfile profile = classifier.classify("帮我生成一份带地点推荐的约会计划 PDF");

        assertEquals(TaskType.ARTIFACT, profile.taskType());
        assertTrue(profile.externalInfoRequired());
        assertTrue(profile.artifactRequired());
    }

    @Test
    void shouldClassifyHighRiskActionRequestWithHigherRiskLevel() {
        TaskProfile profile = classifier.classify("执行终端命令删除一个目录并下载远程脚本");

        assertEquals(TaskType.ACTION, profile.taskType());
        assertEquals("HIGH", profile.riskLevel());
    }
}

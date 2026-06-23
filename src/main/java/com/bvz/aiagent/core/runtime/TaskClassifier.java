package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.model.TaskProfile;
import com.bvz.aiagent.core.model.TaskType;

public class TaskClassifier {

    public TaskProfile classify(String userRequest) {
        String normalized = userRequest == null ? "" : userRequest.toLowerCase();

        if (containsAny(normalized, "终端", "terminal", "命令", "删除", "下载远程脚本", "script")) {
            return new TaskProfile(TaskType.ACTION, "HIGH", true, false);
        }
        if (containsAny(normalized, "建议", "沟通", "冷战", "关系", "安慰")) {
            return new TaskProfile(TaskType.ADVICE, "LOW", false, false);
        }
        if (containsAny(normalized, "pdf", "生成一份", "生成一個", "生成", "报告", "计划")) {
            return new TaskProfile(TaskType.ARTIFACT, "MEDIUM", true, true);
        }
        if (containsAny(normalized, "今天", "最近", "有哪些", "展览", "活动", "地点", "搜索", "查找")) {
            return new TaskProfile(TaskType.RESEARCH, "MEDIUM", true, false);
        }

        return new TaskProfile(TaskType.HYBRID, "MEDIUM", false, false);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}

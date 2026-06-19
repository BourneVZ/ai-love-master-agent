package com.bvz.aiagent.eval;

import java.util.List;

public class LoveTaskScenarioCatalog {

    public List<Scenario> scenarios() {
        return List.of(
                new Scenario("date-plan", "Plan a date, include images, and generate a PDF.", "ARTIFACT"),
                new Scenario("relationship-advice", "We argued yesterday. Help me think through how to talk to her.", "ADVICE"),
                new Scenario("message-draft", "Draft a sincere apology text for my girlfriend.", "MESSAGE_DRAFT")
        );
    }

    public record Scenario(String id, String userPrompt, String category) {
    }
}

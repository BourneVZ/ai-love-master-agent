package com.bvz.aiagent.agent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.main.lazy-initialization=true"
})
class LoveManusRealAcceptanceTest {

    @jakarta.annotation.Resource
    private LoveManus loveManus;

    @Test
    void shouldHandleOriginalDatePlanPromptWithRealRuntimePath() {
        String userPrompt = """
                我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点，
                并结合一些网络图片，制定一份详细的约会计划，
                并以 PDF 格式输出
                """;

        String answer = loveManus.run(userPrompt);

        assertNotNull(answer);
        assertFalse(answer.isBlank(), "LoveManus 返回为空，说明重构后的真实执行链路没有产出可交付结果");
    }
}

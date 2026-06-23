package com.bvz.aiagent.app;

import cn.hutool.core.lang.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LoveAppRealAcceptanceTest {

    @jakarta.annotation.Resource
    private LoveApp loveApp;

    @Test
    void shouldKeepConversationMemoryAcrossThreeTurns() {
        String chatId = UUID.randomUUID().toString();

        String answer1 = loveApp.doChat("你好，我是程序员BVZ", chatId);
        String answer2 = loveApp.doChat("我想让另一半（HW）更爱我", chatId);
        String answer3 = loveApp.doChat("我的另一半叫什么来着？刚跟你说过，帮我回忆一下", chatId);

        assertNotNull(answer1);
        assertNotNull(answer2);
        assertNotNull(answer3);
        assertFalse(answer3.isBlank());
        assertTrue(answer3.contains("HW"), "第三轮未能回忆出前文中的另一半信息，说明 chatId 记忆链路未正确生效");
    }

    @Test
    void shouldGenerateLoveReportWithTitleAndSuggestions() {
        String chatId = UUID.randomUUID().toString();
        LoveApp.LoveReport report = loveApp.doChatWithReport("我想让另一半（编程导航）更爱我，但我不知道该怎么做", chatId);

        assertNotNull(report);
        assertNotNull(report.title());
        assertFalse(report.title().isBlank());
        assertNotNull(report.suggestions());
        assertFalse(report.suggestions().isEmpty(), "结构化恋爱报告没有生成建议列表");
    }

    @Test
    void shouldAnswerMarriageQuestionWithRagPath() {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithRag("我已经结婚了，但是婚后关系不太亲密，怎么办？", chatId);

        assertNotNull(answer);
        assertFalse(answer.isBlank());
    }

    @Test
    void shouldHandleRepresentativeToolScenario() {
        assertToolMessage("周末想带女朋友去杭州约会，推荐几个适合情侣的小众打卡地？");
    }

    @Test
    void shouldHandleRepresentativeMcpScenario() {
        String chatId = UUID.randomUUID().toString();
        String mapAnswer = loveApp.doChatWithMcp("我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点", chatId);

        assertNotNull(mapAnswer);
        assertFalse(mapAnswer.isBlank());
    }

    private void assertToolMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        assertNotNull(answer);
        assertFalse(answer.isBlank(), "工具增强聊天未返回有效内容: " + message);
    }
}

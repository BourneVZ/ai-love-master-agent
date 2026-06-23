package com.bvz.aiagent.app;

import cn.hutool.core.lang.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Manual high-cost acceptance matrix retained for parity with the original LoveAppTest.
 * Run this class only when you explicitly need the full external-dependency sweep.
 */
@SpringBootTest
class LoveAppRealAcceptanceManualMatrixTest {

    @jakarta.annotation.Resource
    private LoveApp loveApp;

    @Test
    void shouldHandleOriginalToolScenarios() {
        assertToolMessage("周末想带女朋友去杭州约会，推荐几个适合情侣的小众打卡地？");
        assertToolMessage("最近和对象吵架了，看看交友网站（https://github.com/BourneVZ）的其他情侣是怎么解决矛盾的？");
        assertToolMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");
        assertToolMessage("执行 Python3 脚本来生成数据分析报告");
        assertToolMessage("保存我的恋爱档案为文件");
        assertToolMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    @Test
    void shouldHandleOriginalMcpScenarios() {
        String chatId = UUID.randomUUID().toString();

        String mapAnswer = loveApp.doChatWithMcp("我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点", chatId);
        String imageAnswer = loveApp.doChatWithMcp("帮我搜索一些哄另一半开心的图片", chatId);

        assertNotNull(mapAnswer);
        assertNotNull(imageAnswer);
        assertFalse(mapAnswer.isBlank());
        assertFalse(imageAnswer.isBlank());
    }

    private void assertToolMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        assertNotNull(answer);
        assertFalse(answer.isBlank(), "工具增强聊天未返回有效内容: " + message);
    }
}

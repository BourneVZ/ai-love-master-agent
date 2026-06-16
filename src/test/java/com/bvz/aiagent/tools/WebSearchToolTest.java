package com.bvz.aiagent.tools;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WebSearchToolTest {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Test
    public void testSearchWeb() {
        WebSearchTool tool = new WebSearchTool(searchApiKey);
        String query = "求之于势，不责于人";
        String result = tool.searchWeb(query);

        assertNotNull(result, "搜索结果不应为 null");
        assertFalse(result.isEmpty(), "搜索结果不应为空");
        assertFalse(result.startsWith("Error"), "搜索结果不应以 Error 开头: " + result);
    }
}

package com.bvz.aiagent.tools;

import com.bvz.aiagent.core.tool.interpreter.WebSearchResultInterpreter;
import com.bvz.aiagent.core.tool.model.WebSearchResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WebSearchToolTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldProduceTopFiveSearchItemsConsumableByInterpreter() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/v1/search", exchange -> write(exchange, """
                {
                  "organic_results": [
                    {"title":"A","snippet":"S1","link":"https://example.com/a"},
                    {"title":"B","snippet":"S2","link":"https://example.com/b"},
                    {"title":"C","snippet":"S3","link":"https://example.com/c"},
                    {"title":"D","snippet":"S4","link":"https://example.com/d"},
                    {"title":"E","snippet":"S5","link":"https://example.com/e"},
                    {"title":"F","snippet":"S6","link":"https://example.com/f"}
                  ]
                }
                """));
        server.start();

        WebSearchTool tool = new WebSearchTool("fake-key");
        replaceClient(tool, rewriteClientToLocalServer());

        String raw = tool.searchWeb("phase8");
        WebSearchResult result = new WebSearchResultInterpreter().interpret(raw);

        assertEquals(5, result.items().size());
        assertEquals("A", result.items().getFirst().title());
        assertEquals("https://example.com/e", result.items().get(4).link());
    }

    @Test
    void shouldProduceInterpreterConsumableFailureOutputOnSearchException() throws Exception {
        WebSearchTool tool = new WebSearchTool("fake-key");
        replaceClient(tool, new OkHttpClient.Builder()
                .addInterceptor(chain -> { throw new IOException("boom"); })
                .build());

        String raw = tool.searchWeb("phase8-error");

        WebSearchResult result = assertDoesNotThrow(() -> new WebSearchResultInterpreter().interpret(raw));
        assertEquals(0, result.items().size());
    }

    private OkHttpClient rewriteClientToLocalServer() {
        return new OkHttpClient.Builder()
                .addInterceptor((Interceptor) chain -> {
                    Request original = chain.request();
                    HttpUrl localUrl = new HttpUrl.Builder()
                            .scheme("http")
                            .host("localhost")
                            .port(server.getAddress().getPort())
                            .addPathSegments("api/v1/search")
                            .query(original.url().query())
                            .build();
                    Request redirected = original.newBuilder().url(localUrl).build();
                    return chain.proceed(redirected);
                })
                .build();
    }

    private static void replaceClient(WebSearchTool tool, OkHttpClient client) throws Exception {
        Field field = WebSearchTool.class.getDeclaredField("client");
        field.setAccessible(true);
        field.set(tool, client);
    }

    private static void write(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        } finally {
            exchange.close();
        }
    }
}

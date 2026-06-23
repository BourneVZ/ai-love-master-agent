package com.bvz.aiagent.tools;

import com.bvz.aiagent.core.tool.interpreter.DownloadResultInterpreter;
import com.bvz.aiagent.core.tool.model.DownloadResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceDownloadToolTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldProduceStructuredDownloadOutputForSuccessfulDownload() throws Exception {
        byte[] pngBytes = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+yF9kAAAAASUVORK5CYII="
        );
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/image.png", exchange -> write(exchange, 200, "image/png", pngBytes));
        server.start();

        ResourceDownloadTool tool = new ResourceDownloadTool();
        String raw = tool.downloadResource("http://localhost:" + server.getAddress().getPort() + "/image.png", "phase8-logo.png");

        DownloadResult result = new DownloadResultInterpreter().interpret(raw);

        assertTrue(result.success());
        assertTrue(result.localPath().endsWith("phase8-logo.png"));
        assertEquals("image/png", result.mimeType());
        assertEquals(pngBytes.length, result.size());
    }

    @Test
    void shouldProduceStructuredDownloadOutputForUnsupportedFormatFailure() throws Exception {
        byte[] textBytes = "not-an-image".getBytes();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/bad.txt", exchange -> write(exchange, 200, "text/plain", textBytes));
        server.start();

        ResourceDownloadTool tool = new ResourceDownloadTool();
        String raw = tool.downloadResource("http://localhost:" + server.getAddress().getPort() + "/bad.txt", "bad.txt");

        DownloadResult result = new DownloadResultInterpreter().interpret(raw);

        assertFalse(result.success());
        assertEquals("text/plain", result.mimeType());
        assertEquals(0L, result.size());
    }

    private static void write(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(body);
        } finally {
            exchange.close();
        }
    }
}

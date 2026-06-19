package com.bvz.aiagent.controller;

import com.bvz.aiagent.app.LoveApp;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoveAppController {

    private final LoveApp loveApp;

    public LoveAppController(LoveApp loveApp) {
        this.loveApp = loveApp;
    }

    @GetMapping(value = "/api/ai/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public String chat(
            @RequestParam("message") String message,
            @RequestParam("chatId") String chatId
    ) {
        return loveApp.doChat(message, chatId);
    }
}

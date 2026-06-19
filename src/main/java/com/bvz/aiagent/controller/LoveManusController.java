package com.bvz.aiagent.controller;

import com.bvz.aiagent.agent.LoveManus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoveManusController {

    private final LoveManus loveManus;

    public LoveManusController(LoveManus loveManus) {
        this.loveManus = loveManus;
    }

    @GetMapping(value = "/api/ai/manus/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public String chat(@RequestParam("message") String message) {
        return loveManus.run(message);
    }
}

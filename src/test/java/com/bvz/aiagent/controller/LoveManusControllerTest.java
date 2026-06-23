package com.bvz.aiagent.controller;

import com.bvz.aiagent.agent.LoveManus;
import com.bvz.aiagent.app.LoveApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
class LoveManusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoveManus loveManus;

    @MockBean
    private LoveApp loveApp;

    @Test
    void shouldExposeLoveManusSseEndpointAndKeepTextStreamCompatibility() throws Exception {
        SseEmitter emitter = new SseEmitter();
        CompletableFuture.runAsync(() -> {
            try {
                emitter.send("completed");
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
        when(loveManus.runStream("draft a date plan")).thenReturn(emitter);

        MvcResult mvcResult = mockMvc.perform(get("/ai/manus/chat")
                        .param("message", "draft a date plan"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("completed")));
    }
}

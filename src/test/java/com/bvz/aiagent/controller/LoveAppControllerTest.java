package com.bvz.aiagent.controller;

import com.bvz.aiagent.agent.LoveManus;
import com.bvz.aiagent.app.LoveApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
class LoveAppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoveApp loveApp;

    @MockBean
    private LoveManus loveManus;

    @Test
    void shouldExposeLoveAppSseEndpointAndPassThroughParameters() throws Exception {
        when(loveApp.doChatByStream("hello", "chat-1")).thenReturn(Flux.just("advice"));

        mockMvc.perform(get("/ai/love_app/chat/sse")
                        .param("message", "hello")
                        .param("chatId", "chat-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("advice")));
    }
}

package com.bvz.aiagent.controller;

import com.bvz.aiagent.agent.LoveManus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoveManusController.class)
class LoveManusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoveManus loveManus;

    @Test
    void shouldExposeLoveManusSseEndpointAndKeepTextStreamCompatibility() throws Exception {
        when(loveManus.run("draft a date plan")).thenReturn("completed");

        mockMvc.perform(get("/api/ai/manus/chat")
                        .param("message", "draft a date plan"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("completed")));
    }
}

package com.multimodal.interview.controller;

import com.multimodal.interview.reactagent.ReactAgentRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class LegacyXunfeiFeatureRemovalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReactAgentRouter reactAgentRouter;

    @Test
    void shouldRejectAgentRequestWithoutAgentKey() throws Exception {
        mockMvc.perform(post("/api/xunfei/getAgentAnswer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "systemContent": "system",
                                  "userContent": "user"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnNotFoundForFacialAnalysisEndpoint() throws Exception {
        mockMvc.perform(multipart("/api/face/getAnalysis")
                        .file("file", "demo".getBytes()))
                .andExpect(status().isNotFound());
    }
}

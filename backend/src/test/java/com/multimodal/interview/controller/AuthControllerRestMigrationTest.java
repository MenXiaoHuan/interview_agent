package com.multimodal.interview.controller;

import com.multimodal.interview.common.security.RsaPasswordCryptoService;
import com.multimodal.interview.service.AuthService;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerRestMigrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private RsaPasswordCryptoService rsaPasswordCryptoService;

    @MockBean
    private Bucket bucket;

    @Test
    void shouldReturnRsaPublicKey() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(true);
        when(rsaPasswordCryptoService.getPublicKeyPem()).thenReturn("-----BEGIN PUBLIC KEY-----demo");

        mockMvc.perform(get("/api/auth/rsa-public-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("-----BEGIN PUBLIC KEY-----demo"));
    }
}

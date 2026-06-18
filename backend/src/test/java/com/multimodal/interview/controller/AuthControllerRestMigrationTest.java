package com.multimodal.interview.controller;

import com.multimodal.interview.common.security.RsaPasswordCryptoService;
import com.multimodal.interview.common.interceptor.RateLimitInterceptor;
import com.multimodal.interview.dto.LoginRequest;
import com.multimodal.interview.entity.User;
import com.multimodal.interview.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
    private RateLimitInterceptor rateLimitInterceptor;

    @Test
    void shouldReturnRsaPublicKey() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(rsaPasswordCryptoService.getPublicKeyPem()).thenReturn("-----BEGIN PUBLIC KEY-----demo");

        mockMvc.perform(get("/api/auth/rsa-public-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("-----BEGIN PUBLIC KEY-----demo"));
    }

    @Test
    void shouldDecryptPasswordBeforeLogin() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin001");
        user.setNickname("Admin");
        user.setUserType(1);
        user.setEyeCareMode(0);
        user.setSurpriseMode(0);

        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(rsaPasswordCryptoService.decrypt(any(LoginRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authService.login(any())).thenReturn(user);
        when(authService.generateToken(any())).thenReturn("token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin001",
                                  "password": "RSA:encrypted"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("token"));

        verify(rsaPasswordCryptoService).decrypt(any(LoginRequest.class));
    }
}

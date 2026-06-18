package com.multimodal.interview.security;

import com.multimodal.interview.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityExposureConfigTest {

    @Test
    void corsUsesConfiguredOriginsWithoutWildcardCredentialPattern() {
        CorsConfig config = new CorsConfig("https://app.example.com,https://admin.example.com");

        CorsConfiguration cors = config.corsConfigurationSource()
                .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/user"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactly("https://app.example.com", "https://admin.example.com");
        assertThat(cors.getAllowedOriginPatterns()).isNullOrEmpty();
        assertThat(cors.getAllowCredentials()).isTrue();
    }
}

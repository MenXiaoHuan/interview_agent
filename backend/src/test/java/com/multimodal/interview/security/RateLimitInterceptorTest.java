package com.multimodal.interview.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.common.interceptor.RateLimitInterceptor;
import com.multimodal.interview.config.RateLimitBucketConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitInterceptorTest {

    @Test
    void usesForwardedIpForAnonymousRequestKey() {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(new RateLimitBucketConfig(10, 10, 1),
                new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");

        assertThat(interceptor.resolveRateLimitKey(request)).isEqualTo("ip:203.0.113.10");
    }
}

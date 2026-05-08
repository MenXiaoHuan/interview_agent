package com.multimodal.interview.config;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 客户端配置。
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 注册 {@link RestTemplate} Bean。
     *
     * @return RestTemplate 实例
     */
    @Operation(summary = "RestTemplate 配置")
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

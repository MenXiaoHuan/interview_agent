package com.multimodal.interview.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.common.filter.JwtAuthenticationFilter;
import com.multimodal.interview.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 相关 Bean 配置。
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret:your-secret-key}")
    private String secretKey;

    /**
     * 注册 JWT 认证过滤器。
     *
     * @param objectMapper JSON 处理对象
     * @param userMapper 用户数据访问对象
     * @return JWT 认证过滤器
     */
    @Operation(summary = "JWT 认证过滤器")
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(ObjectMapper objectMapper, UserMapper userMapper) {
        return new JwtAuthenticationFilter(secretKey, objectMapper, userMapper);
    }
}

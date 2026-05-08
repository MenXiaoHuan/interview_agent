package com.multimodal.interview.config;

import com.multimodal.interview.common.interceptor.RateLimitInterceptor;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    /**
     * 创建 MVC 配置。
     *
     * @param rateLimitInterceptor 限流拦截器
     */
    public WebConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    /**
     * 注册全局拦截器。
     *
     * @param registry 拦截器注册器
     */
    @Operation(summary = "添加拦截器")
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
    }
}

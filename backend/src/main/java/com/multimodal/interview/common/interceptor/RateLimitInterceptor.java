package com.multimodal.interview.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.common.result.ResultCode;
import com.multimodal.interview.config.RateLimitBucketConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于 Bucket4j 的请求限流拦截器。
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitBucketConfig bucketConfig;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * 创建限流拦截器。
     *
     * @param bucketConfig 令牌桶配置，用于按限流键创建独立令牌桶
     * @param objectMapper JSON 序列化对象，用于处理响应数据
     */
    public RateLimitInterceptor(RateLimitBucketConfig bucketConfig, ObjectMapper objectMapper) {
        this.bucketConfig = bucketConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * 在请求进入控制器前执行限流检查。
     *
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler 请求处理方法
     * @return 获取到令牌时返回 {@code true}，否则返回 {@code false}
     * @throws Exception 响应写出异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String key = resolveRateLimitKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> bucketConfig.createNewBucket());
        if (bucket.tryConsume(1)) {
            return true;
        }

        // 令牌不足时直接返回统一错误响应。
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.error(ResultCode.BAD_REQUEST, "请求过于频繁，请稍后再试")
        ));
        return false;
    }

    /**
     * 解析限流键：已认证用户按用户名限流，匿名请求按客户端 IP 限流。
     *
     * @param request 当前 HTTP 请求
     * @return 限流键
     */
    public String resolveRateLimitKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getName() != null) {
            return "user:" + authentication.getName();
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return "ip:" + forwardedFor.split(",")[0].trim();
        }
        return "ip:" + request.getRemoteAddr();
    }
}

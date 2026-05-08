package com.multimodal.interview.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.common.result.ResultCode;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 基于 Bucket4j 的请求限流拦截器。
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Bucket bucket;
    private final ObjectMapper objectMapper;

    /**
     * 创建限流拦截器。
     *
     * @param bucket 令牌桶对象，用于限流控制
     * @param objectMapper JSON 序列化对象，用于处理响应数据
     */
    public RateLimitInterceptor(Bucket bucket, ObjectMapper objectMapper) {
        this.bucket = bucket;
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
}

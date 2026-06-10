package com.multimodal.interview.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 在 DeepSeek 请求发出前显式关闭 thinking 模式。
 */
@Component
public class DeepSeekThinkingModeRequestInterceptor implements ClientHttpRequestInterceptor {

    private final DeepSeekThinkingModeRequestCustomizer customizer;

    public DeepSeekThinkingModeRequestInterceptor(DeepSeekThinkingModeRequestCustomizer customizer) {
        this.customizer = customizer;
    }

    @Override
    public ClientHttpResponse intercept(
            org.springframework.http.HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        if (!shouldCustomize(request.getHeaders(), body)) {
            return execution.execute(request, body);
        }
        return execution.execute(request, customizer.customize(body));
    }

    private boolean shouldCustomize(HttpHeaders headers, byte[] body) {
        if (body == null || body.length == 0) {
            return false;
        }
        MediaType contentType = headers.getContentType();
        return contentType == null || MediaType.APPLICATION_JSON.includes(contentType);
    }
}

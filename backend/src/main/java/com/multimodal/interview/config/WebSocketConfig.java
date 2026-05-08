package com.multimodal.interview.config;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

/**
 * WebSocket 客户端配置。
 */
@Configuration
public class WebSocketConfig {

    /**
     * 注册 WebSocket 客户端。
     *
     * @return WebSocket 客户端实例
     */
    @Operation(summary = "WebSocket 客户端")
    @Bean
    public WebSocketClient webSocketClient() {
        return new StandardWebSocketClient();
    }
}

package com.multimodal.interview.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 为 DeepSeek 请求显式关闭 thinking 模式。
 */
@Component
public class DeepSeekThinkingModeRequestCustomizer {

    private final ObjectMapper objectMapper;

    public DeepSeekThinkingModeRequestCustomizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 向请求体注入 extra_body.thinking.disabled。
     *
     * @param requestBody 原始请求体
     * @return 注入后的请求体
     */
    public byte[] customize(byte[] requestBody) {
        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(requestBody);
            ObjectNode extraBody = resolveExtraBody(root);
            ObjectNode thinking = extraBody.putObject("thinking");
            thinking.put("type", "disabled");
            return objectMapper.writeValueAsBytes(root);
        } catch (IOException | ClassCastException exception) {
            throw new IllegalStateException("DeepSeek 请求体改写失败", exception);
        }
    }

    private ObjectNode resolveExtraBody(ObjectNode root) {
        JsonNode extraBody = root.get("extra_body");
        if (extraBody instanceof ObjectNode objectNode) {
            return objectNode;
        }
        root.remove("extra_body");
        return root.putObject("extra_body");
    }
}

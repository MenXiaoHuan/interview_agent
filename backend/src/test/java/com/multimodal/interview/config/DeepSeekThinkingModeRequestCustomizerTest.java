package com.multimodal.interview.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DeepSeekThinkingModeRequestCustomizerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldInjectDisabledThinkingIntoChatCompletionRequest() throws Exception {
        DeepSeekThinkingModeRequestCustomizer customizer = new DeepSeekThinkingModeRequestCustomizer(objectMapper);

        byte[] customized = customizer.customize("""
                {
                  "model": "deepseek-v4-flash",
                  "temperature": 0.5,
                  "messages": [
                    {
                      "role": "user",
                      "content": "你好"
                    }
                  ]
                }
                """.getBytes(StandardCharsets.UTF_8));

        JsonNode json = objectMapper.readTree(customized);
        assertEquals("deepseek-v4-flash", json.path("model").asText());
        assertEquals(0.5d, json.path("temperature").asDouble());
        assertNotNull(json.get("extra_body"));
        assertEquals("disabled", json.path("extra_body").path("thinking").path("type").asText());
        assertFalse(json.has("thinking"));
    }

    @Test
    void shouldMergeIntoExistingExtraBodyWithoutDroppingFields() throws Exception {
        DeepSeekThinkingModeRequestCustomizer customizer = new DeepSeekThinkingModeRequestCustomizer(objectMapper);

        byte[] customized = customizer.customize("""
                {
                  "model": "deepseek-v4-flash",
                  "extra_body": {
                    "foo": "bar"
                  }
                }
                """.getBytes(StandardCharsets.UTF_8));

        JsonNode json = objectMapper.readTree(customized);
        assertEquals("bar", json.path("extra_body").path("foo").asText());
        assertEquals("disabled", json.path("extra_body").path("thinking").path("type").asText());
    }
}

package com.multimodal.interview.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.http.client.ClientHttpRequestExecution;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeepSeekThinkingModeRequestInterceptorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldRewriteOutgoingRequestBodyWithDisabledThinking() throws Exception {
        DeepSeekThinkingModeRequestCustomizer customizer = new DeepSeekThinkingModeRequestCustomizer(objectMapper);
        DeepSeekThinkingModeRequestInterceptor interceptor = new DeepSeekThinkingModeRequestInterceptor(customizer);

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(), any())).thenReturn(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, URI.create("https://api.deepseek.com/chat/completions"));
        request.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        interceptor.intercept(request, """
                {
                  "model": "deepseek-v4-flash",
                  "messages": [
                    {
                      "role": "user",
                      "content": "你好"
                    }
                  ]
                }
                """.getBytes(StandardCharsets.UTF_8), execution);

        ArgumentCaptor<byte[]> bodyCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(execution).execute(any(), bodyCaptor.capture());

        JsonNode json = objectMapper.readTree(bodyCaptor.getValue());
        assertEquals("disabled", json.path("extra_body").path("thinking").path("type").asText());
    }
}

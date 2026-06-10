package com.multimodal.interview.service.impl;

import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.service.XunfeiUltraAgentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 讯飞大模型服务实现。
 */
@Service
@Slf4j
public class XunfeiUltraAgentServiceImpl implements XunfeiUltraAgentService {

    @Value("${xunfei.Ultra.host-url}")
    private String url;
    @Value("${xunfei.Ultra.authorization}")
    private String authorization;

    private static final String MODEL_VERSION = "4.0Ultra";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAgentAnswer(String systemContent, String userContent) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", authorization);

            JSONObject requestBody = buildRequestBody(systemContent, userContent);
            httpPost.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

            String result = httpClient.execute(httpPost, response -> {
                HttpEntity responseEntity = response.getEntity();
                return EntityUtils.toString(responseEntity);
            });

            return extractAnswerFromResponse(result);
        } catch (IOException e) {
            log.error("调用讯飞API失败", e);
            throw BusinessException.serviceUnavailable("AI 服务当前不可用");
        }
    }

    private JSONObject buildRequestBody(String systemContent, String userContent) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL_VERSION);
        requestBody.put("stream", false);

        JSONArray messages = new JSONArray();
        messages.put(createMessage("system", systemContent));
        messages.put(createMessage("user", userContent));

        requestBody.put("messages", messages);
        return requestBody;
    }

    private JSONObject createMessage(String role, String content) {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String extractAnswerFromResponse(String result) {
        if (result == null || result.trim().isEmpty()) {
            log.warn("响应内容为空，无法解析");
            throw BusinessException.serviceUnavailable("AI 服务当前不可用");
        }

        try {
            JSONObject jsonResult = new JSONObject(result);

            // 检查 code 状态码
            int code = jsonResult.optInt("code", -1);
            if (code != 0) {
                String errorMsg = jsonResult.optString("message", "未知错误");
                log.error("API 响应错误: code={}, message={}", code, errorMsg);
                throw BusinessException.serviceUnavailable("AI 服务当前不可用");
            }

            // 安全地获取 choices 数组
            JSONArray choices = jsonResult.optJSONArray("choices");
            if (choices == null || choices.length() == 0) {
                log.error("响应中缺少 choices 数组或数组为空: {}", result);
                throw BusinessException.serviceUnavailable("AI 服务当前不可用");
            }

            // 获取第一个 choice 对象
            JSONObject firstChoice = choices.optJSONObject(0);
            if (firstChoice == null) {
                log.error("choices 数组的第一个元素格式错误: {}", result);
                throw BusinessException.serviceUnavailable("AI 服务当前不可用");
            }

            // 获取 message 对象
            JSONObject message = firstChoice.optJSONObject("message");
            if (message == null) {
                log.error("choice 对象中缺少 message 字段: {}", result);
                throw BusinessException.serviceUnavailable("AI 服务当前不可用");
            }

            // 获取 content 字段（使用 optString 避免异常）
            String content = message.optString("content", "");
            if (content == null || content.isBlank()) {
                log.error("message.content 为空: {}", result);
                throw BusinessException.serviceUnavailable("AI 服务当前不可用");
            }
            return content;

        } catch (Exception e) {
            log.error("解析响应内容时发生异常: {}", result, e);
            throw BusinessException.serviceUnavailable("AI 服务当前不可用");
        }
    }
}

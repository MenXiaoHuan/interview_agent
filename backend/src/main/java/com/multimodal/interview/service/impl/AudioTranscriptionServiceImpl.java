package com.multimodal.interview.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.service.AudioTranscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 语音转写服务实现。
 */
@Slf4j
@Service
public class AudioTranscriptionServiceImpl implements AudioTranscriptionService {

    @Value("${xunfei.ost.appId}")
    private String appId;

    @Value("${xunfei.ost.apiKey}")
    private String apiKey;

    @Value("${xunfei.ost.apiSecret}")
    private String apiSecret;

    @Value("${xunfei.ost.upload-url}")
    private String UPLOAD_URL;

    @Value("${xunfei.ost.create-task-url}")
    private String CREATE_TASK_URL;

    @Value("${xunfei.ost.query-task-url}")
    private String QUERY_TASK_URL;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AudioTranscriptionServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String uploadAudio(MultipartFile file) throws Exception {
        log.debug("Starting audio file upload");
        String requestId = UUID.randomUUID().toString().replace("-", "");
        String date = getGMTDate();

        // 计算文件内容digest
        byte[] fileBytes = file.getBytes();
        String digest = calculateDigest(fileBytes);

        HttpHeaders headers = createHeaders("upload-ost-api.xfyun.cn", date, digest, "/file/upload");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("app_id", appId);
        body.add("request_id", requestId);
        body.add("data", file.getResource());

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                UPLOAD_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        log.debug("Audio file upload completed with status: {}", response.getStatusCode());
        return response.getBody();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createTask(String audioUrl) throws Exception {
        log.debug("Creating transcription task for audio URL: {}", audioUrl);
        String date = getGMTDate();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("common", Map.of("app_id", appId));
        requestBody.put("business", Map.of(
                "request_id", UUID.randomUUID().toString(),
                "language", "zh_cn",
                "domain", "pro_ost_ed",
                "accent", "mandarin"
        ));
        requestBody.put("data", Map.of(
                "audio_url", audioUrl,
                "audio_src", "http",
                "format", "audio/L16;rate=16000",
                "encoding", "raw"
        ));

        // 计算JSON请求体digest
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        String digest = calculateDigest(jsonBody.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = createHeaders("ost-api.xfyun.cn", date, digest, "/v2/ost/pro_create");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                CREATE_TASK_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        return response.getBody();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String queryTask(String taskId) throws Exception {
        log.debug("Querying task status for taskId: {}", taskId);
        String date = getGMTDate();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("common", Map.of("app_id", appId));
        requestBody.put("business", Map.of("task_id", taskId));

        // 计算JSON请求体digest
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        String digest = calculateDigest(jsonBody.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = createHeaders("ost-api.xfyun.cn", date, digest, "/v2/ost/query");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                QUERY_TASK_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        log.debug("Task query completed with status: {}", response.getStatusCode());
        return response.getBody();
    }

    private HttpHeaders createHeaders(String host, String date, String digest, String path) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Host", host);
        headers.set("Date", date);
        headers.set("Digest", digest);
        headers.set("Authorization", generateAuthorization(date, "POST " + path + " HTTP/1.1", digest, host));
        return headers;
    }

    private String getGMTDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }

    private String generateAuthorization(String date, String requestLine, String digest, String host) throws Exception {
        String signatureOrigin = String.format("host: %s\ndate: %s\n%s\ndigest: %s",
                host, date, requestLine, digest);

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signatureSha = mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signatureSha);

        return String.format("api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line digest\", signature=\"%s\"",
                apiKey, signature);
    }

    // 新增digest计算方法
    private String calculateDigest(byte[] content) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digestBytes = md.digest(content);
        String digestBase64 = Base64.getEncoder().encodeToString(digestBytes);
        return "SHA-256=" + digestBase64;
    }
} 

package com.multimodal.interview.service.impl;

import com.google.gson.Gson;
import com.multimodal.interview.entity.SpeechSynthesis;
import com.multimodal.interview.service.SpeechSynthesisService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 语音合成服务实现。
 */
@Slf4j
@Service
public class SpeechSynthesisServiceImpl implements SpeechSynthesisService {
    private static final Gson gson = new Gson();

    @Value("${xunfei.appid}")
    private String appId;

    @Value("${xunfei.api-key}")
    private String apiKey;

    @Value("${xunfei.api-secret}")
    private String apiSecret;

    @Value("${xunfei.tts.host-url}")
    private String hostUrl;

    @Value("${xunfei.tts.tte}")
    private String defaultTte;

    @Value("${xunfei.tts.vcn}")
    private String defaultVcn;

    @Value("${xunfei.tts.pitch}")
    private Integer defaultPitch;

    @Value("${xunfei.tts.speed}")
    private Integer defaultSpeed;

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] synthesizeSpeech(SpeechSynthesis request) throws Exception {
        log.info("开始语音合成，文本内容: {}", request.getText());

        String wsUrl = getAuthUrl(hostUrl, apiKey, apiSecret).replace("https://", "wss://");
        log.debug("WebSocket URL: {}", wsUrl);

        // 设置默认值
        if (request.getTte() == null) request.setTte(defaultTte);
        if (request.getVcn() == null) request.setVcn(defaultVcn);
        if (request.getPitch() == null) request.setPitch(defaultPitch);
        if (request.getSpeed() == null) request.setSpeed(defaultSpeed);

        // 使用ByteArrayOutputStream来收集音频数据
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            CountDownLatch completionLatch = new CountDownLatch(1);
            websocketWork(wsUrl, outputStream, request, completionLatch);

            // 等待WebSocket完成或超时
            boolean completed = completionLatch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                log.error("语音合成超时");
                throw new RuntimeException("语音合成超时");
            }

            byte[] audioData = outputStream.toByteArray();
            if (audioData.length == 0) {
                log.error("生成的音频数据为空");
                throw new RuntimeException("生成的音频数据为空");
            }

            log.info("语音合成完成，音频数据大小: {} bytes", audioData.length);
            return audioData;
        }
    }

    private void websocketWork(String wsUrl, OutputStream outputStream, SpeechSynthesis request, CountDownLatch completionLatch) {
        try {
            URI uri = new URI(wsUrl);
            WebSocketClient webSocketClient = new WebSocketClient(uri) {
    /**
     * {@inheritDoc}
     */
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    log.info("WebSocket连接已建立");
                }

    /**
     * {@inheritDoc}
     */
                @Override
                public void onMessage(String text) {
                    log.debug("收到WebSocket消息: {}", text);
                    JsonParse myJsonParse = gson.fromJson(text, JsonParse.class);
                    if (myJsonParse.code != 0) {
                        log.error("语音合成错误，错误码：{}，会话ID：{}", myJsonParse.code, myJsonParse.sid);
                        completionLatch.countDown();
                        return;
                    }

                    if (myJsonParse.data != null) {
                        try {
                            byte[] textBase64Decode = Base64.getDecoder().decode(myJsonParse.data.audio);
                            outputStream.write(textBase64Decode);
                            outputStream.flush();

                            if (myJsonParse.data.status == 2) {
                                log.info("语音合成完成");
                                completionLatch.countDown();
                            }
                        } catch (Exception e) {
                            log.error("处理音频数据时发生错误", e);
                            completionLatch.countDown();
                        }
                    }
                }

    /**
     * {@inheritDoc}
     */
                @Override
                public void onClose(int i, String s, boolean b) {
                    log.info("WebSocket连接已关闭: code={}, reason={}, remote={}", i, s, b);
                    completionLatch.countDown();
                }

    /**
     * {@inheritDoc}
     */
                @Override
                public void onError(Exception e) {
                    log.error("WebSocket发生错误", e);
                    completionLatch.countDown();
                }
            };

            webSocketClient.connect();
            while (!webSocketClient.isOpen()) {
                Thread.sleep(100);
            }

            String requestJson = String.format("{\n" +
                            "  \"common\": {\n" +
                            "    \"app_id\": \"%s\"\n" +
                            "  },\n" +
                            "  \"business\": {\n" +
                            "    \"aue\": \"raw\",\n" +
                            "    \"tte\": \"%s\",\n" +
                            "    \"ent\": \"intp65\",\n" +
                            "    \"vcn\": \"%s\",\n" +
                            "    \"pitch\": %d,\n" +
                            "    \"speed\": %d\n" +
                            "  },\n" +
                            "  \"data\": {\n" +
                            "    \"status\": 2,\n" +
                            "    \"text\": \"%s\"\n" +
                            "  }\n" +
                            "}", appId, request.getTte(), request.getVcn(),
                    request.getPitch(), request.getSpeed(),
                    Base64.getEncoder().encodeToString(request.getText().getBytes(StandardCharsets.UTF_8)));

            log.debug("发送WebSocket请求: {}", requestJson);
            webSocketClient.send(requestJson);

        } catch (Exception e) {
            log.error("WebSocket工作过程中发生错误", e);
            completionLatch.countDown();
        }
    }

    private String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URI uri = new URI(hostUrl);
        URL url = uri.toURL();
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());

        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";

        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        String sha = Base64.getEncoder().encodeToString(hexDigits);

        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                apiKey, "hmac-sha256", "host date request-line", sha);

        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder()
                .addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8)))
                .addQueryParameter("date", date)
                .addQueryParameter("host", url.getHost())
                .build();

        return httpUrl.toString();
    }

    private static class JsonParse {
        int code;
        String sid;
        Data data;
    }

    private static class Data {
        int status;
        String audio;
    }
} 

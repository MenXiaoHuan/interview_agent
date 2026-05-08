package com.multimodal.interview.service.impl;

import com.multimodal.interview.service.FacialAnalysisService;
import com.multimodal.interview.util.ImageCompressorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * 面部表情分析服务实现。
 */
@Slf4j
@Service
public class FacialAnalysisServiceImpl implements FacialAnalysisService {

    @Value("${xunfei.tupapi.host-url}")
    private String url;

    @Value("${xunfei.tupapi.appid}")
    private String appid;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFacialAnalysis(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            String errorMsg = "上传文件为空";
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        //处理图片使其大小符合要求
        log.info("上传图片原始大小: {} KB", file.getSize() / 1024);
        file = ImageCompressorUtil.compressImage(file);
        log.info("压缩后图片大小: {} KB", file.getSize() / 1024);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            long timestamp = Instant.now().getEpochSecond();
            String xParam = generateXParam();
            String xCheckSum = generateXCheckSum(appid, String.valueOf(timestamp), xParam);

            HttpPost httpPost = buildHttpPost(url, appid, timestamp, xParam, xCheckSum, file.getBytes());

            String result = executeRequest(httpClient, httpPost);

            return parseEmotionLabel(result);

        } catch (IOException | NoSuchAlgorithmException e) {
            String errorMsg = "面部分析失败";
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg + ": " + e.getMessage(), e);
        }
    }

    private String generateXParam() {
        return "eyJpbWFnZV9uYW1lIjogImltZy5qcGciLCAiaW1hZ2VfdXJsIjogIiJ9";
    }

    private String generateXCheckSum(String appid, String timestamp, String xParam) throws NoSuchAlgorithmException {
        String rawString = appid + timestamp + xParam;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(rawString.getBytes(StandardCharsets.UTF_8));

        StringBuilder md5Hash = new StringBuilder();
        for (byte b : hashBytes) {
            md5Hash.append(String.format("%02x", b));
        }
        return md5Hash.toString();
    }

    private HttpPost buildHttpPost(String url, String appid, long timestamp,
                                   String xParam, String xCheckSum, byte[] fileBytes) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", ContentType.APPLICATION_OCTET_STREAM.getMimeType());
        httpPost.setHeader("X-Appid", appid);
        httpPost.setHeader("X-CurTime", String.valueOf(timestamp));
        httpPost.setHeader("X-Param", xParam);
        httpPost.setHeader("X-CheckSum", xCheckSum);
        httpPost.setEntity(new ByteArrayEntity(fileBytes, ContentType.APPLICATION_OCTET_STREAM));
        return httpPost;
    }

    private String executeRequest(CloseableHttpClient httpClient, HttpPost httpPost) throws IOException {
        return httpClient.execute(httpPost, response -> {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                String errorMsg = "请求失败，状态码: " + statusCode;
                log.error(errorMsg);
                throw new IOException(errorMsg);
            }
            HttpEntity responseEntity = response.getEntity();
            return EntityUtils.toString(responseEntity);
        });
    }

    // 增加对错误响应的兼容（服务端返回 code != 0 时）
    private String parseEmotionLabel(String jsonResult) {
        try {
            JSONObject response = new JSONObject(jsonResult);

            // 先判断业务码是否正常
            if (response.has("code") && response.getInt("code") != 0) {
                String errorDesc = response.optString("desc", "无描述");
                throw new RuntimeException("接口返回错误码: " + response.getInt("code") + ", 描述: " + errorDesc);
            }

            if (!response.has("data")) {
                throw new IllegalArgumentException("响应缺少 data 字段: " + jsonResult);
            }
            JSONObject data = response.getJSONObject("data");

            if (!data.has("fileList") || data.getJSONArray("fileList").isEmpty()) {
                throw new IllegalArgumentException("fileList 为空: " + jsonResult);
            }
            JSONObject fileObj = data.getJSONArray("fileList").getJSONObject(0);

            if (!fileObj.has("label")) {
                throw new IllegalArgumentException("文件信息缺少 label 字段: " + jsonResult);
            }
            int label = fileObj.getInt("label");

            return switch (label) {
                case 0 -> "其他(非人脸表情图片)";
                case 1 -> "其他表情";
                case 2 -> "喜悦";
                case 3 -> "愤怒";
                case 4 -> "悲伤";
                case 5 -> "惊恐";
                case 6 -> "厌恶";
                case 7 -> "中性";
                default -> "未知表情（label: " + label + "）";
            };

        } catch (Exception e) {
            String errorMsg = "解析响应失败: " + jsonResult;
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
}

package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.entity.SpeechSynthesis;
import com.multimodal.interview.service.SpeechSynthesisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

/**
 * 语音合成接口控制器。
 */
@Slf4j
@Tag(name = "语音合成", description = "语音合成相关接口")
@RestController
@RequestMapping("/api/speech")
@RequiredArgsConstructor
public class SpeechSynthesisController {
    private final SpeechSynthesisService speechSynthesisService;
    /**
     * 文本转语音。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "文本转语音",
            description = "将文本内容转换为语音文件")
    @PostMapping("/synthesize")
    public ApiResponse<String> synthesizeSpeech(@RequestBody SpeechSynthesis request){
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            return ApiResponse.badRequest("文本内容不能为空");
        }

        try {
            byte[] audioData = speechSynthesisService.synthesizeSpeech(request);

            if (audioData == null || audioData.length == 0) {
                return ApiResponse.internalError("语音合成失败");
            }

            // 将二进制数据转换为Base64编码
            String base64Audio = Base64.getEncoder().encodeToString(audioData);

            // 返回包含Base64编码音频的ApiResponse
            return ApiResponse.success(base64Audio);
        } catch (Exception e) {
            log.error("语音合成异常", e);
            return ApiResponse.internalError("语音合成异常");
        }
    }
}
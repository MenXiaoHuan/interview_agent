package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * SpeechTranscriptionRequest 数据传输对象。
 */
@Data
public class SpeechTranscriptionRequest {
    @NotBlank(message = "音频数据不能为空")
    private String audioData;

    private String format = "audio/L16;rate=16000";
    private String language = "zh_cn";
    private String accent = "mandarin";
    private String domain = "iat";
    private String vadEos = "3000";
}

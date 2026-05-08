package com.multimodal.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AudioTranscriptionRequest 数据传输对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "音频转写请求")
public class AudioTranscriptionRequest {

    /**
     * 音频URL
     */
    @NotBlank(message = "音频URL不能为空")
    @Schema(description = "音频URL，指向需要转写的音频文件", example = "https://example.com/audio.mp3", required = true)
    private String audioUrl;

    /**
     * 音频格式
     */
    @Schema(description = "音频格式，如mp3、wav等", example = "mp3")
    private String format;

    /**
     * 语言
     */
    @Schema(description = "音频语言，默认为中文", example = "zh-CN")
    private String language;
}

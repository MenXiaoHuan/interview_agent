package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ChatMessageSaveRequest 数据传输对象。
 */
@Data
public class ChatMessageSaveRequest {
    @NotBlank
    private String content;
}

package com.multimodal.interview.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ResetCodeResponse 数据传输对象。
 */
@Data
public class ResetCodeResponse {
    private String contact;
    private String channel;
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}

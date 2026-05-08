package com.multimodal.interview.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * FeedbackItemResponse 数据传输对象。
 */
@Data
public class FeedbackItemResponse {
    private Long id;
    private Long userId;
    private String nickname;
    private String type;
    private String title;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

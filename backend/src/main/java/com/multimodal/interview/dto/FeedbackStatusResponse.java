package com.multimodal.interview.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * FeedbackStatusResponse 数据传输对象。
 */
@Data
public class FeedbackStatusResponse {
    private Long id;
    private String title;
    private String status;
    private String reply;
    private LocalDateTime replyTime;
    private LocalDateTime updatedAt;
}

package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈实体。
 */
@Data
public class Feedback {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private String status;
    private String reply;
    private LocalDateTime replyTime;
    private Long adminId;
    private String adminName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

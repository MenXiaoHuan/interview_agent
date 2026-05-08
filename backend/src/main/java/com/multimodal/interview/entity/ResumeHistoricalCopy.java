package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历历史副本实体。
 */
@Data
public class ResumeHistoricalCopy {
    private String id;
    private String resumeHistoryId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

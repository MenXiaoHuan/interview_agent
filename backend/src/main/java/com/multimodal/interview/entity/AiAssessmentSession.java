package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AIview 测评流程实例实体。
 */
@Data
public class AiAssessmentSession {
    private Long id;
    private String assessmentSessionId;
    private Long userId;
    private Long jobId;
    private String status;
    private String currentStage;
    private String eliminationReason;
    private String finalSummary;
    private String finalSuggestion;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

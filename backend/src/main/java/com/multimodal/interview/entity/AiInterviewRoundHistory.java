package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 综合评估场景历史实体。
 */
@Data
public class AiInterviewRoundHistory {
    private Long id;
    private String assessmentSessionId;
    private String roundType;
    private Long userId;
    private Long jobId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalTime;
    private Integer overallScore;
    private String aiAssessment;
}

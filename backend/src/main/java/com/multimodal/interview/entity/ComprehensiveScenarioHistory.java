package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 综合评估场景历史实体。
 */
@Data
public class ComprehensiveScenarioHistory {
    private Long id;
    private Long userId;
    private Long jobId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalTime;
    private Integer overallScore;
    private Integer fluencyScore;
    private Integer emotionScore;
    private Integer relevanceScore;
    private Integer adaptabilityScore;
    private String aiAssessment;
}

package com.multimodal.interview.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ComprehensiveScenarioHistoryRequest 数据传输对象。
 */
@Data
public class ComprehensiveScenarioHistoryRequest {
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

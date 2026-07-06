package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 综合评估试题历史实体。
 */
@Data
public class AiQuestionHistory {
    private Long id;
    private String assessmentSessionId;
    private Long userId;
    private Long jobId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalTime;
    private Integer overallScore;
    private String aiSuggestions;
}

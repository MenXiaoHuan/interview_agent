package com.multimodal.interview.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ComprehensiveQuestionHistoryRequest 数据传输对象。
 */
@Data
public class ComprehensiveQuestionHistoryRequest {
    private Long userId;
    private Long jobId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalTime;
    private Integer overallScore;
    private String aiSuggestions;
}

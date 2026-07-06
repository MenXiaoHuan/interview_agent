package com.multimodal.interview.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AiQuestionHistoryRequest 数据传输对象。
 */
@Data
public class AiQuestionHistoryRequest {
    private String assessmentSessionId;
    private Long userId;
    private Long jobId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalTime;
    private Integer overallScore;
    private Integer passScore;
    private Map<String, Integer> passScores;
    private String aiSuggestions;
}

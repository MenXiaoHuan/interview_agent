package com.multimodal.interview.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AiInterviewRoundHistoryRequest 数据传输对象。
 */
@Data
public class AiInterviewRoundHistoryRequest {
    private String assessmentSessionId;
    private String roundType;
    private Long userId;
    private Long jobId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalTime;
    private Integer overallScore;
    private Integer passScore;
    private Map<String, Integer> passScores;
    private String aiAssessment;
}

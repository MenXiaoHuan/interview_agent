package com.multimodal.interview.dto;

import lombok.Data;

import java.util.Map;

/**
 * AiResumeHistoryRequest 数据传输对象。
 */
@Data
public class AiResumeHistoryRequest {
    private String assessmentSessionId;
    private Long userId;
    private Long jobId;
    private String fileName;
    private Integer fileSize;
    private String fileType;
    private Integer overallScore;
    private Integer passScore;
    private Map<String, Integer> passScores;
    private String aiAnalysis;
    private String aiSuggestions;
}

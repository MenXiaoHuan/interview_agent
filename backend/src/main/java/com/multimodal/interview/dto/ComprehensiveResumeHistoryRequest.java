package com.multimodal.interview.dto;

import lombok.Data;

/**
 * ComprehensiveResumeHistoryRequest 数据传输对象。
 */
@Data
public class ComprehensiveResumeHistoryRequest {
    private Long userId;
    private Long jobId;
    private String fileName;
    private Integer fileSize;
    private String fileType;
    private Integer overallScore;
    private Integer skillMatch;
    private Integer experienceMatch;
    private Integer educationMatch;
    private Integer communicationSkill;
    private Integer leadershipSkill;
    private String aiAnalysis;
    private String aiSuggestions;
}

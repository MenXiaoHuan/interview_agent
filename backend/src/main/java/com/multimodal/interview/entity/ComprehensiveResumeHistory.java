package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 综合评估简历历史实体。
 */
@Data
public class ComprehensiveResumeHistory {
    private Long id;
    private Long userId;
    private Long jobId;
    private String fileName;
    private Integer fileSize;
    private String fileType;
    private LocalDateTime uploadTime;
    private Integer overallScore;
    private Integer skillMatch;
    private Integer experienceMatch;
    private Integer educationMatch;
    private Integer communicationSkill;
    private Integer leadershipSkill;
    private String aiAnalysis;
    private String aiSuggestions;
}

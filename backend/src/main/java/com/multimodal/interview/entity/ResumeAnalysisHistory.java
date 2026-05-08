package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历分析历史实体。
 */
@Data
public class ResumeAnalysisHistory {
    private String id;
    private Long userId;
    private Long jobId;
    private String resumeContent;
    private Integer score;
    private String advantages;
    private String disadvantages;
    private String improvementSuggestions;
    private Integer professionalSkills;
    private Integer projectExecution;
    private Integer innovation;
    private Integer communication;
    private Integer adaptability;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String evaluationType;
}

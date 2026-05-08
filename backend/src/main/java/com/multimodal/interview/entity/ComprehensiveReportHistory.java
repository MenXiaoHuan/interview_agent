package com.multimodal.interview.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 综合评估报告历史实体。
 */
@Data
public class ComprehensiveReportHistory {
    private Long id;
    private Long userId;
    private Long jobId;
    private BigDecimal overallScore;
    private BigDecimal resumeScore;
    private BigDecimal questionScore;
    private BigDecimal scenarioScore;
    private String strengthAnalysis;
    private String improvementAnalysis;
    private String learningRoute;
    private LocalDateTime createTime;
    private User user;
    private Job job;
}

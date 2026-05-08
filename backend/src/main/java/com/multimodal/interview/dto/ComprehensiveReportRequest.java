package com.multimodal.interview.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ComprehensiveReportRequest 数据传输对象。
 */
@Data
public class ComprehensiveReportRequest {
    private Long userId;
    private Long jobId;
    private BigDecimal overallScore;
    private BigDecimal resumeScore;
    private BigDecimal questionScore;
    private BigDecimal scenarioScore;
    private String strengthAnalysis;
    private String improvementAnalysis;
    private String learningRoute;
}

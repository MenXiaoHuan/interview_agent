package com.multimodal.interview.dto;

import com.multimodal.interview.entity.Job;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ComprehensiveReportResponse 数据传输对象。
 */
@Data
public class ComprehensiveReportResponse {
    private Long id;
    private Long userId;
    private Long jobId;
    private String jobTitle;
    private BigDecimal overallScore;
    private BigDecimal resumeScore;
    private BigDecimal questionScore;
    private BigDecimal scenarioScore;
    private String strengthAnalysis;
    private String improvementAnalysis;
    private String learningRoute;
    private LocalDateTime createTime;
    private Job job;
}

package com.multimodal.interview.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ComprehensiveReportSummaryResponse 数据传输对象。
 */
@Data
public class ComprehensiveReportSummaryResponse {
    private Long id;
    private Long userId;
    private Long jobId;
    private String jobName;
    private LocalDateTime createTime;
    private String strengthAnalysis;
    private String improvementAnalysis;
    private String learningRoute;
}

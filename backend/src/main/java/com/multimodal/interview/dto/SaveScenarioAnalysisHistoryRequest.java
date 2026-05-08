package com.multimodal.interview.dto;

import lombok.Data;

import lombok.Data;

/**
 * SaveScenarioAnalysisHistoryRequest 数据传输对象。
 */
@Data
public class SaveScenarioAnalysisHistoryRequest {
    private String userId;
    private Integer totalScore;
    private Integer score1;
    private String suggestion1;
    private Integer score2;
    private String suggestion2;
    private Integer score3;
    private String suggestion3;
    private Integer score4;
    private String suggestion4;
    private Integer score5;
    private String suggestion5;
}

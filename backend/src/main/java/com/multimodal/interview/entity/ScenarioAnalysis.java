package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 场景分析历史实体。
 */
@Data
public class ScenarioAnalysis {
    private Long id;
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
    private String evaluationType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

package com.multimodal.interview.dto;

import lombok.Data;

/**
 * SaveResumeHistoryRequest 数据传输对象。
 */
@Data
public class SaveResumeHistoryRequest {
    private String id;
    private Long userId;
    private Long jobId;
    private String resumeContent;
    private Integer score;
    private FeedbackDTO feedback;
    private CompetenciesScoreDTO competenciesScore;

    /**
     * 反馈信息。
     */
    @Data
    public static class FeedbackDTO {
        private String advantages;
        private String disadvantages;
        private String improvementSuggestions;
    }

    /**
     * 能力评分信息。
     */
    @Data
    public static class CompetenciesScoreDTO {
        private Integer professionalSkills;
        private Integer projectExecution;
        private Integer innovation;
        private Integer communication;
        private Integer adaptability;
    }
}

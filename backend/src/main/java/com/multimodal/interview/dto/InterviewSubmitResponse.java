package com.multimodal.interview.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * InterviewSubmitResponse 数据传输对象。
 */
@Data
public class InterviewSubmitResponse {
    private Long interviewId;
    private Integer totalScore;
    private Integer correctCount;
    private BigDecimal correctRate;
    private List<QuestionResult> questions;

    /**
     * 题目结果项。
     */
    @Data
    public static class QuestionResult {
        private Long questionId;
        private String question;
        private String userAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        private Integer score;
    }
}

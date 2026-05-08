package com.multimodal.interview.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * InterviewResultResponse 数据传输对象。
 */
@Data
public class InterviewResultResponse {
    private Integer totalScore = 0;
    private Integer correctCount = 0;
    private BigDecimal correctRate = BigDecimal.ZERO;
    private List<QuestionResult> questions = new ArrayList<>();

    /**
     * 题目结果项。
     */
    @Data
    public static class QuestionResult {
        private Long id;
        private String question = "";
        private List<String> options = new ArrayList<>();
        private String correctAnswer = "";
        private String userAnswer = "";
        private String explanation = "";

        public QuestionResult() {
            this.question = "";
            this.options = new ArrayList<>();
            this.correctAnswer = "";
            this.userAnswer = "";
            this.explanation = "";
        }
    }
}

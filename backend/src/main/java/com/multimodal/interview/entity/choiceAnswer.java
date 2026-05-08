package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选择题答案实体。
 */
@Data
public class choiceAnswer {
    private Long id;
    private Long recordId;
    private Long questionId;
    private String userAnswer;
    private Boolean isCorrect;
    private Integer score;
    private Integer answerTime;
    private LocalDateTime createdAt;
}

package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选择题实体。
 */
@Data
public class choiceQuestion {
    private Long id;
    private Long jobId;
    private String question;
    private String options;
    private String correctAnswer;
    private String explanation;
    private Integer difficulty;
    private Integer score;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.multimodal.interview.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 选择题作答记录实体。
 */
@Data
public class choiceQuestionRecord {
    private Long id;
    private Long userId;
    private Long jobId;
    private Integer totalScore;
    private Integer correctCount;
    private BigDecimal correctRate;
    private Integer duration;
    private Integer status;
    private String evaluationType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

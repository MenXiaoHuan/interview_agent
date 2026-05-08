package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 场景问题实体。
 */
@Data
public class ScenarioQuestion {
    private Long id;
    private Long jobId;
    private String question;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

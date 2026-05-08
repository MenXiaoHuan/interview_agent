package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位实体。
 */
@Data
public class Job {
    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private String requirements;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private JobCategory category;
}

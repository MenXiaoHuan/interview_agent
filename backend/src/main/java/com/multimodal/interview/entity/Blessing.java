package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 祝福语实体。
 */
@Data
public class Blessing {
    private Long id;
    private Long userId;
    private String content;
    private String type;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

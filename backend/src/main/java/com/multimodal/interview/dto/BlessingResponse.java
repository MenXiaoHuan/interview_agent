package com.multimodal.interview.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * BlessingResponse 数据传输对象。
 */
@Data
public class BlessingResponse {
    private Long id;
    private String content;
    private String type;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

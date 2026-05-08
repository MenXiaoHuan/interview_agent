package com.multimodal.interview.dto;

import lombok.Data;

/**
 * ResumeUpdateRequest 数据传输对象。
 */
@Data
public class ResumeUpdateRequest {
    private String content;
    private Long userId;
    private Long jobId;
}

package com.multimodal.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ResumeContentResponse 数据传输对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeContentResponse {
    private String resumeId;
    private String content;
}

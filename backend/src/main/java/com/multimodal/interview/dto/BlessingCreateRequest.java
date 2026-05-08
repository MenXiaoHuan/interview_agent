package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * BlessingCreateRequest 数据传输对象。
 */
@Data
public class BlessingCreateRequest {
    @NotBlank
    private String content;
    private String type;
}

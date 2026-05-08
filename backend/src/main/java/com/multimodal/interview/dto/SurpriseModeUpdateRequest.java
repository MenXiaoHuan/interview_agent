package com.multimodal.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * SurpriseModeUpdateRequest 数据传输对象。
 */
@Data
public class SurpriseModeUpdateRequest {
    @NotNull
    @Min(0)
    @Max(1)
    private Integer surpriseMode;
}

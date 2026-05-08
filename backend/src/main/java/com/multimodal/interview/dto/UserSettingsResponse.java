package com.multimodal.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * UserSettingsResponse 数据传输对象。
 */
@Data
public class UserSettingsResponse {
    @NotNull(message = "护眼模式设置不能为空")
    @Min(value = 0, message = "护眼模式值必须为0或1")
    @Max(value = 1, message = "护眼模式值必须为0或1")
    private Integer eyeCareMode;
}

package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * GenderUpdateRequest 数据传输对象。
 */
@Data
public class GenderUpdateRequest {
    @NotNull(message = "用户性别不能为空")
    private Integer gender;
}

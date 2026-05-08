package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * StatusUpdateRequest 数据传输对象。
 */
@Data
public class StatusUpdateRequest {

    @NotNull(message = "用户状态不能为空")
    private Integer status;
}

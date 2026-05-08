package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * UserTypeUpdateRequest 数据传输对象。
 */
@Data
public class UserTypeUpdateRequest {
    @NotNull(message = "用户类型不能为空")
    private Integer userType;
}

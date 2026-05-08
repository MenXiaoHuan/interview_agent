package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * PhoneBindRequest 数据传输对象。
 */
@Data
public class PhoneBindRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;
}

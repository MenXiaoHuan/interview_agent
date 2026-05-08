package com.multimodal.interview.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * ForgotSendCodeRequest 数据传输对象。
 */
@Data
public class ForgotSendCodeRequest {
    @Email(message = "邮箱格式不正确")
    private String email;
    @Pattern(regexp = "^\\+?[0-9]{6,20}$", message = "手机号格式不正确")
    private String phone;
}

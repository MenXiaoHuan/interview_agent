package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * NicknameUpdateRequest 数据传输对象。
 */
@Data
public class NicknameUpdateRequest {
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度必须在2-20个字符之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$", message = "昵称只能包含中文、英文、数字和下划线")
    private String nickname;
}

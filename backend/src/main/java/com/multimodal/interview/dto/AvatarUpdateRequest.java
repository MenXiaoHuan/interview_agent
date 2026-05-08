package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AvatarUpdateRequest 数据传输对象。
 */
@Data
public class AvatarUpdateRequest {
    @NotBlank(message = "头像URL不能为空")
    private String avatarUrl;
}

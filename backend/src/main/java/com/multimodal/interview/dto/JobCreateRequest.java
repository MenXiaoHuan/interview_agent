package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * JobCreateRequest 数据传输对象。
 */
@Data
public class JobCreateRequest {
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "岗位名称不能为空")
    @Size(min = 1, max = 100, message = "岗位名称长度必须在1-100个字符之间")
    private String name;

    @NotBlank(message = "岗位描述不能为空")
    private String description;

    @NotBlank(message = "岗位要求不能为空")
    private String requirements;
}

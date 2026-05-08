package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * JobCategoryCreateRequest 数据传输对象。
 */
@Data
public class JobCategoryCreateRequest {
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 1, max = 50, message = "分类名称长度必须在1-50个字符之间")
    private String name;

    @NotNull(message = "分类层级不能为空")
    private Integer level;

    private Long parentId;

    private Integer sortOrder = 0;
}

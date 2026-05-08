package com.multimodal.interview.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位分类实体。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobCategory {
    private Long id;
    private String name;
    private Integer level;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<JobCategory> children;
}

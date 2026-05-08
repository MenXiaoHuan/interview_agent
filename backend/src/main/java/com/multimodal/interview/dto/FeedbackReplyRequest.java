package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * FeedbackReplyRequest 数据传输对象。
 */
@Data
public class FeedbackReplyRequest {
    @NotNull(message = "反馈ID不能为空")
    private Long id;

    @NotBlank(message = "回复内容不能为空")
    private String reply;

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(PENDING|RESOLVED)$", message = "状态必须是：处理中/已完成")
    private String status;
}

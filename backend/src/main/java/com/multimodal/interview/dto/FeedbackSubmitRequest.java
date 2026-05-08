package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * FeedbackSubmitRequest 数据传输对象。
 */
@Data
public class FeedbackSubmitRequest {
    @NotBlank(message = "反馈类型不能为空")
    @Pattern(regexp = "^(bug|suggestion|experience|other)$", message = "反馈类型必须是：bug/suggestion/experience/other")
    private String type;

    @NotBlank(message = "反馈标题不能为空")
    @Size(max = 100, message = "反馈标题最大长度为100")
    private String title;

    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 500, message = "反馈内容最大长度为500")
    private String content;
}

package com.multimodal.interview.dto;

import lombok.Data;

/**
 * SubmittedAnswerRequest 数据传输对象。
 */
@Data
public class SubmittedAnswerRequest {
    private Long questionId;
    private String answer;
}

package com.multimodal.interview.dto;

import lombok.Data;

import java.util.List;

/**
 * InterviewSubmitRequest 数据传输对象。
 */
@Data
public class InterviewSubmitRequest {
    private Long jobId;
    private List<SubmittedAnswerRequest> answers;
}

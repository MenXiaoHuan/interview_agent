package com.multimodal.interview.dto;

import lombok.Data;

/**
 * BlessingUpdateRequest 数据传输对象。
 */
@Data
public class BlessingUpdateRequest {
    private String content;
    private String type;
    private String status;
}

package com.multimodal.interview.entity;

import lombok.Data;

/**
 * 语音合成请求实体。
 */
@Data
public class SpeechSynthesis {
    private String text;
    private String vcn;
    private Integer pitch;
    private Integer speed;
    private String tte;
    private String audioPath;
}

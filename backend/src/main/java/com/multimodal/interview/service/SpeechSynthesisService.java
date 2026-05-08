package com.multimodal.interview.service;

import com.multimodal.interview.entity.SpeechSynthesis;

/**
 * 语音合成服务接口。
 */
public interface SpeechSynthesisService {
    /**
     * 语音合成
     *
     * @param request 语音合成请求
     * @return 语音合成结果
     */
    byte[] synthesizeSpeech(SpeechSynthesis request) throws Exception;
}

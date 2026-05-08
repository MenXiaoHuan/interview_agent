package com.multimodal.interview.service;

/**
 * 敏感词校验服务接口。
 */
public interface SensitiveWordService {
    /**
     * 判断文本中是否包含敏感词
     */
    boolean containsSensitiveWord(String text);
}

package com.multimodal.interview.service;

/**
 * 讯飞大模型服务接口。
 */
public interface XunfeiUltraAgentService {
    /**
     * 获取讯飞大模型回答
     *
     * @param systemContent 系统内容
     * @param userContent   用户内容
     * @return 讯飞大模型回答
     */
    String getAgentAnswer(String systemContent, String userContent);
}

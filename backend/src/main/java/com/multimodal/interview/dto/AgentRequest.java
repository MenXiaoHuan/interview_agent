package com.multimodal.interview.dto;

import lombok.Data;

import java.util.Map;

/**
 * AgentRequest 数据传输对象。
 */
@Data
public class AgentRequest {

    /**
     * AI Agent 标识。
     *
     * <p>当该字段存在时，后端会根据 agentKey 选择内置 agent，并使用后端统一维护的系统提示词。</p>
     */
    private String agentKey;

    /**
     * Agent 参数载荷（与 agentKey 对应）。
     */
    private Map<String, Object> params;

    /**
     * 会话 ID。
     *
     * <p>用于为同一个 Agent 对话绑定会话级上下文与压缩记忆。</p>
     */
    private String chatId;

}

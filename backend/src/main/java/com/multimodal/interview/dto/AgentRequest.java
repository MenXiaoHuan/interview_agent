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

    /**
     * 系统提示词（legacy 兼容字段）。
     *
     * <p>当 agentKey 为空时，后端将使用该字段与 userContent 直接调用模型，保持对旧调用方的兼容。</p>
     */
    private String systemContent;

    /**
     * 用户提示词（legacy 兼容字段）。
     */
    private String userContent;
}

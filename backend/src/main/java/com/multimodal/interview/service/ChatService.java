package com.multimodal.interview.service;

import com.multimodal.interview.dto.ChatMessageResponse;
import com.multimodal.interview.dto.ChatMessageSaveRequest;
import com.multimodal.interview.entity.ChatMessage;

import java.util.List;

/**
 * 聊天记录服务接口。
 */
public interface ChatService {
    ChatMessage save(Long userId, ChatMessageSaveRequest request);
    List<ChatMessageResponse> listRecent(Long userId, int days);
    List<ChatMessageResponse> listRecentGlobal(int days);
}

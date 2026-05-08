package com.multimodal.interview.service.impl;

import com.multimodal.interview.dto.ChatMessageResponse;
import com.multimodal.interview.dto.ChatMessageSaveRequest;
import com.multimodal.interview.entity.ChatMessage;
import com.multimodal.interview.mapper.ChatMessageMapper;
import com.multimodal.interview.service.ChatService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

/**
 * 聊天记录服务实现。
 */
@Service
public class ChatServiceImpl implements ChatService {
    private final ChatMessageMapper chatMessageMapper;

    public ChatServiceImpl(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatMessage save(Long userId, ChatMessageSaveRequest request) {
        ChatMessage message = new ChatMessage();
        message.setUserId(userId);
        message.setContent(request.getContent());
        message.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(message);
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChatMessageResponse> listRecent(Long userId, int days) {
        return chatMessageMapper.findRecentByUserId(userId, days);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChatMessageResponse> listRecentGlobal(int days) {
        return chatMessageMapper.findRecentGlobal(days);
    }
}

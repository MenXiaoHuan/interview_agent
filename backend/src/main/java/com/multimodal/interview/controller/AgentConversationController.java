package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.AgentConversationMemoryResponse;
import com.multimodal.interview.dto.AgentConversationMessageResponse;
import com.multimodal.interview.dto.AgentConversationResponse;
import com.multimodal.interview.dto.AgentConversationUpsertRequest;
import com.multimodal.interview.entity.User;
import com.multimodal.interview.mapper.UserMapper;
import com.multimodal.interview.service.AgentConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Agent 会话持久化接口。
 */
@Tag(name = "Agent 会话", description = "AI 助手会话列表与消息持久化接口")
@RestController
@RequestMapping("/api/agent-conversations")
public class AgentConversationController {
    private final AgentConversationService agentConversationService;
    private final UserMapper userMapper;

    public AgentConversationController(
            AgentConversationService agentConversationService,
            UserMapper userMapper
    ) {
        this.agentConversationService = agentConversationService;
        this.userMapper = userMapper;
    }

    @Operation(summary = "查询当前用户的 Agent 会话")
    @GetMapping
    public ApiResponse<List<AgentConversationResponse>> listCurrentUserConversations(
            @RequestParam(required = false) String agentKey,
            Authentication authentication
    ) {
        User user = userMapper.findByUsername(authentication.getName());
        return ApiResponse.success(agentConversationService.listUserConversations(user.getId(), agentKey));
    }

    @Operation(summary = "保存当前用户的 Agent 会话")
    @PostMapping("/session")
    public ApiResponse<AgentConversationResponse> upsertCurrentUserConversation(
            @RequestBody AgentConversationUpsertRequest request,
            Authentication authentication
    ) {
        User user = userMapper.findByUsername(authentication.getName());
        return ApiResponse.success("保存成功", agentConversationService.upsertUserConversation(user.getId(), request));
    }

    @Operation(summary = "删除当前用户的 Agent 会话")
    @DeleteMapping("/{chatId}")
    public ApiResponse<Void> deleteCurrentUserConversation(
            @PathVariable String chatId,
            Authentication authentication
    ) {
        User user = userMapper.findByUsername(authentication.getName());
        agentConversationService.deleteUserConversation(user.getId(), chatId);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "查询当前用户某个会话的消息记录")
    @GetMapping("/{chatId}/messages")
    public ApiResponse<List<AgentConversationMessageResponse>> listCurrentUserConversationMessages(
            @PathVariable String chatId,
            Authentication authentication
    ) {
        User user = userMapper.findByUsername(authentication.getName());
        return ApiResponse.success(agentConversationService.listUserConversationMessages(user.getId(), chatId));
    }

    @Operation(summary = "查询当前用户某个会话的事件记录")
    @GetMapping("/{chatId}/events")
    public ApiResponse<List<Map<String, Object>>> listCurrentUserConversationEvents(
            @PathVariable String chatId,
            Authentication authentication
    ) {
        User user = userMapper.findByUsername(authentication.getName());
        return ApiResponse.success(agentConversationService.listUserConversationEvents(user.getId(), chatId));
    }

    @Operation(summary = "查询当前用户某个会话的压缩记忆")
    @GetMapping("/{chatId}/memory")
    public ApiResponse<AgentConversationMemoryResponse> getCurrentUserConversationMemory(
            @PathVariable String chatId,
            Authentication authentication
    ) {
        User user = userMapper.findByUsername(authentication.getName());
        return ApiResponse.success(agentConversationService.getUserConversationMemory(user.getId(), chatId));
    }
}

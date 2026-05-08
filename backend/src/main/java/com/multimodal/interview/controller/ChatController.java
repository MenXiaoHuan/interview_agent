package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.ChatMessageResponse;
import com.multimodal.interview.dto.ChatMessageSaveRequest;
import com.multimodal.interview.entity.ChatMessage;
import com.multimodal.interview.entity.User;
import com.multimodal.interview.mapper.UserMapper;
import com.multimodal.interview.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 聊天记录接口控制器。
 */
@Tag(name = "聊天记录", description = "聊天记录保存与读取接口")
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;
    private final UserMapper userMapper;

    public ChatController(ChatService chatService, UserMapper userMapper) {
        this.chatService = chatService;
        this.userMapper = userMapper;
    }
    /**
     * 保存聊天记录。
     *
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "保存聊天记录")
    @PostMapping("/message")
    public ApiResponse<ChatMessage> save(@Valid @RequestBody ChatMessageSaveRequest request, Authentication authentication){
        User user = userMapper.findByUsername(authentication.getName());
        ChatMessage saved = chatService.save(user.getId(), request);
        return ApiResponse.success("保存成功", saved);
    }
    /**
     * 读取近1天聊天记录。
     *
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "读取近1天聊天记录")
    @GetMapping("/messages/1d")
    public ApiResponse<List<ChatMessageResponse>> list1d(Authentication authentication){
        return ApiResponse.success(chatService.listRecentGlobal(1));
    }
    /**
     * 读取近1天聊天记录(兼容days参数)。
     *
     * @param days 查询天数
     * @return 统一响应体
     */
    @Operation(summary = "读取近1天聊天记录(兼容days参数)")
    @GetMapping("/messages")
    public ApiResponse<List<ChatMessageResponse>> listCompat(@RequestParam(required = false) Integer days){
        if (days != null && days != 1) {
            return ApiResponse.badRequest("仅支持近1天查询，请使用 /api/chat/messages/1d 或 days=1");
        }
        return ApiResponse.success(chatService.listRecentGlobal(1));
    }
}

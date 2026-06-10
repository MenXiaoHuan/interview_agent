package com.multimodal.interview.controller;


import com.multimodal.interview.reactagent.ReactAgentRouter;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.common.result.ResultCode;
import com.multimodal.interview.dto.AgentRequest;
import com.multimodal.interview.service.XunfeiUltraAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * AI 统一调用入口控制器。
 *
 * <p>职责：</p>
 * <p>
 * - 对前端暴露统一的 AI 调用 REST 接口
 * </p>
 * <p>
 * - 新架构下：根据 {@code agentKey} 路由到项目内置的 ReactAgent 体系
 * </p>
 * <p>
 * - 兼容旧架构：未传 {@code agentKey} 时，仍允许直接透传 system/user prompt 到旧的讯飞调用实现
 * </p>
 * <p>
 * 这样前端只感知一个 AI 入口，而后端可以逐步把能力迁移到基于 Agent + Skill 的新架构。
 * </p>
 */
@Tag(name = "讯飞大模型", description = "讯飞大模型调用相关接口")
@Slf4j
@RestController
@RequestMapping("/api/xunfei")
public class AgentController {
    @Autowired
    private ReactAgentRouter reactAgentRouter;

    @Autowired
    private XunfeiUltraAgentService xunfeiUltraAgentService;
    /**
     * 获取 AI 能力返回结果。
     *
     * <p>调用路径分为两类：</p>
     * <p>
     * - 新路径：携带 {@code agentKey}，进入 {@link ReactAgentRouter}，按业务能力路由到指定 Agent
     * </p>
     * <p>
     * - 旧路径：不携带 {@code agentKey}，直接走旧的讯飞 prompt 调用服务
     * </p>
     *
     * @param request AI 调用请求体
     * @return 统一响应体；新路径通常返回对象化结构，旧路径通常返回字符串
     */
    @Operation(summary = "获取 AI/Agent 回答", description = "优先走 agentKey 对应的 ReactAgent；未传 agentKey 时兼容旧的讯飞 prompt 调用方式")
    @PostMapping("/getAgentAnswer")
    public ApiResponse<Object> getAgentAnswer(@RequestBody AgentRequest request){
        try {
            Object answer;
            if (request.getAgentKey() != null && !request.getAgentKey().isBlank()) {
                // 新路径：真正的 Spring AI Alibaba ReactAgent
                answer = reactAgentRouter.run(request.getAgentKey(), request.getParams(), request.getChatId());
            } else {
                // 旧路径兼容：直接转发 system/user prompt 到原讯飞实现
                answer = xunfeiUltraAgentService.getAgentAnswer(request.getSystemContent(), request.getUserContent());
            }
            return ApiResponse.success("获取答案成功", answer);
        } catch (BusinessException e) {
            log.warn("Agent调用业务异常, agentKey={}, chatId={}, code={}, message={}",
                    request.getAgentKey(),
                    request.getChatId(),
                    e.getCode(),
                    e.getMessage());
            if (e.getCode() == ResultCode.SERVICE_UNAVAILABLE.getCode()) {
                return ApiResponse.serviceUnavailable("AI 服务当前不可用");
            }
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Agent调用失败, agentKey={}, chatId={}, paramsKeys={}",
                    request.getAgentKey(),
                    request.getChatId(),
                    request.getParams() == null ? "[]" : request.getParams().keySet(),
                    e);
            return ApiResponse.error(ResultCode.INTERNAL_ERROR, "获取答案失败");
        }

    }

}

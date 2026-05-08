package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.RankingResponse;
import com.multimodal.interview.service.RankingSpecializedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 专项评测排行榜接口控制器。
 */
@Tag(name = "专项评测排行榜", description = "试题、简历、场景专项评测排行榜接口")
@RestController
@RequestMapping("/api/rank/specialized")
public class RankingSpecializedController {
    private final RankingSpecializedService service;

    public RankingSpecializedController(RankingSpecializedService service) {
        this.service = service;
    }
    /**
     * 专项评测-简历排行榜（按最高分）。
     *
     * @param jobId 岗位 ID
     * @param evaluationType 评估类型
     * @param size 返回数量
     * @return 统一响应体
     */
    @Operation(summary = "专项评测-简历排行榜（按最高分）")
    @GetMapping("/resume")
    public ApiResponse<List<RankingResponse>> resume(@RequestParam(required = false) Long jobId,
                                                     @RequestParam(required = false) String evaluationType,
                                                     @RequestParam(defaultValue = "50") int size){
        return ApiResponse.success(service.topResume(jobId, evaluationType, size));
    }
    /**
     * 专项评测-试题排行榜（按最高分）。
     *
     * @param jobId 岗位 ID
     * @param evaluationType 评估类型
     * @param size 返回数量
     * @return 统一响应体
     */
    @Operation(summary = "专项评测-试题排行榜（按最高分）")
    @GetMapping("/question")
    public ApiResponse<List<RankingResponse>> question(@RequestParam(required = false) Long jobId,
                                                       @RequestParam(required = false) String evaluationType,
                                                       @RequestParam(defaultValue = "50") int size){
        return ApiResponse.success(service.topQuestion(jobId, evaluationType, size));
    }
    /**
     * 专项评测-场景排行榜（按最高分）。
     *
     * @param evaluationType 评估类型
     * @param size 返回数量
     * @return 统一响应体
     */
    @Operation(summary = "专项评测-场景排行榜（按最高分）")
    @GetMapping("/scenario")
    public ApiResponse<List<RankingResponse>> scenario(@RequestParam(required = false) String evaluationType,
                                                       @RequestParam(defaultValue = "50") int size){
        return ApiResponse.success(service.topScenario(evaluationType, size));
    }
}


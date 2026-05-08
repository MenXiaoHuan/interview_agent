package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.RankingResponse;
import com.multimodal.interview.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 综合测评排行榜接口控制器。
 */
@Tag(name = "综合测评排行榜", description = "简历、试题、场景排行榜接口")
@RestController
@RequestMapping("/api/rank/comprehensive")
public class RankingController {
    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }
    /**
     * 综合测评-简历排行榜（按最高总分）。
     *
     * @param jobId 岗位 ID
     * @param size 返回数量
     * @return 统一响应体
     */
    @Operation(summary = "综合测评-简历排行榜（按最高总分）")
    @GetMapping("/resume")
    public ApiResponse<List<RankingResponse>> topResume(@RequestParam(required = false) Long jobId,
                                                        @RequestParam(defaultValue = "50") int size){
        return ApiResponse.success(rankingService.topResume(jobId, size));
    }
    /**
     * 综合测评-试题排行榜（按最高总分）。
     *
     * @param jobId 岗位 ID
     * @param size 返回数量
     * @return 统一响应体
     */
    @Operation(summary = "综合测评-试题排行榜（按最高总分）")
    @GetMapping("/question")
    public ApiResponse<List<RankingResponse>> topQuestion(@RequestParam(required = false) Long jobId,
                                                          @RequestParam(defaultValue = "50") int size){
        return ApiResponse.success(rankingService.topQuestion(jobId, size));
    }
    /**
     * 综合测评-场景排行榜（按最高总分）。
     *
     * @param jobId 岗位 ID
     * @param size 返回数量
     * @return 统一响应体
     */
    @Operation(summary = "综合测评-场景排行榜（按最高总分）")
    @GetMapping("/scenario")
    public ApiResponse<List<RankingResponse>> topScenario(@RequestParam(required = false) Long jobId,
                                                          @RequestParam(defaultValue = "50") int size){
        return ApiResponse.success(rankingService.topScenario(jobId, size));
    }
}

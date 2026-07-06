package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.RankingResponse;
import com.multimodal.interview.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * AIview 排行榜接口控制器。
 */
@Tag(name = "AIview排行榜", description = "简历、试题、一面、二面、三面排行榜接口")
@RestController
@RequestMapping("/api/rank/aiview")
public class RankingController {
    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }
    /**
     * AIview-简历排行榜（每人取最高分）。
     *
     * @param jobId 岗位 ID
     * @param size 返回数量
     * @return 统一响应体
     */
    @Operation(summary = "AIview-简历排行榜（每人取最高分）")
    @GetMapping("/resume")
    public ApiResponse<List<RankingResponse>> topResume(@RequestParam(required = false) Long jobId,
                                                        @RequestParam(defaultValue = "50") int size){
        return ApiResponse.success(rankingService.topResume(jobId, size));
    }
    /**
     * AIview-试题排行榜（每人取最高分）。
     *
     * @param jobId 岗位 ID
     * @param size 返回数量
     * @return 统一响应体
     */
    @Operation(summary = "AIview-试题排行榜（每人取最高分）")
    @GetMapping("/question")
    public ApiResponse<List<RankingResponse>> topQuestion(@RequestParam(required = false) Long jobId,
                                                          @RequestParam(defaultValue = "50") int size){
        return ApiResponse.success(rankingService.topQuestion(jobId, size));
    }
    /**
     * AIview-面试轮次排行榜（每人每轮取最高分）。
     *
     * @param jobId 岗位 ID
     * @param size 返回数量
     * @return 统一响应体
     */
    @Operation(summary = "AIview-面试轮次排行榜（每人每轮取最高分）")
    @GetMapping("/round/{roundType}")
    public ApiResponse<List<RankingResponse>> topRound(@PathVariable String roundType,
                                                       @RequestParam(required = false) Long jobId,
                                                       @RequestParam(defaultValue = "50") int size){
        return ApiResponse.success(rankingService.topRound(roundType, jobId, size));
    }
}

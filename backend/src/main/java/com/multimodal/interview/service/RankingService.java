package com.multimodal.interview.service;

import com.multimodal.interview.dto.RankingResponse;

import java.util.List;

/**
 * AIview 排行榜服务接口。
 */
public interface RankingService {
    List<RankingResponse> topResume(Long jobId, int size);
    List<RankingResponse> topQuestion(Long jobId, int size);
    List<RankingResponse> topRound(String roundType, Long jobId, int size);
}

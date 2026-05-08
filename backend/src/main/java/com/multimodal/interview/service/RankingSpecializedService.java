package com.multimodal.interview.service;

import com.multimodal.interview.dto.RankingResponse;

import java.util.List;

/**
 * 专项评测排行榜服务接口。
 */
public interface RankingSpecializedService {
    List<RankingResponse> topResume(Long jobId, String evaluationType, int size);
    List<RankingResponse> topQuestion(Long jobId, String evaluationType, int size);
    List<RankingResponse> topScenario(String evaluationType, int size);
}

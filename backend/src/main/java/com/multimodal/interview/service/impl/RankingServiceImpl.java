package com.multimodal.interview.service.impl;

import com.multimodal.interview.dto.RankingResponse;
import com.multimodal.interview.mapper.RankingMapper;
import com.multimodal.interview.service.RankingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AIview 排行榜服务实现。
 */
@Service
public class RankingServiceImpl implements RankingService {
    private final RankingMapper rankingMapper;

    public RankingServiceImpl(RankingMapper rankingMapper) {
        this.rankingMapper = rankingMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RankingResponse> topResume(Long jobId, int size) {
        return withDenseRank(rankingMapper.topResume(jobId, size));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RankingResponse> topQuestion(Long jobId, int size) {
        return withDenseRank(rankingMapper.topQuestion(jobId, size));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RankingResponse> topRound(String roundType, Long jobId, int size) {
        return withDenseRank(rankingMapper.topRound(roundType, jobId, size));
    }

    private List<RankingResponse> withDenseRank(List<RankingResponse> list) {
        List<RankingResponse> ranked = new ArrayList<>();
        double lastScore = Double.NaN;
        int currentRank = 0;
        for (RankingResponse r : list) {
            double s = r.getScore() != null ? r.getScore() : 0d;
            if (Double.isNaN(lastScore) || Double.compare(s, lastScore) != 0) {
                currentRank += 1;
                lastScore = s;
            }
            r.setRank(currentRank);
            ranked.add(r);
        }
        return ranked;
    }
}

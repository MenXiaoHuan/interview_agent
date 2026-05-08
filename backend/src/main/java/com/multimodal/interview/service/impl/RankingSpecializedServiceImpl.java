package com.multimodal.interview.service.impl;

import com.multimodal.interview.dto.RankingResponse;
import com.multimodal.interview.mapper.RankingSpecializedMapper;
import com.multimodal.interview.service.RankingSpecializedService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 专项评测排行榜服务实现。
 */
@Service
public class RankingSpecializedServiceImpl implements RankingSpecializedService {
    private final RankingSpecializedMapper mapper;

    public RankingSpecializedServiceImpl(RankingSpecializedMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RankingResponse> topResume(Long jobId, String evaluationType, int size) {
        return withDenseRank(mapper.topResume(jobId, evaluationType, size));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RankingResponse> topQuestion(Long jobId, String evaluationType, int size) {
        return withDenseRank(mapper.topQuestion(jobId, evaluationType, size));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RankingResponse> topScenario(String evaluationType, int size) {
        return withDenseRank(mapper.topScenario(evaluationType, size));
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


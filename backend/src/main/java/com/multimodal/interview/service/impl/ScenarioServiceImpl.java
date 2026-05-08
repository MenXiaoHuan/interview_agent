package com.multimodal.interview.service.impl;

import com.multimodal.interview.dto.SaveScenarioAnalysisHistoryRequest;
import com.multimodal.interview.entity.ScenarioAnalysis;
import com.multimodal.interview.mapper.ScenarioAnalysisMapper;
import com.multimodal.interview.service.ScenarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 场景分析服务实现。
 */
@Slf4j
@Service
public class ScenarioServiceImpl implements ScenarioService {
    @Autowired
    private ScenarioAnalysisMapper scenarioAnalysisMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveScenarioAnalysisHistory(SaveScenarioAnalysisHistoryRequest scenarioHistory, String evaluationType) {
        ScenarioAnalysis scenarioAnalysis = new ScenarioAnalysis();
        scenarioAnalysis.setUserId(scenarioHistory.getUserId());
        scenarioAnalysis.setTotalScore(scenarioHistory.getTotalScore());
        scenarioAnalysis.setScore1(scenarioHistory.getScore1());
        scenarioAnalysis.setSuggestion1(scenarioHistory.getSuggestion1());
        scenarioAnalysis.setScore2(scenarioHistory.getScore2());
        scenarioAnalysis.setSuggestion2(scenarioHistory.getSuggestion2());
        scenarioAnalysis.setScore3(scenarioHistory.getScore3());
        scenarioAnalysis.setSuggestion3(scenarioHistory.getSuggestion3());
        scenarioAnalysis.setScore4(scenarioHistory.getScore4());
        scenarioAnalysis.setSuggestion4(scenarioHistory.getSuggestion4());
        scenarioAnalysis.setScore5(scenarioHistory.getScore5());
        scenarioAnalysis.setSuggestion5(scenarioHistory.getSuggestion5());
        scenarioAnalysis.setEvaluationType(evaluationType);
        scenarioAnalysisMapper.insert(scenarioAnalysis);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ScenarioAnalysis> getScenarioAnalysisHistory(String userId, String evaluationType) {
        return scenarioAnalysisMapper.findByUserIdAndEvaluationType(userId, evaluationType);
    }
} 

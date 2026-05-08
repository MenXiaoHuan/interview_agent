package com.multimodal.interview.service;

import com.multimodal.interview.dto.SaveScenarioAnalysisHistoryRequest;
import com.multimodal.interview.entity.ScenarioAnalysis;

import java.util.List;

/**
 * 场景分析服务接口。
 */
public interface ScenarioService {
    /**
     * 保存场景分析历史。
     */
    void saveScenarioAnalysisHistory(SaveScenarioAnalysisHistoryRequest scenarioHistory, String evaluationType);

    /**
     * 获取场景分析历史。
     */
    List<ScenarioAnalysis> getScenarioAnalysisHistory(String userId, String evaluationType);
}

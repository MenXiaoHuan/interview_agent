package com.multimodal.interview.service;

import com.multimodal.interview.entity.ScenarioQuestion;

import java.util.List;

/**
 * 场景问题服务接口。
 */
public interface ScenarioQuestionService {
    /**
     * 获取场景问题
     */
    List<ScenarioQuestion> getQuestion(Long jobId);

    /**
     * 获取所有场景问题
     */
    List<ScenarioQuestion> getAll();

    /**
     * 创建场景问题
     */
    void createQuestion(ScenarioQuestion scenarioQuestion);

    /**
     * 更新场景问题
     */
    void updateQuestion(ScenarioQuestion scenarioQuestion);

    /**
     * 删除场景问题
     */
    void deleteQuestion(Long id);
}

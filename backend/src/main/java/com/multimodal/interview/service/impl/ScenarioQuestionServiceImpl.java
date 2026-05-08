package com.multimodal.interview.service.impl;

import com.multimodal.interview.entity.ScenarioQuestion;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.mapper.ScenarioQuestionMapper;
import com.multimodal.interview.service.ScenarioQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 场景问题服务实现。
 */
@Service
public class ScenarioQuestionServiceImpl implements ScenarioQuestionService {
    @Autowired
    private ScenarioQuestionMapper scenarioQuestionMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ScenarioQuestion> getQuestion(Long jobId) {
        try {
            return scenarioQuestionMapper.getQuestionsByJobId(jobId);
        } catch (Exception e) {
            throw BusinessException.badRequest("查找失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ScenarioQuestion> getAll() {
        try {
            return scenarioQuestionMapper.getAll();
        } catch (Exception e) {
            throw BusinessException.badRequest("查找失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void createQuestion(ScenarioQuestion scenarioQuestion) {
        int result = scenarioQuestionMapper.addQuestion(scenarioQuestion);
        if (result == 0) {
            throw BusinessException.badRequest("创建失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateQuestion(ScenarioQuestion scenarioQuestion) {
        int result = scenarioQuestionMapper.updateQuestion(scenarioQuestion);
        if (result == 0) {
            throw BusinessException.badRequest("更新失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        int result = scenarioQuestionMapper.deleteQuestion(id);
        if (result == 0) {
            throw BusinessException.badRequest("删除失败");
        }
    }
}

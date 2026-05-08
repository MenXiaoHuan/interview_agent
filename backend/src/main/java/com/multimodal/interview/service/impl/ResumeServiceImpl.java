package com.multimodal.interview.service.impl;

import com.multimodal.interview.dto.SaveResumeHistoryRequest;
import com.multimodal.interview.entity.ResumeAnalysisHistory;
import com.multimodal.interview.entity.ResumeHistoricalCopy;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.mapper.ResumeAnalysisHistoryMapper;
import com.multimodal.interview.mapper.ResumeHistoricalCopyMapper;
import com.multimodal.interview.service.ResumeService;
import com.multimodal.interview.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 简历分析服务实现。
 */
@Slf4j
@Service
public class ResumeServiceImpl implements ResumeService {
    @Autowired
    private ResumeAnalysisHistoryMapper resumeAnalysisHistoryMapper;

    @Autowired
    private ResumeHistoricalCopyMapper resumeHistoricalCopyMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public String extractResumeContent(MultipartFile file) throws IOException {
        return FileUtil.extractText(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String saveResumeHistory(SaveResumeHistoryRequest resumeHistory, String evaluationType) {
        ResumeAnalysisHistory resumeAnalysisHistory = createResumeAnalysisHistory(resumeHistory, evaluationType);
        if (resumeHistory.getId() == null) {
            resumeAnalysisHistory.setId(UUID.randomUUID().toString());
            resumeAnalysisHistoryMapper.insert(resumeAnalysisHistory);
            createResumeHistoricalCopy(resumeAnalysisHistory);
            return resumeAnalysisHistory.getId();
        } else {
            resumeAnalysisHistory.setId(resumeHistory.getId());
            resumeAnalysisHistoryMapper.update(resumeAnalysisHistory);
            createResumeHistoricalCopy(resumeAnalysisHistory);
            return resumeAnalysisHistory.getId();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResumeAnalysisHistory> getResumeAnalysisHistoryByUserIdAndEvaluationType(Long userId, String evaluationType) {
        return resumeAnalysisHistoryMapper.findByUserIdAndevaluationType(userId, evaluationType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResumeAnalysisHistory getResumeAnalysisHistoryByResumeHistoryId(String resumeHistoryId) {
        return resumeAnalysisHistoryMapper.findByResumeHistoryId(resumeHistoryId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String rollBackResumeContent(String resumeHistoryId) {
        ResumeAnalysisHistory resume = resumeAnalysisHistoryMapper.findByResumeHistoryId(resumeHistoryId);
        if (resume == null) {
            throw BusinessException.badRequest("简历不存在");
        }

        // 获取最新的历史版本
        ResumeHistoricalCopy history = resumeHistoricalCopyMapper.findLatestByResumeId(resumeHistoryId);
        if (history == null) {
            throw BusinessException.badRequest("没有可恢复的历史版本");
        }

        return history.getContent();
    }

    private ResumeAnalysisHistory createResumeAnalysisHistory(SaveResumeHistoryRequest resumeHistory, String evaluationType) {
        ResumeAnalysisHistory resumeAnalysisHistory = new ResumeAnalysisHistory();

        resumeAnalysisHistory.setUserId(resumeHistory.getUserId());
        resumeAnalysisHistory.setJobId(resumeHistory.getJobId());
        resumeAnalysisHistory.setResumeContent(resumeHistory.getResumeContent());
        resumeAnalysisHistory.setScore(resumeHistory.getScore());
        resumeAnalysisHistory.setAdvantages(resumeHistory.getFeedback().getAdvantages());
        resumeAnalysisHistory.setDisadvantages(resumeHistory.getFeedback().getDisadvantages());
        resumeAnalysisHistory.setImprovementSuggestions(resumeHistory.getFeedback().getImprovementSuggestions());
        resumeAnalysisHistory.setProfessionalSkills(resumeHistory.getCompetenciesScore().getProfessionalSkills());
        resumeAnalysisHistory.setProjectExecution(resumeHistory.getCompetenciesScore().getProjectExecution());
        resumeAnalysisHistory.setInnovation(resumeHistory.getCompetenciesScore().getInnovation());
        resumeAnalysisHistory.setCommunication(resumeHistory.getCompetenciesScore().getCommunication());
        resumeAnalysisHistory.setAdaptability(resumeHistory.getCompetenciesScore().getAdaptability());
        resumeAnalysisHistory.setEvaluationType(evaluationType);

        return resumeAnalysisHistory;
    }

    private void createResumeHistoricalCopy(ResumeAnalysisHistory resumeAnalysisHistory) {
        ResumeHistoricalCopy resumeHistoricalCopy = new ResumeHistoricalCopy();
        resumeHistoricalCopy.setId(UUID.randomUUID().toString());
        resumeHistoricalCopy.setResumeHistoryId(resumeAnalysisHistory.getId());
        resumeHistoricalCopy.setContent(resumeAnalysisHistory.getResumeContent());

        resumeHistoricalCopyMapper.insert(resumeHistoricalCopy);
    }
} 

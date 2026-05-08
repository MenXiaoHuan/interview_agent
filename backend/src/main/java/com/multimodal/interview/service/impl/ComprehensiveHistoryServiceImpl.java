package com.multimodal.interview.service.impl;

import com.multimodal.interview.dto.*;
import com.multimodal.interview.entity.*;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.mapper.*;
import com.multimodal.interview.service.ComprehensiveHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 综合评估历史服务实现。
 */
@Slf4j
@Service
public class ComprehensiveHistoryServiceImpl implements ComprehensiveHistoryService {

    @Autowired
    private ComprehensiveReportHistoryMapper reportHistoryMapper;

    @Autowired
    private ComprehensiveResumeHistoryMapper resumeHistoryMapper;

    @Autowired
    private ComprehensiveQuestionHistoryMapper questionHistoryMapper;

    @Autowired
    private ComprehensiveScenarioHistoryMapper scenarioHistoryMapper;

    @Autowired
    private JobMapper jobMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ComprehensiveResumeHistory saveResumeHistory(ComprehensiveResumeHistoryRequest request) {
        ComprehensiveResumeHistory resume = new ComprehensiveResumeHistory();
        BeanUtils.copyProperties(request, resume);

        int result = resumeHistoryMapper.insert(resume);
        if (result <= 0) {
            throw BusinessException.internalError("保存简历综合评估历史失败");
        }

        return resume;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ComprehensiveQuestionHistory saveQuestionHistory(ComprehensiveQuestionHistoryRequest request) {
        ComprehensiveQuestionHistory question = new ComprehensiveQuestionHistory();
        BeanUtils.copyProperties(request, question);

        int result = questionHistoryMapper.insert(question);
        if (result <= 0) {
            throw BusinessException.internalError("保存问题综合评估历史失败");
        }

        return question;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ComprehensiveScenarioHistory saveScenarioHistory(ComprehensiveScenarioHistoryRequest request) {
        ComprehensiveScenarioHistory scenario = new ComprehensiveScenarioHistory();
        BeanUtils.copyProperties(request, scenario);

        // 验证和清理 aiAssessment 数据
        if (scenario.getAiAssessment() != null) {
            String aiAssessment = scenario.getAiAssessment().trim();

            // 处理包含 Markdown 代码块的 JSON 格式
            if (aiAssessment.startsWith("```json\n") && aiAssessment.endsWith("\n```")) {
                // 提取 JSON 内容，去掉 Markdown 代码块标记
                String jsonContent = aiAssessment.substring(8, aiAssessment.length() - 4).trim();
                scenario.setAiAssessment(jsonContent);
            } else if (aiAssessment.startsWith("```") && aiAssessment.endsWith("```")) {
                // 处理其他类型的代码块
                String jsonContent = aiAssessment.substring(3, aiAssessment.length() - 3).trim();
                scenario.setAiAssessment(jsonContent);
            } else if (aiAssessment.equals("Invalid value.") || aiAssessment.isEmpty() ||
                    aiAssessment.equals("null") || aiAssessment.equals("undefined")) {
                scenario.setAiAssessment(null);
            } else {
                // 验证是否为有效的 JSON 格式
                try {
                    // 检查是否以 { 或 [ 开头，并且以 } 或 ] 结尾
                    if ((aiAssessment.startsWith("{") && aiAssessment.endsWith("}")) ||
                            (aiAssessment.startsWith("[") && aiAssessment.endsWith("]"))) {
                        // 使用 Jackson 验证 JSON 格式
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        mapper.readTree(aiAssessment); // 如果JSON无效，这里会抛出异常
                    } else {
                        // 不是有效的JSON格式，设置为null
                        scenario.setAiAssessment(null);
                    }
                } catch (Exception e) {
                    // 如果JSON格式无效，设置为null
                    log.warn("AI Assessment JSON格式无效，设置为null: {}", aiAssessment);
                    scenario.setAiAssessment(null);
                }
            }
        }

        int result = scenarioHistoryMapper.insert(scenario);
        if (result <= 0) {
            throw BusinessException.internalError("保存场景综合评估历史失败");
        }

        return scenario;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ComprehensiveReportHistory saveComprehensiveReport(ComprehensiveReportRequest request) {
        ComprehensiveReportHistory report = new ComprehensiveReportHistory();
        BeanUtils.copyProperties(request, report);

        int result = reportHistoryMapper.insert(report);
        if (result <= 0) {
            throw BusinessException.internalError("保存综合评估报告失败");
        }

        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComprehensiveReportResponse getComprehensiveReportById(Long id) {
        ComprehensiveReportHistory report = reportHistoryMapper.findById(id);
        if (report == null) {
            throw BusinessException.notFound("未找到ID为" + id + "的综合评估报告");
        }

        return convertToResponse(report);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ComprehensiveReportResponse> getComprehensiveReportsByUserId(Long userId) {
        List<ComprehensiveReportHistory> reports = reportHistoryMapper.findByUserId(userId);
        return reports.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    private ComprehensiveReportResponse convertToResponse(ComprehensiveReportHistory report) {
        ComprehensiveReportResponse response = new ComprehensiveReportResponse();
        BeanUtils.copyProperties(report, response);

        // 设置职位名称
        if (report.getJobId() != null) {
            Job job = jobMapper.getJobById(report.getJobId());
            if (job != null) {
                response.setJobTitle(job.getName());
                response.setJob(job);
            }
        }

        return response;
    }
} 

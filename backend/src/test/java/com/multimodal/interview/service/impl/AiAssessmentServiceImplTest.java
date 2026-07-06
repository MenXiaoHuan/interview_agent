package com.multimodal.interview.service.impl;

import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.dto.AiQuestionHistoryRequest;
import com.multimodal.interview.dto.AiInterviewRoundHistoryRequest;
import com.multimodal.interview.entity.AiAssessmentSession;
import com.multimodal.interview.entity.AiQuestionHistory;
import com.multimodal.interview.entity.AiResumeHistory;
import com.multimodal.interview.entity.AiInterviewRoundHistory;
import com.multimodal.interview.mapper.AiAssessmentSessionMapper;
import com.multimodal.interview.mapper.AiQuestionHistoryMapper;
import com.multimodal.interview.mapper.AiResumeHistoryMapper;
import com.multimodal.interview.mapper.AiInterviewRoundHistoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiAssessmentServiceImplTest {

    @Test
    void rejectsQuestionHistoryWhenResumeScoreIsBelowThreshold() {
        AiAssessmentServiceImpl service = createService();
        AiAssessmentSessionMapper sessionMapper = getSessionMapper(service);
        AiResumeHistoryMapper resumeMapper = getResumeMapper(service);
        AiQuestionHistoryRequest request = new AiQuestionHistoryRequest();
        request.setAssessmentSessionId("cmp_001");
        request.setUserId(1L);
        request.setJobId(2L);

        AiAssessmentSession session = new AiAssessmentSession();
        session.setAssessmentSessionId("cmp_001");
        session.setStatus("IN_PROGRESS");
        when(sessionMapper.findByAssessmentSessionId("cmp_001")).thenReturn(session);

        AiResumeHistory resumeHistory = new AiResumeHistory();
        resumeHistory.setAssessmentSessionId("cmp_001");
        resumeHistory.setOverallScore(79);
        when(resumeMapper.findLatestByAssessmentSessionId("cmp_001")).thenReturn(resumeHistory);

        assertThatThrownBy(() -> service.saveQuestionHistory(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("80");
    }

    @Test
    void rejectsRoundTwoWhenRoundOneHistoryIsMissing() {
        AiAssessmentServiceImpl service = createService();
        AiAssessmentSessionMapper sessionMapper = getSessionMapper(service);
        AiResumeHistoryMapper resumeMapper = getResumeMapper(service);
        AiQuestionHistoryMapper questionMapper = getQuestionMapper(service);
        AiInterviewRoundHistoryMapper scenarioMapper = getScenarioMapper(service);
        AiInterviewRoundHistoryRequest request = new AiInterviewRoundHistoryRequest();
        request.setAssessmentSessionId("cmp_002");
        request.setUserId(1L);
        request.setJobId(2L);
        request.setRoundType("round_2");

        AiAssessmentSession session = new AiAssessmentSession();
        session.setAssessmentSessionId("cmp_002");
        session.setStatus("IN_PROGRESS");
        when(sessionMapper.findByAssessmentSessionId("cmp_002")).thenReturn(session);

        AiResumeHistory resumeHistory = new AiResumeHistory();
        resumeHistory.setAssessmentSessionId("cmp_002");
        resumeHistory.setOverallScore(85);
        when(resumeMapper.findLatestByAssessmentSessionId("cmp_002")).thenReturn(resumeHistory);
        AiQuestionHistory questionHistory = new AiQuestionHistory();
        questionHistory.setOverallScore(85);
        when(questionMapper.findLatestByAssessmentSessionId("cmp_002")).thenReturn(questionHistory);
        when(scenarioMapper.findLatestByAssessmentSessionIdAndRoundType("cmp_002", "round_1")).thenReturn(null);

        assertThatThrownBy(() -> service.saveScenarioHistory(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("技术一面");
    }

    @Test
    void marksSessionPassedWhenHrRoundIsSaved() {
        AiAssessmentServiceImpl service = createService();
        AiAssessmentSessionMapper sessionMapper = getSessionMapper(service);
        AiResumeHistoryMapper resumeMapper = getResumeMapper(service);
        AiQuestionHistoryMapper questionMapper = getQuestionMapper(service);
        AiInterviewRoundHistoryMapper scenarioMapper = getScenarioMapper(service);
        AiInterviewRoundHistoryRequest request = new AiInterviewRoundHistoryRequest();
        request.setAssessmentSessionId("cmp_003");
        request.setUserId(1L);
        request.setJobId(2L);
        request.setRoundType("round_3");
        request.setOverallScore(88);

        AiAssessmentSession session = new AiAssessmentSession();
        session.setAssessmentSessionId("cmp_003");
        session.setStatus("IN_PROGRESS");
        when(sessionMapper.findByAssessmentSessionId("cmp_003")).thenReturn(session);

        AiResumeHistory resumeHistory = new AiResumeHistory();
        resumeHistory.setAssessmentSessionId("cmp_003");
        resumeHistory.setOverallScore(88);
        when(resumeMapper.findLatestByAssessmentSessionId("cmp_003")).thenReturn(resumeHistory);
        AiQuestionHistory questionHistory = new AiQuestionHistory();
        questionHistory.setOverallScore(88);
        when(questionMapper.findLatestByAssessmentSessionId("cmp_003")).thenReturn(questionHistory);
        AiInterviewRoundHistory roundOneHistory = new AiInterviewRoundHistory();
        roundOneHistory.setOverallScore(88);
        AiInterviewRoundHistory roundTwoHistory = new AiInterviewRoundHistory();
        roundTwoHistory.setOverallScore(88);
        when(scenarioMapper.findLatestByAssessmentSessionIdAndRoundType("cmp_003", "round_1")).thenReturn(roundOneHistory);
        when(scenarioMapper.findLatestByAssessmentSessionIdAndRoundType("cmp_003", "round_2")).thenReturn(roundTwoHistory);
        when(scenarioMapper.insert(org.mockito.ArgumentMatchers.any(AiInterviewRoundHistory.class))).thenReturn(1);

        service.saveScenarioHistory(request);

        assertThat(session.getStatus()).isEqualTo("PASSED");
        assertThat(session.getCurrentStage()).isEqualTo("completed");
        verify(sessionMapper).updateByAssessmentSessionId(session);
    }

    private static AiAssessmentServiceImpl createService() {
        AiAssessmentServiceImpl service = new AiAssessmentServiceImpl();
        ReflectionTestUtils.setField(service, "resumeHistoryMapper", mock(AiResumeHistoryMapper.class));
        ReflectionTestUtils.setField(service, "questionHistoryMapper", mock(AiQuestionHistoryMapper.class));
        ReflectionTestUtils.setField(service, "scenarioHistoryMapper", mock(AiInterviewRoundHistoryMapper.class));
        ReflectionTestUtils.setField(service, "assessmentSessionMapper", mock(AiAssessmentSessionMapper.class));
        return service;
    }

    private static AiResumeHistoryMapper getResumeMapper(AiAssessmentServiceImpl service) {
        return (AiResumeHistoryMapper) ReflectionTestUtils.getField(service, "resumeHistoryMapper");
    }

    private static AiQuestionHistoryMapper getQuestionMapper(AiAssessmentServiceImpl service) {
        return (AiQuestionHistoryMapper) ReflectionTestUtils.getField(service, "questionHistoryMapper");
    }

    private static AiInterviewRoundHistoryMapper getScenarioMapper(AiAssessmentServiceImpl service) {
        return (AiInterviewRoundHistoryMapper) ReflectionTestUtils.getField(service, "scenarioHistoryMapper");
    }

    private static AiAssessmentSessionMapper getSessionMapper(AiAssessmentServiceImpl service) {
        return (AiAssessmentSessionMapper) ReflectionTestUtils.getField(service, "assessmentSessionMapper");
    }
}

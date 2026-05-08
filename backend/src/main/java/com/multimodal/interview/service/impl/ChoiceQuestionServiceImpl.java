package com.multimodal.interview.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.cache.ReadCacheService;
import com.multimodal.interview.dto.InterviewResultResponse;
import com.multimodal.interview.dto.InterviewSubmitRequest;
import com.multimodal.interview.dto.InterviewSubmitResponse;
import com.multimodal.interview.dto.SubmittedAnswerRequest;
import com.multimodal.interview.entity.choiceAnswer;
import com.multimodal.interview.entity.choiceQuestion;
import com.multimodal.interview.entity.choiceQuestionRecord;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.mapper.ChoiceAnswerMapper;
import com.multimodal.interview.mapper.ChoiceQuestionMapper;
import com.multimodal.interview.mapper.ChoiceQuestionRecordMapper;
import com.multimodal.interview.service.ChoiceQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 试题作答服务实现。
 */
@Service
public class ChoiceQuestionServiceImpl implements ChoiceQuestionService {
    private static final String CACHE_PREFIX = "read-cache:choice-question:";
    private static final Duration QUESTION_TTL = Duration.ofMinutes(6);
    private static final Duration NULL_TTL = Duration.ofMinutes(2);

    @Autowired
    private ChoiceQuestionMapper questionRepository;

    @Autowired
    private ChoiceQuestionRecordMapper recordRepository;

    @Autowired
    private ChoiceAnswerMapper answerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReadCacheService readCacheService;

    @Autowired
    private ChoiceQuestionRecordMapper choiceQuestionRecordMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<choiceQuestion> getAllQuestions() {
        List<choiceQuestion> cached = readCacheService.getOrLoad(
                CACHE_PREFIX + "all",
                objectMapper.getTypeFactory().constructCollectionType(List.class, choiceQuestion.class),
                QUESTION_TTL,
                NULL_TTL,
                questionRepository::getAll
        );
        return cached == null ? List.of() : cached;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<choiceQuestion> getQuestions(Long jobId) {
        List<choiceQuestion> cached = readCacheService.getOrLoad(
                CACHE_PREFIX + "job:" + jobId,
                objectMapper.getTypeFactory().constructCollectionType(List.class, choiceQuestion.class),
                QUESTION_TTL,
                NULL_TTL,
                () -> questionRepository.findRandomQuestionsByJobId(jobId)
        );
        return cached == null ? List.of() : cached;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public InterviewSubmitResponse submitAnswers(Long userId, InterviewSubmitRequest request) {
        // 1. 验证请求数据
        if (request == null || request.getJobId() == null || request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("请求数据无效：request、jobId 或 answers 不能为 null（空值）或空内容");
        }

        // 2. 获取所有相关问题并按ID存储，方便查找
        List<choiceQuestion> allJobQuestions = questionRepository.findByJobId(request.getJobId());
        Map<Long, choiceQuestion> questionMap = allJobQuestions.stream()
                .collect(Collectors.toMap(choiceQuestion::getId, Function.identity()));

        // 3. 处理提交的答案，计算分数和正确题数
        int totalScore = 0;
        int correctCount = 0;
        List<choiceAnswer> interviewAnswers = new ArrayList<>();
        List<InterviewSubmitResponse.QuestionResult> questionResults = new ArrayList<>();

        for (SubmittedAnswerRequest submittedAnswerRequest : request.getAnswers()) {
            Long questionId = submittedAnswerRequest.getQuestionId();
            String userAnswerText = submittedAnswerRequest.getAnswer();

            // 查找对应的题目
            choiceQuestion question = questionMap.get(questionId);
            if (question == null) {
                System.err.println("Submitted answer for unknown questionId: " + questionId + " for jobId: " + request.getJobId());
                continue;
            }

            // 标准化答案字符串，去除首尾空格并转换为小写
            String normalizedUserAnswer = userAnswerText != null ? userAnswerText.trim().toLowerCase() : "";
            String normalizedCorrectAnswer = question.getCorrectAnswer() != null ?
                    question.getCorrectAnswer().trim().toLowerCase() : "";

            // 比较答案
            boolean isCorrect = false;
            // 移除所有空白字符后比较
            String cleanUserAnswer = normalizedUserAnswer.replaceAll("\\s+", "");
            String cleanCorrectAnswer = normalizedCorrectAnswer.replaceAll("\\s+", "");
            isCorrect = cleanUserAnswer.equals(cleanCorrectAnswer);

            // 创建答案记录
            choiceAnswer interviewAnswer = new choiceAnswer();
            interviewAnswer.setQuestionId(question.getId());
            interviewAnswer.setUserAnswer(userAnswerText);
            interviewAnswer.setIsCorrect(isCorrect);
            interviewAnswer.setScore(isCorrect ? question.getScore() : 0);
            interviewAnswers.add(interviewAnswer);

            // 创建问题结果
            InterviewSubmitResponse.QuestionResult result = new InterviewSubmitResponse.QuestionResult();
            result.setQuestionId(question.getId());
            result.setQuestion(question.getQuestion());
            result.setUserAnswer(userAnswerText);
            result.setCorrectAnswer(question.getCorrectAnswer());
            result.setIsCorrect(isCorrect);
            result.setScore(interviewAnswer.getScore());
            questionResults.add(result);

            if (isCorrect) {
                totalScore += question.getScore();
                correctCount++;
            }
        }

        // 4. 创建面试记录
        List<choiceQuestion> submittedQuestions = request.getAnswers().stream()
                .map(sa -> questionMap.get(sa.getQuestionId()))
                .filter(Objects::nonNull)
                .toList();

        choiceQuestionRecord record = new choiceQuestionRecord();
        record.setUserId(userId);
        record.setJobId(request.getJobId());
        record.setTotalScore(totalScore);
        record.setCorrectCount(correctCount);

        if (!submittedQuestions.isEmpty()) {
            record.setCorrectRate(BigDecimal.valueOf(correctCount)
                    .divide(BigDecimal.valueOf(submittedQuestions.size()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)));
        } else {
            record.setCorrectRate(BigDecimal.ZERO);
        }

        record.setStatus(2);
        recordRepository.save(record);

        // 5. 保存答案记录
        for (choiceAnswer interviewAnswer : interviewAnswers) {
            interviewAnswer.setRecordId(record.getId());
            answerRepository.save(interviewAnswer);
        }

        // 6. 返回响应
        InterviewSubmitResponse response = new InterviewSubmitResponse();
        response.setInterviewId(record.getId());
        response.setTotalScore(totalScore);
        response.setCorrectCount(correctCount);
        response.setCorrectRate(record.getCorrectRate());
        response.setQuestions(questionResults);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InterviewResultResponse getResult(Long interviewId) {
        // 1. 获取面试记录
        choiceQuestionRecord record = recordRepository.findById(interviewId);
        if (record == null) {
            throw new IllegalArgumentException("Interview record not found");
        }

        // 2. 获取该次面试的所有答案
        List<choiceAnswer> answers = answerRepository.findByRecordId(interviewId);
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("No answers found for interview record: " + interviewId);
        }

        // 3. 构建结果
        List<InterviewResultResponse.QuestionResult> questionResults = new ArrayList<>();
        for (choiceAnswer answer : answers) {
            if (answer == null) {
                continue;
            }

            // 4. 获取对应的问题
            choiceQuestion question = questionRepository.findById(answer.getQuestionId());
            if (question == null) {
                continue;
            }

            if (!answer.getIsCorrect()) {
                // 5. 构建问题结果
                InterviewResultResponse.QuestionResult result = new InterviewResultResponse.QuestionResult();
                result.setId(question.getId());
                result.setQuestion(question.getQuestion() != null ? question.getQuestion() : "");

                // 6. 处理选项
                try {
                    List<String> options = objectMapper.readValue(question.getOptions(), new TypeReference<List<String>>() {
                    });
                    result.setOptions(options != null ? options : new ArrayList<>());
                } catch (Exception e) {
                    result.setOptions(new ArrayList<>());
                }

                // 7. 设置答案相关信息
                result.setCorrectAnswer(question.getCorrectAnswer() != null ? question.getCorrectAnswer() : "");
                result.setUserAnswer(answer.getUserAnswer() != null ? answer.getUserAnswer() : "");
                result.setExplanation(question.getExplanation() != null ? question.getExplanation() : "");

                // 8. 验证必要字段
                if (result.getId() == null) {
                    continue;
                }

                questionResults.add(result);
            }
        }

        // 9. 验证结果
        if (questionResults.isEmpty()) {
            return new InterviewResultResponse();
        }

        // 10. 构建最终响应
        InterviewResultResponse response = new InterviewResultResponse();
        response.setTotalScore(record.getTotalScore() != null ? record.getTotalScore() : 0);
        response.setCorrectCount(record.getCorrectCount() != null ? record.getCorrectCount() : 0);
        response.setCorrectRate(record.getCorrectRate() != null ? record.getCorrectRate() : BigDecimal.ZERO);
        response.setQuestions(questionResults);

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<choiceQuestionRecord> getRecordByUserId(Long userId, String evaluationType) {
        return choiceQuestionRecordMapper.findByUserIdAndEvaluationType(userId, evaluationType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<choiceQuestionRecord> getRecordByUserIdAndJob(Long userId, Long jobId, String evaluationType) {
        return choiceQuestionRecordMapper.findByUserIdAndJobIdAndEvaluationType(userId, jobId, evaluationType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void addQuestion(choiceQuestion question) {
        int result = questionRepository.addQuestion(question);
        if (result == 0) {
            throw BusinessException.badRequest("添加失败");
        }
        evictQuestionCaches(question.getJobId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateQuestion(choiceQuestion question) {
        int result = questionRepository.updateQuestion(question);
        if (result == 0) {
            throw BusinessException.badRequest("更新失败");
        }
        evictQuestionCaches(question.getJobId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        choiceQuestion existing = questionRepository.findById(id);
        int result = questionRepository.deleteQuestion(id);
        if (result == 0) {
            throw BusinessException.badRequest("删除失败");
        }
        evictQuestionCaches(existing == null ? null : existing.getJobId());
    }

    private void evictQuestionCaches(Long jobId) {
        readCacheService.evict(CACHE_PREFIX + "all");
        if (jobId != null) {
            readCacheService.evict(CACHE_PREFIX + "job:" + jobId);
        } else {
            readCacheService.evictByPrefix(CACHE_PREFIX + "job:");
        }
    }
} 

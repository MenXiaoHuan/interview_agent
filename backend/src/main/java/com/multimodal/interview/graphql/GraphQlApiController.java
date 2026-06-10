package com.multimodal.interview.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.common.security.RsaPasswordCryptoService;
import com.multimodal.interview.controller.AudioTranscriptionController;
import com.multimodal.interview.controller.AuthController;
import com.multimodal.interview.controller.BlessingController;
import com.multimodal.interview.controller.ChatController;
import com.multimodal.interview.controller.ChoiceQuestionController;
import com.multimodal.interview.controller.ComprehensiveHistoryController;
import com.multimodal.interview.controller.FacialAnalysisController;
import com.multimodal.interview.controller.FeedbackController;
import com.multimodal.interview.controller.ForgotPasswordController;
import com.multimodal.interview.controller.JobController;
import com.multimodal.interview.controller.RankingController;
import com.multimodal.interview.controller.RankingSpecializedController;
import com.multimodal.interview.controller.ResumeController;
import com.multimodal.interview.controller.ScenarioAnalysisController;
import com.multimodal.interview.controller.ScenarioQuestionController;
import com.multimodal.interview.controller.SpeechSynthesisController;
import com.multimodal.interview.controller.UserController;
import com.multimodal.interview.controller.AgentController;
import com.multimodal.interview.controller.AgentConversationController;
import com.multimodal.interview.dto.AgentConversationMemoryResponse;
import com.multimodal.interview.dto.AgentConversationMessageResponse;
import com.multimodal.interview.dto.AgentConversationResponse;
import com.multimodal.interview.dto.AgentConversationUpsertRequest;
import com.multimodal.interview.dto.AvatarUpdateRequest;
import com.multimodal.interview.dto.BlessingCreateRequest;
import com.multimodal.interview.dto.BlessingResponse;
import com.multimodal.interview.dto.BlessingUpdateRequest;
import com.multimodal.interview.dto.ChatMessageResponse;
import com.multimodal.interview.dto.ChatMessageSaveRequest;
import com.multimodal.interview.dto.ComprehensiveQuestionHistoryRequest;
import com.multimodal.interview.dto.ComprehensiveReportRequest;
import com.multimodal.interview.dto.ComprehensiveReportResponse;
import com.multimodal.interview.dto.ComprehensiveResumeHistoryRequest;
import com.multimodal.interview.dto.ComprehensiveScenarioHistoryRequest;
import com.multimodal.interview.dto.EmailBindRequest;
import com.multimodal.interview.dto.FeedbackItemResponse;
import com.multimodal.interview.dto.FeedbackReplyRequest;
import com.multimodal.interview.dto.FeedbackStatusResponse;
import com.multimodal.interview.dto.FeedbackSubmitRequest;
import com.multimodal.interview.dto.ForgotResetRequest;
import com.multimodal.interview.dto.ForgotSendCodeRequest;
import com.multimodal.interview.dto.GenderUpdateRequest;
import com.multimodal.interview.dto.InterviewResultResponse;
import com.multimodal.interview.dto.InterviewSubmitRequest;
import com.multimodal.interview.dto.InterviewSubmitResponse;
import com.multimodal.interview.dto.JobCategoryCreateRequest;
import com.multimodal.interview.dto.JobCategoryUpdateRequest;
import com.multimodal.interview.dto.JobCreateRequest;
import com.multimodal.interview.dto.JobUpdateRequest;
import com.multimodal.interview.dto.LoginRequest;
import com.multimodal.interview.dto.NicknameUpdateRequest;
import com.multimodal.interview.dto.PasswordUpdateRequest;
import com.multimodal.interview.dto.PhoneBindRequest;
import com.multimodal.interview.dto.RankingResponse;
import com.multimodal.interview.dto.RegisterRequest;
import com.multimodal.interview.dto.ResetCodeResponse;
import com.multimodal.interview.dto.ResumeContentResponse;
import com.multimodal.interview.dto.SaveResumeHistoryRequest;
import com.multimodal.interview.dto.SaveScenarioAnalysisHistoryRequest;
import com.multimodal.interview.dto.StatusUpdateRequest;
import com.multimodal.interview.dto.SurpriseModeUpdateRequest;
import com.multimodal.interview.dto.UserSettingsResponse;
import com.multimodal.interview.dto.UserTypeUpdateRequest;
import com.multimodal.interview.dto.AgentRequest;
import com.multimodal.interview.entity.Blessing;
import com.multimodal.interview.entity.ChatMessage;
import com.multimodal.interview.entity.ComprehensiveQuestionHistory;
import com.multimodal.interview.entity.ComprehensiveReportHistory;
import com.multimodal.interview.entity.ComprehensiveResumeHistory;
import com.multimodal.interview.entity.ComprehensiveScenarioHistory;
import com.multimodal.interview.entity.Feedback;
import com.multimodal.interview.entity.Job;
import com.multimodal.interview.entity.JobCategory;
import com.multimodal.interview.entity.ResumeAnalysisHistory;
import com.multimodal.interview.entity.ScenarioAnalysis;
import com.multimodal.interview.entity.ScenarioQuestion;
import com.multimodal.interview.entity.SpeechSynthesis;
import com.multimodal.interview.entity.User;
import com.multimodal.interview.entity.choiceQuestion;
import com.multimodal.interview.entity.choiceQuestionRecord;
import jakarta.validation.Validator;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * GraphQL API 控制器。
 *
 * <p>该控制器仅负责将 GraphQL 查询与变更适配到现有 REST 控制器，
 * 以便最大限度复用既有业务逻辑与返回规则。</p>
 */
@Controller
public class GraphQlApiController extends GraphQlSupport {

    private final AudioTranscriptionController audioTranscriptionController;
    private final AuthController authController;
    private final BlessingController blessingController;
    private final ChatController chatController;
    private final ChoiceQuestionController choiceQuestionController;
    private final ComprehensiveHistoryController comprehensiveHistoryController;
    private final FacialAnalysisController facialAnalysisController;
    private final FeedbackController feedbackController;
    private final ForgotPasswordController forgotPasswordController;
    private final JobController jobController;
    private final RankingController rankingController;
    private final RankingSpecializedController rankingSpecializedController;
    private final RsaPasswordCryptoService rsaPasswordCryptoService;
    private final ResumeController resumeController;
    private final ScenarioAnalysisController scenarioAnalysisController;
    private final ScenarioQuestionController scenarioQuestionController;
    private final SpeechSynthesisController speechSynthesisController;
    private final UserController userController;
    private final AgentController xunfeiUltraAgentController;
    private final AgentConversationController agentConversationController;

    public GraphQlApiController(
            ObjectMapper objectMapper,
            Validator validator,
            AudioTranscriptionController audioTranscriptionController,
            AuthController authController,
            BlessingController blessingController,
            ChatController chatController,
            ChoiceQuestionController choiceQuestionController,
            ComprehensiveHistoryController comprehensiveHistoryController,
            FacialAnalysisController facialAnalysisController,
            FeedbackController feedbackController,
            ForgotPasswordController forgotPasswordController,
            JobController jobController,
            RankingController rankingController,
            RankingSpecializedController rankingSpecializedController,
            RsaPasswordCryptoService rsaPasswordCryptoService,
            ResumeController resumeController,
            ScenarioAnalysisController scenarioAnalysisController,
            ScenarioQuestionController scenarioQuestionController,
            SpeechSynthesisController speechSynthesisController,
            UserController userController,
            AgentController xunfeiUltraAgentController,
            AgentConversationController agentConversationController) {
        super(objectMapper, validator);
        this.audioTranscriptionController = audioTranscriptionController;
        this.authController = authController;
        this.blessingController = blessingController;
        this.chatController = chatController;
        this.choiceQuestionController = choiceQuestionController;
        this.comprehensiveHistoryController = comprehensiveHistoryController;
        this.facialAnalysisController = facialAnalysisController;
        this.feedbackController = feedbackController;
        this.forgotPasswordController = forgotPasswordController;
        this.jobController = jobController;
        this.rankingController = rankingController;
        this.rankingSpecializedController = rankingSpecializedController;
        this.rsaPasswordCryptoService = rsaPasswordCryptoService;
        this.resumeController = resumeController;
        this.scenarioAnalysisController = scenarioAnalysisController;
        this.scenarioQuestionController = scenarioQuestionController;
        this.speechSynthesisController = speechSynthesisController;
        this.userController = userController;
        this.xunfeiUltraAgentController = xunfeiUltraAgentController;
        this.agentConversationController = agentConversationController;
    }

    @QueryMapping
    public String rsaPublicKey() {
        return rsaPasswordCryptoService.getPublicKeyPem();
    }

    @QueryMapping
    public User me() {
        return unwrap(userController.getProfile(requireAuthentication()));
    }

    @QueryMapping
    public List<User> users(@Argument int page, @Argument int size) {
        requireAuthentication();
        return unwrap(userController.getUserList(page, size));
    }

    @QueryMapping
    public List<JobCategory> jobCategoryTree() {
        return unwrap(jobController.getCategoryTree());
    }

    @QueryMapping
    public String jobCategoryName(@Argument Long categoryId) {
        return unwrap(jobController.getCategoryNameById(categoryId));
    }

    @QueryMapping
    public List<Job> jobs(@Argument Long categoryId) {
        return unwrap(jobController.getJobs(categoryId));
    }

    @QueryMapping
    public List<Job> allJobs() {
        return unwrap(jobController.getAllJobs());
    }

    @QueryMapping
    public Job job(@Argument Long id) {
        return unwrap(jobController.getJobDetail(id));
    }

    @QueryMapping
    public List<choiceQuestion> allChoiceQuestions() {
        requireAuthentication();
        return unwrap(choiceQuestionController.getAllQuestions());
    }

    @QueryMapping
    public List<choiceQuestion> choiceQuestions(@Argument Long jobId) {
        requireAuthentication();
        return unwrap(choiceQuestionController.getQuestions(jobId));
    }

    @QueryMapping
    public InterviewResultResponse interviewResult(@Argument Long interviewQuestionRecordId) {
        requireAuthentication();
        return unwrap(choiceQuestionController.getResult(interviewQuestionRecordId));
    }

    @SuppressWarnings("unchecked")
    @QueryMapping
    public List<choiceQuestionRecord> interviewRecords(@Argument Long userId, @Argument String evaluationType) {
        requireAuthentication();
        return (List<choiceQuestionRecord>) unwrap(choiceQuestionController.getRecordByUserId(
                userId,
                evaluationType == null ? "SPECIAL" : evaluationType
        ));
    }

    @SuppressWarnings("unchecked")
    @QueryMapping
    public List<choiceQuestionRecord> interviewRecordsByJob(
            @Argument Long userId,
            @Argument Long jobId,
            @Argument String evaluationType) {
        requireAuthentication();
        return (List<choiceQuestionRecord>) unwrap(choiceQuestionController.getRecordByUserIdAndJob(
                userId,
                jobId,
                evaluationType == null ? "SPECIAL" : evaluationType
        ));
    }

    @QueryMapping
    public List<ScenarioQuestion> allScenarioQuestions() {
        requireAuthentication();
        return unwrap(scenarioQuestionController.getAll());
    }

    @QueryMapping
    public List<ScenarioQuestion> scenarioQuestions(@Argument Long jobId) {
        requireAuthentication();
        return unwrap(scenarioQuestionController.getQuestion(jobId));
    }

    @QueryMapping
    public List<ResumeAnalysisHistory> resumeAnalysisHistories(@Argument Long userId, @Argument String evaluationType) {
        requireAuthentication();
        return unwrap(resumeController.getResumeAnalysisHistoryByUserIdAndEvaluationType(
                userId,
                evaluationType == null ? "SPECIAL" : evaluationType
        ));
    }

    @QueryMapping
    public ResumeAnalysisHistory resumeAnalysisHistory(@Argument String resumeHistoryId) {
        requireAuthentication();
        return unwrap(resumeController.getResumeAnalysisHistoryByResumeHistoryId(resumeHistoryId));
    }

    @QueryMapping
    public List<ScenarioAnalysis> scenarioAnalysisHistories(@Argument String userId, @Argument String evaluationType) {
        requireAuthentication();
        return unwrap(scenarioAnalysisController.getScenarioAnalysisHistory(
                userId,
                evaluationType == null ? "SPECIAL" : evaluationType
        ));
    }

    @QueryMapping
    public ComprehensiveReportResponse comprehensiveReport(@Argument Long id) {
        requireAuthentication();
        return unwrap(comprehensiveHistoryController.getComprehensiveReport(id));
    }

    @QueryMapping
    public List<ComprehensiveReportResponse> userComprehensiveReports(@Argument Long userId) {
        requireAuthentication();
        return unwrap(comprehensiveHistoryController.getUserComprehensiveReports(userId));
    }

    @QueryMapping
    public List<FeedbackItemResponse> feedbackList() {
        return unwrap(feedbackController.getFeedbackList(requireAuthentication()));
    }

    @QueryMapping
    public Feedback feedback(@Argument Long id) {
        requireAuthentication();
        return unwrap(feedbackController.getFeedbackDetail(id));
    }

    @QueryMapping
    public List<FeedbackStatusResponse> myFeedbackStatus() {
        return unwrap(feedbackController.getMyFeedbackStatus(requireAuthentication()));
    }

    @QueryMapping
    public List<BlessingResponse> blessings(@Argument String status, @Argument String type) {
        return unwrap(blessingController.list(status, type));
    }

    @QueryMapping
    public Blessing blessing(@Argument Long id) {
        return unwrap(blessingController.get(id));
    }

    @QueryMapping
    public List<ChatMessageResponse> chatMessages(@Argument Integer days) {
        requireAuthentication();
        return unwrap(chatController.listCompat(days == null ? 1 : days));
    }

    @QueryMapping
    public List<AgentConversationResponse> agentConversations(@Argument String agentKey) {
        return unwrap(agentConversationController.listCurrentUserConversations(agentKey, requireAuthentication()));
    }

    @QueryMapping
    public List<AgentConversationMessageResponse> agentConversationMessages(@Argument String chatId) {
        return unwrap(agentConversationController.listCurrentUserConversationMessages(chatId, requireAuthentication()));
    }

    @QueryMapping
    public List<Map<String, Object>> agentConversationEvents(@Argument String chatId) {
        return unwrap(agentConversationController.listCurrentUserConversationEvents(chatId, requireAuthentication()));
    }

    @QueryMapping
    public AgentConversationMemoryResponse agentConversationMemory(@Argument String chatId) {
        return unwrap(agentConversationController.getCurrentUserConversationMemory(chatId, requireAuthentication()));
    }

    @QueryMapping
    public List<RankingResponse> comprehensiveResumeRankings(@Argument Long jobId, @Argument Integer size) {
        return unwrap(rankingController.topResume(jobId, size == null ? 50 : size));
    }

    @QueryMapping
    public List<RankingResponse> comprehensiveQuestionRankings(@Argument Long jobId, @Argument Integer size) {
        return unwrap(rankingController.topQuestion(jobId, size == null ? 50 : size));
    }

    @QueryMapping
    public List<RankingResponse> comprehensiveScenarioRankings(@Argument Long jobId, @Argument Integer size) {
        return unwrap(rankingController.topScenario(jobId, size == null ? 50 : size));
    }

    @QueryMapping
    public List<RankingResponse> specializedResumeRankings(
            @Argument Long jobId,
            @Argument String evaluationType,
            @Argument Integer size) {
        return unwrap(rankingSpecializedController.resume(jobId, evaluationType, size == null ? 50 : size));
    }

    @QueryMapping
    public List<RankingResponse> specializedQuestionRankings(
            @Argument Long jobId,
            @Argument String evaluationType,
            @Argument Integer size) {
        return unwrap(rankingSpecializedController.question(jobId, evaluationType, size == null ? 50 : size));
    }

    @QueryMapping
    public List<RankingResponse> specializedScenarioRankings(@Argument String evaluationType, @Argument Integer size) {
        return unwrap(rankingSpecializedController.scenario(evaluationType, size == null ? 50 : size));
    }

    @QueryMapping
    public ResetCodeResponse latestResetCode(@Argument String contact) {
        return unwrap(forgotPasswordController.getCodeByContact(contact));
    }

    @QueryMapping
    public String queryTranscriptionTask(@Argument String taskId) {
        return (String) unwrap(audioTranscriptionController.queryTask(taskId));
    }

    @MutationMapping
    public Map<String, Object> register(@Argument("input") RegisterRequest input) {
        return unwrap(authController.register(validate(rsaPasswordCryptoService.decrypt(input))));
    }

    @MutationMapping
    public Map<String, Object> login(@Argument("input") LoginRequest input) {
        return unwrap(authController.login(validate(rsaPasswordCryptoService.decrypt(input))));
    }

    @MutationMapping
    public Boolean sendResetCode(@Argument("input") ForgotSendCodeRequest input) {
        return unwrapSuccess(forgotPasswordController.sendCode(validate(input)));
    }

    @MutationMapping
    public Boolean resetPassword(@Argument("input") ForgotResetRequest input) {
        return unwrapSuccess(forgotPasswordController.reset(validate(rsaPasswordCryptoService.decrypt(input))));
    }

    @MutationMapping
    public UserSettingsResponse updateEyeMode(@Argument("input") UserSettingsResponse input) {
        return unwrap(userController.updateEyeMode(validate(input), requireAuthentication()));
    }

    @MutationMapping
    public Map<String, Object> updateSurpriseMode(@Argument("input") SurpriseModeUpdateRequest input) {
        return unwrap(userController.updateSurpriseMode(validate(input), requireAuthentication()));
    }

    @MutationMapping
    public Map<String, Object> updateNickname(@Argument("input") NicknameUpdateRequest input) {
        return unwrap(userController.updateNickname(validate(input), requireAuthentication()));
    }

    @MutationMapping
    public Boolean updateAvatar(@Argument("input") AvatarUpdateRequest input) {
        return unwrapSuccess(userController.updateAvatar(validate(input), requireAuthentication()));
    }

    @MutationMapping
    public Boolean updatePassword(@Argument("input") PasswordUpdateRequest input) {
        return unwrapSuccess(userController.updatePassword(validate(rsaPasswordCryptoService.decrypt(input)), requireAuthentication()));
    }

    @MutationMapping
    public Boolean bindEmail(@Argument("input") EmailBindRequest input) {
        return unwrapSuccess(userController.bindEmail(validate(input), requireAuthentication()));
    }

    @MutationMapping
    public Boolean bindPhone(@Argument("input") PhoneBindRequest input) {
        return unwrapSuccess(userController.bindPhone(validate(input), requireAuthentication()));
    }

    @MutationMapping
    public Boolean updateStatus(@Argument("input") StatusUpdateRequest input) {
        return unwrapSuccess(userController.updateStatus(validate(input), requireAuthentication()));
    }

    @MutationMapping
    public Boolean updateUserType(@Argument("input") UserTypeUpdateRequest input) {
        return unwrapSuccess(userController.updateUserType(validate(input), requireAuthentication()));
    }

    @MutationMapping
    public Boolean updateGender(@Argument("input") GenderUpdateRequest input) {
        return unwrapSuccess(userController.updateGender(validate(input), requireAuthentication()));
    }

    @MutationMapping
    public Map<String, Object> adminUpdateUserNickname(@Argument String username, @Argument String nickname) {
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        return unwrap(userController.updateNicknameByUsername(user));
    }

    @MutationMapping
    public Map<String, Object> adminUpdateUserEmail(@Argument String username, @Argument String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return unwrap(userController.updateEmailByUsername(user));
    }

    @MutationMapping
    public Map<String, Object> adminUpdateUserPhone(@Argument String username, @Argument String phone) {
        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        return unwrap(userController.updatePhoneByUsername(user));
    }

    @MutationMapping
    public Map<String, Object> adminUpdateUserGender(@Argument String username, @Argument Integer gender) {
        User user = new User();
        user.setUsername(username);
        user.setGender(gender);
        return unwrap(userController.updateGenderByUsername(user));
    }

    @MutationMapping
    public Map<String, Object> adminUpdateUserType(@Argument String username, @Argument Integer userType) {
        User user = new User();
        user.setUsername(username);
        user.setUserType(userType);
        return unwrap(userController.updateUserTypeByUsername(user));
    }

    @MutationMapping
    public Map<String, Object> adminUpdateUserStatus(@Argument String username, @Argument Integer status) {
        User user = new User();
        user.setUsername(username);
        user.setStatus(status);
        return unwrap(userController.updateStatusByUsername(user));
    }

    @MutationMapping
    public Boolean deleteUser(@Argument String username) {
        return unwrapSuccess(userController.deleteUser(username));
    }

    @MutationMapping
    public JobCategory createFirstLevelCategory(@Argument("input") JobCategoryCreateRequest input) {
        return unwrap(jobController.createFirstLevelCategory(validate(input)));
    }

    @MutationMapping
    public JobCategory createSecondLevelCategory(@Argument("input") JobCategoryCreateRequest input) {
        return unwrap(jobController.createSecondLevelCategory(validate(input)));
    }

    @MutationMapping
    public JobCategory updateCategory(@Argument("input") JobCategoryUpdateRequest input) {
        return unwrap(jobController.updateCategory(validate(input)));
    }

    @MutationMapping
    public Boolean deleteFirstLevelCategory(@Argument Long categoryId) {
        return unwrapSuccess(jobController.deleteFirstLevelCategory(categoryId));
    }

    @MutationMapping
    public Boolean deleteSecondLevelCategory(@Argument Long categoryId) {
        return unwrapSuccess(jobController.deleteSecondLevelCategory(categoryId));
    }

    @MutationMapping
    public Job createJob(@Argument("input") JobCreateRequest input) {
        return unwrap(jobController.createJob(validate(input)));
    }

    @MutationMapping
    public Job updateJob(@Argument("input") JobUpdateRequest input) {
        return unwrap(jobController.updateJob(validate(input)));
    }

    @MutationMapping
    public Boolean deleteJob(@Argument Long jobId) {
        return unwrapSuccess(jobController.deleteJob(jobId));
    }

    @MutationMapping
    public String addChoiceQuestion(@Argument("input") choiceQuestion input) {
        return unwrap(choiceQuestionController.addQuestion(input));
    }

    @MutationMapping
    public String updateChoiceQuestion(@Argument("input") choiceQuestion input) {
        return unwrap(choiceQuestionController.updateQuestion(input));
    }

    @MutationMapping
    public Boolean deleteChoiceQuestion(@Argument Long id) {
        unwrap(choiceQuestionController.deleteQuestion(id));
        return true;
    }

    @MutationMapping
    public InterviewSubmitResponse submitInterviewAnswers(@Argument("input") InterviewSubmitRequest input) {
        return unwrap(choiceQuestionController.submitAnswers(requireUserDetails(), input));
    }

    @MutationMapping
    public String createScenarioQuestion(@Argument("input") ScenarioQuestion input) {
        return unwrap(scenarioQuestionController.createScenarioQuestion(input));
    }

    @MutationMapping
    public String updateScenarioQuestion(@Argument("input") ScenarioQuestion input) {
        return unwrap(scenarioQuestionController.updateQuestion(input));
    }

    @MutationMapping
    public Boolean deleteScenarioQuestion(@Argument Long id) {
        unwrap(scenarioQuestionController.deleteScenarioQuestion(id));
        return true;
    }

    @MutationMapping
    public String extractResumeContent(@Argument MultipartFile file) {
        return unwrap(resumeController.extractResumeContent(file));
    }

    @MutationMapping
    public String saveResumeAnalysisHistory(@Argument("input") SaveResumeHistoryRequest input, @Argument String evaluationType) {
        return unwrap(resumeController.saveResumeAnalysisHistory(
                input,
                evaluationType == null ? "SPECIAL" : evaluationType
        ));
    }

    @MutationMapping
    public ResumeContentResponse restoreResume(@Argument String resumeHistoryId) {
        return unwrap(resumeController.restoreResume(resumeHistoryId));
    }

    @MutationMapping
    public Boolean saveScenarioAnalysisHistory(@Argument("input") SaveScenarioAnalysisHistoryRequest input,
                                               @Argument String evaluationType) {
        unwrap(scenarioAnalysisController.saveScenarioAnalysisHistory(
                input,
                evaluationType == null ? "SPECIAL" : evaluationType
        ));
        return true;
    }

    @MutationMapping
    public ComprehensiveResumeHistory saveComprehensiveResumeHistory(@Argument("input") ComprehensiveResumeHistoryRequest input) {
        return unwrap(comprehensiveHistoryController.saveResumeHistory(input));
    }

    @MutationMapping
    public ComprehensiveQuestionHistory saveComprehensiveQuestionHistory(@Argument("input") ComprehensiveQuestionHistoryRequest input) {
        return unwrap(comprehensiveHistoryController.saveQuestionHistory(input));
    }

    @MutationMapping
    public ComprehensiveScenarioHistory saveComprehensiveScenarioHistory(@Argument("input") ComprehensiveScenarioHistoryRequest input) {
        return unwrap(comprehensiveHistoryController.saveScenarioHistory(input));
    }

    @MutationMapping
    public ComprehensiveReportHistory saveComprehensiveReport(@Argument("input") ComprehensiveReportRequest input) {
        return unwrap(comprehensiveHistoryController.saveComprehensiveReport(input));
    }

    @MutationMapping
    public Feedback submitFeedback(@Argument("input") FeedbackSubmitRequest input) {
        return unwrap(feedbackController.submitFeedback(validate(input), requireAuthentication()));
    }

    @MutationMapping
    public Feedback replyFeedback(@Argument("input") FeedbackReplyRequest input) {
        return unwrap(feedbackController.replyFeedback(input.getId(), validate(input), requireAuthentication()));
    }

    @MutationMapping
    public Blessing createBlessing(@Argument("input") BlessingCreateRequest input) {
        return unwrap(blessingController.create(validate(input), requireAuthentication()));
    }

    @MutationMapping
    public Blessing updateBlessing(@Argument Long id, @Argument("input") BlessingUpdateRequest input) {
        return unwrap(blessingController.update(id, input, requireAuthentication()));
    }

    @MutationMapping
    public Boolean deleteBlessing(@Argument Long id) {
        return unwrapSuccess(blessingController.delete(id, requireAuthentication()));
    }

    @MutationMapping
    public ChatMessage saveChatMessage(@Argument("input") ChatMessageSaveRequest input) {
        return unwrap(chatController.save(validate(input), requireAuthentication()));
    }

    @MutationMapping
    public AgentConversationResponse upsertAgentConversation(@Argument("input") AgentConversationUpsertRequest input) {
        return unwrap(agentConversationController.upsertCurrentUserConversation(validate(input), requireAuthentication()));
    }

    @MutationMapping
    public Boolean deleteAgentConversation(@Argument String chatId) {
        return unwrapSuccess(agentConversationController.deleteCurrentUserConversation(chatId, requireAuthentication()));
    }

    @MutationMapping
    public String synthesizeSpeech(@Argument("input") SpeechSynthesis input) {
        return unwrap(speechSynthesisController.synthesizeSpeech(input));
    }

    @MutationMapping
    public String analyzeFace(@Argument MultipartFile file) throws Exception {
        return unwrap(facialAnalysisController.getFacialAnalysis(file));
    }

    @MutationMapping
    public String uploadAudio(@Argument MultipartFile file) {
        return (String) unwrap(audioTranscriptionController.uploadAudio(file));
    }

    @MutationMapping
    public String createTranscriptionTask(@Argument String audioUrl) {
        return (String) unwrap(audioTranscriptionController.createTask(audioUrl));
    }

    /**
     * GraphQL 入口下的统一 AI / Agent 调用。
     *
     * <p>这里不直接操作模型，而是复用 REST 侧的 {@link AgentController}，
     * 以保证 REST 与 GraphQL 两条入口最终共享同一套 Agent 路由、结构化输出和异常处理逻辑。</p>
     */
    @MutationMapping
    public Object getAgentAnswer(@Argument("input") AgentRequest input) {
        return unwrap(xunfeiUltraAgentController.getAgentAnswer(input));
    }

    @SchemaMapping(typeName = "ChoiceQuestion", field = "options")
    public List<String> choiceQuestionOptions(choiceQuestion question) {
        return parseOptions(question.getOptions());
    }

    @SchemaMapping(typeName = "InterviewResultQuestion", field = "options")
    public List<String> interviewResultQuestionOptions(InterviewResultResponse.QuestionResult question) {
        return question.getOptions();
    }
}

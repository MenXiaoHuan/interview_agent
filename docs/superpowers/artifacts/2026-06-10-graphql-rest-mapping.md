# GraphQL -> REST Mapping

## 说明

- 来源文件：`project/src/utils/graphql-bridge.js`
- 目标：列出当前前端“REST 风格调用”在桥接层对应的 GraphQL operation、后端真实 REST 落点，以及迁移风险与状态
- 状态说明：
  - `done`：已有真实 REST 落点，且路径与返回结构基本可直连
  - `verify`：已有真实 REST 落点，但返回结构、鉴权或参数仍需验证
  - `create`：当前仅 GraphQL 可用，需要补新的 REST 能力

## 认证与用户

| Method | Frontend URL | GraphQL Operation | Backend REST Target | Gap | Status |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/auth/rsa-public-key` | `rsaPublicKey` | `AuthController#getRsaPublicKey` | 已新增独立 REST 公钥接口 | done |
| POST | `/api/auth/login` | `login` | `AuthController#login` | 无 | done |
| POST | `/api/auth/register` | `register` | `AuthController#register` | 无 | done |
| POST | `/api/auth/forgot/send-code` | `sendResetCode` | `ForgotPasswordController#sendCode` | 无 | done |
| POST | `/api/auth/forgot/reset` | `resetPassword` | `ForgotPasswordController#reset` | 无 | done |
| GET | `/api/auth/forgot/code` | `latestResetCode` | `ForgotPasswordController#getCodeByContact` | 无 | done |
| GET | `/api/auth/forgot/email-code` | `latestResetCode` | `ForgotPasswordController#getCodeByEmail` | 无 | done |
| GET | `/api/auth/forgot/phone-code` | `latestResetCode` | `ForgotPasswordController#getCodeByPhone` | 无 | done |
| GET | `/api/user/profile` | `me` | `UserController#getProfile` | 无 | done |
| PUT | `/api/user/eyemode` | `updateEyeMode` | `UserController#updateEyeMode` | 无 | done |
| PUT | `/api/user/surprisemode` | `updateSurpriseMode` | `UserController#updateSurpriseMode` | 无 | done |
| PUT | `/api/user/nickname` | `updateNickname` | `UserController#updateNickname` | 无 | done |
| POST | `/api/user/avatar/upload` | `updateAvatar` | `UserController#updateAvatar` / `UserController#uploadAvatarFile` | 需确认前端当前到底走 JSON 还是 multipart | verify |
| POST | `/api/user/avatar` | `updateAvatar` | `UserController#updateAvatar` | `/api/user/avatar` 当前未见显式 REST 映射，需核对是否统一到 `/avatar/upload` | verify |
| PUT | `/api/user/password` | `updatePassword` | `UserController#updatePassword` | 无 | done |
| PUT | `/api/user/email` | `bindEmail` | `UserController#bindEmail` | 无 | done |
| PUT | `/api/user/phone` | `bindPhone` | `UserController#bindPhone` | 无 | done |
| PUT | `/api/user/status` | `updateStatus` | `UserController#updateStatus` | 无 | done |
| PUT | `/api/user/userType` | `updateUserType` | `UserController#updateUserType` | 无 | done |
| PUT | `/api/user/gender` | `updateGender` | `UserController#updateGender` | 无 | done |
| GET | `/api/user/list` | `users` | `UserController#getUserList` | 无 | done |
| DELETE | `/api/user/delete/{username}` | `deleteUser` | `UserController#deleteUser` | 无 | done |
| PUT | `/api/user/update/nickname` | `adminUpdateUserNickname` | `UserController#updateNicknameByUsername` | 无 | done |
| PUT | `/api/user/update/email` | `adminUpdateUserEmail` | `UserController#updateEmailByUsername` | 需核对方法名与请求体结构 | verify |
| PUT | `/api/user/update/phone` | `adminUpdateUserPhone` | `UserController#updatePhoneByUsername` | 需核对方法名与请求体结构 | verify |
| PUT | `/api/user/update/gender` | `adminUpdateUserGender` | `UserController#updateGenderByUsername` | 需核对方法名与请求体结构 | verify |
| PUT | `/api/user/update/userType` | `adminUpdateUserType` | `UserController#updateUserTypeByUsername` | 需核对方法名与请求体结构 | verify |
| PUT | `/api/user/update/status` | `adminUpdateUserStatus` | `UserController#updateStatusByUsername` | 需核对方法名与请求体结构 | verify |

## 岗位与分类

| Method | Frontend URL | GraphQL Operation | Backend REST Target | Gap | Status |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/job-categories/tree` | `jobCategoryTree` | `JobController#getCategoryTree` | 无 | done |
| GET | `/api/job-categories/{categoryId}/name` | `jobCategoryName` | `JobController#getCategoryNameById` | 无 | done |
| POST | `/api/job-categories/first-level` | `createFirstLevelCategory` | `JobController#createFirstLevelCategory` | 无 | done |
| POST | `/api/job-categories/second-level` | `createSecondLevelCategory` | `JobController#createSecondLevelCategory` | 无 | done |
| PUT | `/api/job-categories` | `updateCategory` | `JobController#updateCategory` | 无 | done |
| DELETE | `/api/job-categories/first-level/{categoryId}` | `deleteFirstLevelCategory` | `JobController#deleteFirstLevelCategory` | 无 | done |
| DELETE | `/api/job-categories/second-level/{categoryId}` | `deleteSecondLevelCategory` | `JobController#deleteSecondLevelCategory` | 无 | done |
| GET | `/api/job?categoryId=...` | `jobs` | `JobController#getJobs` | 无 | done |
| GET | `/api/job/{id}` | `job` | `JobController#getJobDetail` | 无 | done |
| POST | `/api/jobs` | `createJob` | `JobController#createJob` | 无 | done |
| PUT | `/api/jobs` | `updateJob` | `JobController#updateJob` | 无 | done |
| GET | `/api/jobs` | `allJobs` | `JobController#getAllJobs` | 无 | done |
| DELETE | `/api/jobs/{jobId}` | `deleteJob` | `JobController#deleteJob` | 无 | done |

## 选择题与场景题

| Method | Frontend URL | GraphQL Operation | Backend REST Target | Gap | Status |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/interview/question/getAll` | `allChoiceQuestions` | `ChoiceQuestionController#getAllQuestions` | 无 | done |
| GET | `/api/interview/question/{jobId}` | `choiceQuestions` | `ChoiceQuestionController#getQuestions` | 无 | done |
| POST | `/api/interview/question/add` | `addChoiceQuestion` | `ChoiceQuestionController#addQuestion` | 无 | done |
| PUT | `/api/interview/question/update` | `updateChoiceQuestion` | `ChoiceQuestionController#updateQuestion` | 无 | done |
| POST | `/api/interview/question/delete/{id}` | `deleteChoiceQuestion` | `ChoiceQuestionController#deleteQuestion` | 历史接口仍为 `POST` 删除，后续可二阶段规范 | done |
| POST | `/api/interview/submit` | `submitInterviewAnswers` | `ChoiceQuestionController#submitAnswers` | 无 | done |
| GET | `/api/interview/result/{interviewQuestionRecordId}` | `interviewResult` | `ChoiceQuestionController#getResult` | 无 | done |
| GET | `/api/interview/question/history/{userId}` | `interviewRecords` | `ChoiceQuestionController#getRecordByUserId` | 无 | done |
| GET | `/api/interview/question/history/{userId}/job/{jobId}` | `interviewRecordsByJob` | `ChoiceQuestionController#getRecordByUserIdAndJob` | 无 | done |
| GET | `/api/scenario-question/getAll` | `allScenarioQuestions` | `ScenarioQuestionController#getAll` | 无 | done |
| GET | `/api/scenario-question/get/{jobId}` | `scenarioQuestions` | `ScenarioQuestionController#getQuestion` | 无 | done |
| POST | `/api/scenario-question/create` | `createScenarioQuestion` | `ScenarioQuestionController#createScenarioQuestion` | 无 | done |
| POST | `/api/scenario-question/update` | `updateScenarioQuestion` | `ScenarioQuestionController#updateQuestion` | 历史接口为 `POST` 更新，后续可再规范 | done |
| POST | `/api/scenario-question/delete/{id}` | `deleteScenarioQuestion` | `ScenarioQuestionController#deleteScenarioQuestion` | 历史接口为 `POST` 删除，后续可再规范 | done |

## 简历、场景分析与综合评估

| Method | Frontend URL | GraphQL Operation | Backend REST Target | Gap | Status |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/resume/extract` | `extractResumeContent` | `ResumeController#extractResumeContent` | 无 | done |
| POST | `/api/resume/save/history` | `saveResumeAnalysisHistory` | `ResumeController#saveResumeAnalysisHistory` | 无 | done |
| GET | `/api/resume/history/{userId}` | `resumeAnalysisHistories` | `ResumeController#getResumeAnalysisHistoryByUserIdAndEvaluationType` | 无 | done |
| GET | `/api/resume/history/id/{resumeHistoryId}` | `resumeAnalysisHistory` | `ResumeController#getResumeAnalysisHistoryByResumeHistoryId` | 无 | done |
| POST | `/api/resume/restore/{resumeHistoryId}` | `restoreResume` | `ResumeController#restoreResume` | 无 | done |
| POST | `/scenario/save/history` | `saveScenarioAnalysisHistory` | `ScenarioAnalysisController#saveScenarioAnalysisHistory` | 无 | done |
| GET | `/scenario/history/{userId}` | `scenarioAnalysisHistories` | `ScenarioAnalysisController#getScenarioAnalysisHistory` | 无 | done |
| POST | `/api/comprehensive-history/resume` | `saveComprehensiveResumeHistory` | `ComprehensiveHistoryController#saveResumeHistory` | 无 | done |
| POST | `/api/comprehensive-history/question` | `saveComprehensiveQuestionHistory` | `ComprehensiveHistoryController#saveQuestionHistory` | 无 | done |
| POST | `/api/comprehensive-history/scenario` | `saveComprehensiveScenarioHistory` | `ComprehensiveHistoryController#saveScenarioHistory` | 无 | done |
| POST | `/api/comprehensive-history/report` | `saveComprehensiveReport` | `ComprehensiveHistoryController#saveComprehensiveReport` | 无 | done |
| GET | `/api/comprehensive-history/report/{id}` | `comprehensiveReport` | `ComprehensiveHistoryController#getComprehensiveReport` | 无 | done |
| GET | `/api/comprehensive-history/reports/user/{userId}` | `userComprehensiveReports` | `ComprehensiveHistoryController#getUserComprehensiveReports` | 无 | done |

## 反馈、祝福与聊天

| Method | Frontend URL | GraphQL Operation | Backend REST Target | Gap | Status |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/feedback/submit` | `submitFeedback` | `FeedbackController#submitFeedback` | 无 | done |
| GET | `/api/feedback` | `feedbackList` | `FeedbackController#getFeedbackList` | 无 | done |
| GET | `/api/feedback/{id}` | `feedback` | `FeedbackController#getFeedbackDetail` | 无 | done |
| POST | `/api/feedback/{id}/reply` | `replyFeedback` | `FeedbackController#replyFeedback` | 无 | done |
| GET | `/api/feedback/my/status` | `myFeedbackStatus` | `FeedbackController#getMyFeedbackStatus` | 无 | done |
| GET | `/api/blessings` | `blessings` | `BlessingController#list` | 无 | done |
| POST | `/api/blessings` | `createBlessing` | `BlessingController#create` | 无 | done |
| GET | `/api/blessings/{id}` | `blessing` | `BlessingController#get` | 无 | done |
| PUT | `/api/blessings/{id}` | `updateBlessing` | `BlessingController#update` | 无 | done |
| DELETE | `/api/blessings/{id}` | `deleteBlessing` | `BlessingController#delete` | 无 | done |
| POST | `/api/chat/message` | `saveChatMessage` | `ChatController#save` | 无 | done |
| GET | `/api/chat/messages?days=1` | `chatMessages` | `ChatController#listCompat` | 无 | done |

## Agent、会话与排行榜

| Method | Frontend URL | GraphQL Operation | Backend REST Target | Gap | Status |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/xunfei/getAgentAnswer` | `getAgentAnswer` | `AgentController#getAgentAnswer` | 无 | done |
| GET | `/api/agent-conversations` | `agentConversations` | `AgentConversationController#listCurrentUserConversations` | 无 | done |
| POST | `/api/agent-conversations/session` | `upsertAgentConversation` | `AgentConversationController#upsertCurrentUserConversation` | 无 | done |
| DELETE | `/api/agent-conversations/{chatId}` | `deleteAgentConversation` | `AgentConversationController#deleteCurrentUserConversation` | 无 | done |
| GET | `/api/rank/comprehensive/resume` | `comprehensiveResumeRankings` | `RankingController#topResume` | 无 | done |
| GET | `/api/rank/comprehensive/question` | `comprehensiveQuestionRankings` | `RankingController#topQuestion` | 无 | done |
| GET | `/api/rank/comprehensive/scenario` | `comprehensiveScenarioRankings` | `RankingController#topScenario` | 无 | done |
| GET | `/api/rank/specialized/resume` | `specializedResumeRankings` | `RankingSpecializedController#resume` | 无 | done |
| GET | `/api/rank/specialized/question` | `specializedQuestionRankings` | `RankingSpecializedController#question` | 无 | done |
| GET | `/api/rank/specialized/scenario` | `specializedScenarioRankings` | `RankingSpecializedController#scenario` | 无 | done |

## 语音、转写与人脸分析

| Method | Frontend URL | GraphQL Operation | Backend REST Target | Gap | Status |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/speech/synthesize` | `synthesizeSpeech` | `SpeechSynthesisController#synthesizeSpeech` | 无 | done |
| POST | `/api/transcription/upload` | `uploadAudio` | `AudioTranscriptionController#uploadAudio` | 现有 REST 返回 `ResponseEntity<String>`，需确认前端是否继续按 `ApiResponse` 消费 | verify |
| POST | `/api/transcription/create-task` | `createTranscriptionTask` | `AudioTranscriptionController#createTask` | 现有 REST 通过 `@RequestParam("audioUrl")` 接收，需确认前端 `FormData` 兼容性 | verify |
| GET | `/api/transcription/query-task/{taskId}` | `queryTranscriptionTask` | `AudioTranscriptionController#queryTask` | 现有 REST 返回 `ResponseEntity<String>`，需确认前端解析方式 | verify |
| POST | `/api/face/getAnalysis` | `analyzeFace` | `FacialAnalysisController#getFacialAnalysis` | 无 | done |

## 额外说明

### 当前剩余观察项

1. `AudioTranscriptionController` 当前返回原始字符串 `ResponseEntity<String>`，但前端请求层已兼容包装；如后续希望接口完全统一，可再收敛为 `ApiResponse`
2. `POST` 风格的历史删除/更新接口目前仍保留，为保证页面最小改动未在本次迁移中统一改成更严格的 REST 语义

### 当前可直接复用的 REST 资源

- 认证与用户
- 岗位与分类
- 选择题与场景题
- 简历分析与综合评估
- 反馈、祝福、聊天
- Agent 会话
- 排行榜

### 删除 GraphQL 前的检查项

- 所有表格中 `create` 项全部消除
- 所有 `verify` 项完成接口/页面回归
- 浏览器网络中 `/graphql` 请求归零
- 后端安全配置移除 `/graphql` 放行
- Maven 依赖和 `schema.graphqls` 已删除

## 本次执行结果

- 已新增 `GET /api/auth/rsa-public-key`
- 已移除前端 `graphql-bridge.js`，并通过 `npm run build:h5`
- 已删除后端 GraphQL 依赖、配置、schema 与运行时代码
- 已通过 `./mvnw test`
- 已通过 `./mvnw -q -DskipTests package`
- 已通过 `PLATFORM_SERVER_PORT=18442 ./mvnw spring-boot:run` 启动验证
- 已通过 `curl -k https://localhost:18442/api/auth/rsa-public-key`
- 当前源码中仅测试文件仍保留 `/graphql` 字面量，用于验证匿名访问被拒绝

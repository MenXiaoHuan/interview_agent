package com.multimodal.interview.reactagent;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import com.multimodal.interview.controller.ChoiceQuestionController;
import com.multimodal.interview.controller.JobController;
import com.multimodal.interview.controller.ResumeController;
import com.multimodal.interview.reactagent.output.AgentStructuredOutputs;
import com.multimodal.interview.reactagent.skill.SingleSkillRegistry;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ReactAgent Spring Boot 配置入口。
 *
 * <p>本项目的约定：</p>
 * <p>
 * - 一个业务能力 = 一个 Agent（ReactAgent Bean），通过 agentKey 进行路由
 * </p>
 * <p>
 * - 一个 Agent 只绑定一个同名 Skill（skill name == agentKey），避免把所有技能一次性暴露给模型
 * </p>
 * <p>
 * - Skill 的加载与 read_skill 工具由官方 {@link SkillsAgentHook} 提供，满足 “渐进式披露”：
 *   系统提示里只注入技能列表（本项目每个 Agent 仅 1 条），需要时再调用 read_skill(skill_name) 读取 SKILL.md 全文
 * </p>
 */
@Configuration
public class ReactAgentConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(
            @Value("${spring.data.redis.host:127.0.0.1}") String host,
            @Value("${spring.data.redis.port:6379}") int port,
            @Value("${spring.data.redis.password:}") String password,
            @Value("${spring.data.redis.database:0}") int database
    ) {
        Config config = new Config();
        String address = "redis://" + host + ":" + port;
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database);
        if (password != null && !password.isBlank()) {
            config.useSingleServer().setPassword(password);
        }
        return Redisson.create(config);
    }

    @Bean
    public SkillRegistry rootSkillRegistry() {
        // 从 classpath:skills 扫描 skills/*/SKILL.md，目录结构需满足 skills 教程规范
        return ClasspathSkillRegistry.builder()
                .classpathPath("skills")
                .build();
    }

    @Bean("resume-analysis")
    public ReactAgent resumeAnalysisAgent(
            ChatModel model,
            SkillRegistry rootSkillRegistry,
            JobController jobController,
            ResumeController resumeController,
            ChoiceQuestionController choiceQuestionController
    ) {
        return buildStructured("resume-analysis", "简历评测", model, rootSkillRegistry,
                AgentStructuredOutputs.ResumeAnalysisResult.class,
                jobController, resumeController, choiceQuestionController);
    }

    @Bean("scenario-evaluation")
    public ReactAgent scenarioInterviewEvaluationAgent(
            ChatModel model,
            SkillRegistry rootSkillRegistry,
            JobController jobController,
            ResumeController resumeController,
            ChoiceQuestionController choiceQuestionController
    ) {
        return buildStructured("scenario-evaluation", "场景面试评测", model, rootSkillRegistry,
                AgentStructuredOutputs.ScenarioEvaluationResult.class,
                jobController, resumeController, choiceQuestionController);
    }

    @Bean("comprehensive-report")
    public ReactAgent comprehensiveReportAgent(
            ChatModel model,
            SkillRegistry rootSkillRegistry,
            JobController jobController,
            ResumeController resumeController,
            ChoiceQuestionController choiceQuestionController
    ) {
        return buildStructured("comprehensive-report", "综合报告生成", model, rootSkillRegistry,
                AgentStructuredOutputs.ComprehensiveReportResult.class,
                jobController, resumeController, choiceQuestionController);
    }

    @Bean("interview-assistant")
    public ReactAgent interviewAssistantAgent(
            ChatModel model,
            SkillRegistry rootSkillRegistry,
            JobController jobController,
            ResumeController resumeController,
            ChoiceQuestionController choiceQuestionController
    ) {
        return buildStructured("interview-assistant", "面试对话助手", model, rootSkillRegistry,
                AgentStructuredOutputs.InterviewAssistantResult.class,
                jobController, resumeController, choiceQuestionController);
    }

    @Bean("scenario-question-gen")
    public ReactAgent scenarioQuestionGenerationAgent(
            ChatModel model,
            SkillRegistry rootSkillRegistry,
            JobController jobController,
            ResumeController resumeController,
            ChoiceQuestionController choiceQuestionController
    ) {
        return buildStructured("scenario-question-gen", "场景题生成", model, rootSkillRegistry,
                AgentStructuredOutputs.ScenarioQuestionGenerationResult.class,
                jobController, resumeController, choiceQuestionController);
    }

    @Bean("scenario-audio-evaluation")
    public ReactAgent scenarioAudioEvaluationAgent(
            ChatModel model,
            SkillRegistry rootSkillRegistry,
            JobController jobController,
            ResumeController resumeController,
            ChoiceQuestionController choiceQuestionController
    ) {
        return buildStructured("scenario-audio-evaluation", "场景语音评测", model, rootSkillRegistry,
                AgentStructuredOutputs.ScenarioAudioEvaluationResult.class,
                jobController, resumeController, choiceQuestionController);
    }

    @Bean("comprehensive-resume-analysis")
    public ReactAgent comprehensiveResumeAnalysisAgent(
            ChatModel model,
            SkillRegistry rootSkillRegistry,
            JobController jobController,
            ResumeController resumeController,
            ChoiceQuestionController choiceQuestionController
    ) {
        return buildStructured("comprehensive-resume-analysis", "综合简历评测", model, rootSkillRegistry,
                AgentStructuredOutputs.ComprehensiveResumeAnalysisResult.class,
                jobController, resumeController, choiceQuestionController);
    }

    @Bean("comprehensive-question-generation")
    public ReactAgent comprehensiveQuestionGenerationAgent(
            ChatModel model,
            SkillRegistry rootSkillRegistry,
            JobController jobController,
            ResumeController resumeController,
            ChoiceQuestionController choiceQuestionController
    ) {
        return buildStructured("comprehensive-question-generation", "综合题库生成", model, rootSkillRegistry,
                AgentStructuredOutputs.ComprehensiveQuestionGenerationResult.class,
                jobController, resumeController, choiceQuestionController);
    }

    @Bean("scenario-question-scoring")
    public ReactAgent scenarioQuestionScoringAgent(
            ChatModel model,
            SkillRegistry rootSkillRegistry,
            JobController jobController,
            ResumeController resumeController,
            ChoiceQuestionController choiceQuestionController
    ) {
        return buildStructured("scenario-question-scoring", "场景题评分", model, rootSkillRegistry,
                AgentStructuredOutputs.ScenarioQuestionScoringResult.class,
                jobController, resumeController, choiceQuestionController);
    }

    @Bean("question-analysis")
    public ReactAgent questionAnalysisAgent(
            ChatModel model,
            SkillRegistry rootSkillRegistry,
            JobController jobController,
            ResumeController resumeController,
            ChoiceQuestionController choiceQuestionController
    ) {
        return buildStructured("question-analysis", "题目解析", model, rootSkillRegistry,
                AgentStructuredOutputs.QuestionAnalysisResult.class,
                jobController, resumeController, choiceQuestionController);
    }

    private SkillsAgentHook skillsHookFor(SkillRegistry rootSkillRegistry, String skillName) {
        // 每个 agent 只允许看到一个 skill：skillName
        SkillRegistry perAgentRegistry = new SingleSkillRegistry(rootSkillRegistry, skillName);
        return SkillsAgentHook.builder()
                .skillRegistry(perAgentRegistry)
                .build();
    }

    /**
     * 基于 Java 输出类型生成结构化输出 Schema。
     *
     * <p>这里使用 {@link BeanOutputConverter} 将 POJO 转成 JSON Schema，
     * 再交给 ReactAgent 的 {@code outputSchema(...)} 约束模型输出。
     * 这样可以把“输出格式”从 prompt 文案里抽离出来，交给类型系统统一维护。</p>
     */
    private String outputSchemaFor(Class<?> outputType) {
        return new BeanOutputConverter<>(outputType).getFormat();
    }

    /**
     * 构建项目中默认使用的结构化 ReactAgent。
     *
     * <p>本项目几乎所有 AI 能力都采用这套模板装配：</p>
     * <p>
     * - 一个 agentKey 对应一个 ReactAgent Bean
     * </p>
     * <p>
     * - 一个 Agent 只允许读取同名 skill
     * </p>
     * <p>
     * - 使用 outputSchema 强约束模型输出结构
     * </p>
     * <p>
     * - 将 Controller Bean 作为 method tools 暴露给模型调用
     * </p>
     *
     * @param agentKey agent 唯一标识，同时也是 skill 名称
     * @param description agent 业务描述
     * @param model Spring AI 注入的底层 ChatModel
     * @param rootSkillRegistry 全量 skill 注册表
     * @param outputType 结构化输出对应的 Java 类型
     * @param methodToolBeans 可暴露给 Agent 的 method tools 宿主 Bean
     * @return 已装配完成的 ReactAgent
     */
    private ReactAgent buildStructured(
            String agentKey,
            String description,
            ChatModel model,
            SkillRegistry rootSkillRegistry,
            Class<?> outputType,
            Object... methodToolBeans
    ) {
        SkillsAgentHook skillsHook = skillsHookFor(rootSkillRegistry, agentKey);

        String instruction = ""
                + "你是一个后端内置的业务智能体（" + description + "）。\n"
                + "当你需要业务规则时，请先调用 read_skill(\"" + agentKey + "\")。\n"
                + "最终输出必须同时满足技能文档约束，并严格匹配结构化输出 Schema。";

        return ReactAgent.builder()
                .name(agentKey)
                .description(description)
                .model(model)
                .instruction(instruction)
                .outputSchema(outputSchemaFor(outputType))
                .methodTools(methodToolBeans)
                .hooks(skillsHook, ModelCallLimitHook.builder().runLimit(6).build())
                .build();
    }

}

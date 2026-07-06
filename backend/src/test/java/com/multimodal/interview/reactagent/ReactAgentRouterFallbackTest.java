package com.multimodal.interview.reactagent;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.reactagent.output.AgentStructuredOutputs;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReactAgentRouterFallbackTest {

    @Test
    void fallsBackResumeAnalysisWhenStructuredOutputHasWrongFieldTypes() throws Exception {
        ReactAgent agent = mock(ReactAgent.class);
        when(agent.call(any(UserMessage.class))).thenReturn(new AssistantMessage("""
                {"score":"偏低","suggestions":"补充项目量化结果"}
                """));
        ReactAgentRouter router = newRouter("ai-resume-analysis", agent);

        Object output = router.run("ai-resume-analysis", Map.of(
                "jobName", "后端开发工程师",
                "jobRequirements", "Java, Spring Boot, MySQL",
                "resumeContent", "熟悉 Java，项目结果描述较少"
        ), "chat-1");

        assertThat(output).isInstanceOf(AgentStructuredOutputs.AiResumeAnalysisResult.class);
        AgentStructuredOutputs.AiResumeAnalysisResult result =
                (AgentStructuredOutputs.AiResumeAnalysisResult) output;
        assertThat(result.getScore()).isZero();
        assertThat(result.getSuggestions()).contains("补充项目量化结果");
    }

    @Test
    void fallsBackQuestionAnalysisWhenAgentReturnsPlainText() throws Exception {
        ReactAgent agent = mock(ReactAgent.class);
        when(agent.call(any(UserMessage.class))).thenReturn(new AssistantMessage("这道题主要考察事务隔离级别，需要区分脏读、不可重复读和幻读。"));
        ReactAgentRouter router = newRouter("ai-question-analysis", agent);

        Object output = router.run("ai-question-analysis", Map.of(
                "question", "MySQL 可重复读隔离级别主要解决什么问题？"
        ), "chat-1");

        assertThat(output).isInstanceOf(AgentStructuredOutputs.QuestionAnalysisResult.class);
        AgentStructuredOutputs.QuestionAnalysisResult result =
                (AgentStructuredOutputs.QuestionAnalysisResult) output;
        assertThat(result.getAnalysis()).contains("事务隔离级别");
    }

    private ReactAgentRouter newRouter(String agentKey, ReactAgent agent) {
        InterviewAssistantMemoryService memoryService = mock(InterviewAssistantMemoryService.class);
        return new ReactAgentRouter(
                Map.of(agentKey, agent),
                new ObjectMapper(),
                memoryService
        );
    }
}

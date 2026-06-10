package com.multimodal.interview.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatProperties;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekConnectionProperties;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 自定义 DeepSeek ChatModel，请求体里显式传入 extra_body.thinking.disabled。
 */
@Configuration
public class DeepSeekChatModelConfig {

    @Bean
    @Primary
    public ChatModel deepSeekChatModelWithDisabledThinking(
            DeepSeekConnectionProperties connectionProperties,
            DeepSeekChatProperties chatProperties,
            ObjectProvider<RestClient.Builder> restClientBuilderProvider,
            ObjectProvider<WebClient.Builder> webClientBuilderProvider,
            ToolCallingManager toolCallingManager,
            RetryTemplate retryTemplate,
            ResponseErrorHandler responseErrorHandler,
            ObjectProvider<ObservationRegistry> observationRegistryProvider,
            ObjectProvider<ChatModelObservationConvention> observationConventionProvider,
            ObjectProvider<ToolExecutionEligibilityPredicate> toolExecutionEligibilityPredicateProvider,
            DeepSeekThinkingModeRequestInterceptor interceptor
    ) {
        RestClient.Builder restClientBuilder = restClientBuilderProvider.getIfAvailable(RestClient::builder).clone();
        restClientBuilder.requestInterceptor(interceptor);

        WebClient.Builder webClientBuilder = webClientBuilderProvider.getIfAvailable(WebClient::builder);

        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .baseUrl(resolveBaseUrl(chatProperties, connectionProperties))
                .apiKey(new SimpleApiKey(resolveApiKey(chatProperties, connectionProperties)))
                .completionsPath(chatProperties.getCompletionsPath())
                .betaPrefixPath(chatProperties.getBetaPrefixPath())
                .restClientBuilder(restClientBuilder)
                .webClientBuilder(webClientBuilder)
                .responseErrorHandler(responseErrorHandler)
                .build();

        DeepSeekChatModel chatModel = DeepSeekChatModel.builder()
                .deepSeekApi(deepSeekApi)
                .defaultOptions(chatProperties.getOptions())
                .toolCallingManager(toolCallingManager)
                .toolExecutionEligibilityPredicate(
                        toolExecutionEligibilityPredicateProvider.getIfUnique(DefaultToolExecutionEligibilityPredicate::new)
                )
                .retryTemplate(retryTemplate)
                .observationRegistry(observationRegistryProvider.getIfUnique(() -> ObservationRegistry.NOOP))
                .build();

        observationConventionProvider.ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }

    private String resolveBaseUrl(DeepSeekChatProperties chatProperties, DeepSeekConnectionProperties connectionProperties) {
        String baseUrl = StringUtils.hasText(chatProperties.getBaseUrl())
                ? chatProperties.getBaseUrl()
                : connectionProperties.getBaseUrl();
        Assert.hasText(baseUrl, "DeepSeek base URL must be set");
        return baseUrl;
    }

    private String resolveApiKey(DeepSeekChatProperties chatProperties, DeepSeekConnectionProperties connectionProperties) {
        String apiKey = StringUtils.hasText(chatProperties.getApiKey())
                ? chatProperties.getApiKey()
                : connectionProperties.getApiKey();
        Assert.hasText(apiKey, "DeepSeek API key must be set");
        return apiKey;
    }
}

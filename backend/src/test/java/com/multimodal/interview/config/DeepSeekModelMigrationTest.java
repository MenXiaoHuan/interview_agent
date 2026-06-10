package com.multimodal.interview.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class DeepSeekModelMigrationTest {

    @Value("${spring.ai.deepseek.chat.options.model}")
    private String configuredModel;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldUseDeepseekV4FlashAsDefaultModel() {
        assertEquals("deepseek-v4-flash", configuredModel);
        assertFalse(applicationContext.getBeansOfType(ChatModel.class).isEmpty());
    }
}

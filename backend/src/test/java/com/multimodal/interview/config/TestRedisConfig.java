package com.multimodal.interview.config;

import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("test")
public class TestRedisConfig {

    @Bean(destroyMethod = "")
    public RedissonClient redissonClient() {
        return mock(RedissonClient.class);
    }
}

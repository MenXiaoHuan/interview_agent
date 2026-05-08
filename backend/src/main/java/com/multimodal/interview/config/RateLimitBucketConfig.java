package com.multimodal.interview.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 限流令牌桶配置。
 */
@Configuration
public class RateLimitBucketConfig {

    /**
     * 创建默认令牌桶。
     *
     * @return Bucket4j 令牌桶实例
     */
    @Operation(summary = "创建令牌桶")
    @Bean
    public Bucket createNewBucket() {
        Refill refill = Refill.intervally(10, Duration.ofSeconds(1));
        Bandwidth limit = Bandwidth.classic(10, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}

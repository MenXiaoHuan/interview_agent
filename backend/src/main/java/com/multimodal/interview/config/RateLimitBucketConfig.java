package com.multimodal.interview.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 限流令牌桶配置。
 */
@Configuration
public class RateLimitBucketConfig {

    private final long capacity;
    private final long refillTokens;
    private final long refillSeconds;

    public RateLimitBucketConfig(
            @Value("${app.rate-limit.capacity:10}") long capacity,
            @Value("${app.rate-limit.refill-tokens:10}") long refillTokens,
            @Value("${app.rate-limit.refill-seconds:1}") long refillSeconds) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillSeconds = refillSeconds;
    }

    /**
     * 创建新的令牌桶实例。
     *
     * @return Bucket4j 令牌桶实例
     */
    public Bucket createNewBucket() {
        Refill refill = Refill.intervally(refillTokens, Duration.ofSeconds(refillSeconds));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}

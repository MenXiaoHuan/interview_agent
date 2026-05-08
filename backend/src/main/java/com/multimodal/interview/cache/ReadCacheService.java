package com.multimodal.interview.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 读缓存服务（Redis）。
 *
 * <p>目标：</p>
 * <p>- 只给高频读接口提供统一缓存能力</p>
 * <p>- 防穿透：对空结果写入短 TTL 空值哨兵</p>
 * <p>- 防雪崩：TTL 增加随机抖动，避免同一时刻大面积失效</p>
 * <p>- 防击穿：热点 key 重建时加分布式互斥锁</p>
 */
@Service
public class ReadCacheService {

    private static final String NULL_SENTINEL = "__CACHE_NULL__";
    private static final Duration DEFAULT_LOCK_LEASE = Duration.ofSeconds(5);

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    public ReadCacheService(RedissonClient redissonClient, ObjectMapper objectMapper) {
        this.redissonClient = redissonClient;
        this.objectMapper = objectMapper;
    }

    public <T> T getOrLoad(
            String cacheKey,
            JavaType valueType,
            Duration ttl,
            Duration nullTtl,
            Supplier<T> loader
    ) {
        String cached = readRaw(cacheKey);
        if (cached != null) {
            return parseCachedValue(cached, valueType);
        }

        String lockKey = "lock:" + cacheKey;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(100, DEFAULT_LOCK_LEASE.toMillis(), TimeUnit.MILLISECONDS);
            if (!locked) {
                // 没拿到锁时，短暂等待并回读，尽量复用其他线程刚写回的结果
                Thread.sleep(60);
                String retryValue = readRaw(cacheKey);
                if (retryValue != null) {
                    return parseCachedValue(retryValue, valueType);
                }
            }

            String doubleChecked = readRaw(cacheKey);
            if (doubleChecked != null) {
                return parseCachedValue(doubleChecked, valueType);
            }

            T loaded = loader.get();
            if (loaded == null) {
                writeRaw(cacheKey, NULL_SENTINEL, withJitter(nullTtl));
                return null;
            }
            String serialized = objectMapper.writeValueAsString(loaded);
            writeRaw(cacheKey, serialized, withJitter(ttl));
            return loaded;
        } catch (Exception exception) {
            throw new IllegalStateException("读取缓存失败: " + cacheKey, exception);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void evict(String cacheKey) {
        redissonClient.getBucket(cacheKey).delete();
    }

    public void evictByPrefix(String prefix) {
        String pattern = prefix.endsWith("*") ? prefix : prefix + "*";
        for (String key : redissonClient.getKeys().getKeysStreamByPattern(pattern).toList()) {
            redissonClient.getBucket(key).delete();
        }
    }

    private String readRaw(String cacheKey) {
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        return bucket.get();
    }

    private void writeRaw(String cacheKey, String value, Duration ttl) {
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            bucket.set(value);
            return;
        }
        bucket.set(value, ttl);
    }

    private <T> T parseCachedValue(String raw, JavaType valueType) {
        if (Objects.equals(NULL_SENTINEL, raw)) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, valueType);
        } catch (Exception exception) {
            throw new IllegalStateException("反序列化缓存失败", exception);
        }
    }

    private Duration withJitter(Duration base) {
        if (base == null || base.isZero() || base.isNegative()) {
            return base;
        }
        long jitterMillis = ThreadLocalRandom.current().nextLong(Math.max(1, base.toMillis() / 5));
        return base.plusMillis(jitterMillis);
    }
}

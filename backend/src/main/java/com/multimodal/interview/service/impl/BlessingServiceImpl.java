package com.multimodal.interview.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.cache.ReadCacheService;
import com.multimodal.interview.dto.BlessingCreateRequest;
import com.multimodal.interview.dto.BlessingResponse;
import com.multimodal.interview.dto.BlessingUpdateRequest;
import com.multimodal.interview.entity.Blessing;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.mapper.BlessingMapper;
import com.multimodal.interview.service.BlessingService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 祝福语管理服务实现。
 */
@Service
public class BlessingServiceImpl implements BlessingService {
    private static final String CACHE_PREFIX = "read-cache:blessing:";
    private static final Duration LIST_TTL = Duration.ofMinutes(10);
    private static final Duration NULL_TTL = Duration.ofMinutes(2);

    private final BlessingMapper blessingMapper;
    private final ReadCacheService readCacheService;
    private final ObjectMapper objectMapper;

    public BlessingServiceImpl(
            BlessingMapper blessingMapper,
            ReadCacheService readCacheService,
            ObjectMapper objectMapper
    ) {
        this.blessingMapper = blessingMapper;
        this.readCacheService = readCacheService;
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Blessing create(Long userId, BlessingCreateRequest request) {
        Blessing b = new Blessing();
        b.setUserId(userId);
        b.setContent(request.getContent());
        b.setType(request.getType());
        int r = blessingMapper.insert(b);
        if (r != 1) throw BusinessException.internalError("创建失败");
        evictBlessingCaches();
        return b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Blessing update(Long id, BlessingUpdateRequest request) {
        Blessing exist = blessingMapper.findById(id);
        if (exist == null) throw BusinessException.notFound("不存在");
        if (request.getContent() != null) exist.setContent(request.getContent());
        if (request.getType() != null) exist.setType(request.getType());
        if (request.getStatus() != null) exist.setStatus(request.getStatus());
        int r = blessingMapper.update(exist);
        if (r != 1) throw BusinessException.internalError("更新失败");
        evictBlessingCaches();
        return exist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Long id) {
        int r = blessingMapper.delete(id);
        if (r != 1) throw BusinessException.internalError("删除失败");
        evictBlessingCaches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Blessing get(Long id) {
        Blessing b = blessingMapper.findById(id);
        if (b == null) throw BusinessException.notFound("不存在");
        return b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BlessingResponse> list(String status, String type) {
        String cacheKey = CACHE_PREFIX + "list:" + (status == null ? "_" : status) + ":" + (type == null ? "_" : type);
        List<BlessingResponse> cached = readCacheService.getOrLoad(
                cacheKey,
                objectMapper.getTypeFactory().constructCollectionType(List.class, BlessingResponse.class),
                LIST_TTL,
                NULL_TTL,
                () -> blessingMapper.list(status, type)
        );
        return cached == null ? List.of() : cached;
    }

    private void evictBlessingCaches() {
        readCacheService.evictByPrefix(CACHE_PREFIX + "list:");
    }
}

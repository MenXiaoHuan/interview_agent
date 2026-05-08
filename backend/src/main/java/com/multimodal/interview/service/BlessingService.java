package com.multimodal.interview.service;

import com.multimodal.interview.dto.BlessingCreateRequest;
import com.multimodal.interview.dto.BlessingResponse;
import com.multimodal.interview.dto.BlessingUpdateRequest;
import com.multimodal.interview.entity.Blessing;

import java.util.List;

/**
 * 祝福语管理服务接口。
 */
public interface BlessingService {
    Blessing create(Long userId, BlessingCreateRequest request);
    Blessing update(Long id, BlessingUpdateRequest request);
    void delete(Long id);
    Blessing get(Long id);
    List<BlessingResponse> list(String status, String type);
}

package com.multimodal.interview.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadAvatar(Long userId, MultipartFile file);
}

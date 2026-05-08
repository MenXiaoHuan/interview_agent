package com.multimodal.interview.service.impl;

import com.multimodal.interview.service.SensitiveWordService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 敏感词校验服务实现。
 */
@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {
    private final Set<String> sensitiveWords;

    public SensitiveWordServiceImpl() {
        // 这里可以从配置文件或数据库加载敏感词
        sensitiveWords = new HashSet<>(Arrays.asList(
                "admin", "root", "system", "管理员", "超级管理员"
        ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        return sensitiveWords.stream()
                .anyMatch(lowerText::contains);
    }
} 

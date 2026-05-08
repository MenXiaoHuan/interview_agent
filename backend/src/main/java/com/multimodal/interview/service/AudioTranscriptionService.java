package com.multimodal.interview.service;

import org.springframework.web.multipart.MultipartFile;


/**
 * 语音转写服务接口。
 */
public interface AudioTranscriptionService {
    /**
     * 上传音频
     */
    String uploadAudio(MultipartFile file) throws Exception;

    /**
     * 创建转录任务
     */
    String createTask(String audioUrl) throws Exception;

    /**
     * 查询转录任务状态
     */
    String queryTask(String taskId) throws Exception;
}

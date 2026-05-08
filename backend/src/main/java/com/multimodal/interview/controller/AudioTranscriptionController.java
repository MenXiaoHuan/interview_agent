package com.multimodal.interview.controller;

import com.multimodal.interview.service.AudioTranscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
/**
 * 语音转写接口控制器。
 */
@Tag(name = "语音转写", description = "语音转写相关接口")
@Slf4j
@RestController
@RequestMapping("/api/transcription")
public class AudioTranscriptionController {

    private final AudioTranscriptionService transcriptionService;

    public AudioTranscriptionController(AudioTranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }
    /**
     * 上传一个音频。
     *
     * @param file 上传文件
     * @return HTTP 响应结果
     */
    @Operation(summary = "上传一个音频",
            description = "上传一个音频进行转录")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadAudio(@RequestParam("file") MultipartFile file){
        try {
            String response = transcriptionService.uploadAudio(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("上传文件出错: " + e.getMessage());
        }
    }
    /**
     * 创建转录任务。
     *
     * @param audioUrl 音频地址
     * @return HTTP 响应结果
     */
    @Operation(summary = "创建转录任务",
            description = "为上传的音频创建转录任务")
    @PostMapping("/create-task")
    public ResponseEntity<String> createTask(@RequestParam("audioUrl") String audioUrl){
        try {
            String response = transcriptionService.createTask(audioUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建文件出错: " + e.getMessage());
        }
    }
    /**
     * 查询任务状态。
     *
     * @param taskId 任务 ID
     * @return HTTP 响应结果
     */
    @Operation(summary = "查询任务状态",
            description = "查询转录任务的状态")
    @GetMapping("/query-task/{taskId}")
    public ResponseEntity<String> queryTask(@PathVariable String taskId){
        try {
            String response = transcriptionService.queryTask(taskId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询文件出错: " + e.getMessage());
        }
    }
} 
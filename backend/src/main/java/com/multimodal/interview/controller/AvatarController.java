package com.multimodal.interview.controller;

import com.multimodal.interview.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class AvatarController {

    private final StorageService storageService;

    public AvatarController(StorageService storageService) {
        this.storageService = storageService;
    }

    @Operation(summary = "读取头像文件", description = "通过后端代理从 MinIO 读取头像对象")
    @GetMapping("/avatar")
    public ResponseEntity<byte[]> getAvatar(@RequestParam("object") String objectName) {
        StorageService.StoredObject object = storageService.readObject(objectName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(object.contentType()))
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(object.content());
    }
}

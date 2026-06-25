package com.multimodal.interview.service.impl;

import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.config.StorageProperties;
import com.multimodal.interview.service.StorageService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class MinioStorageService implements StorageService {

    private static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024 * 1024;

    private final MinioClient minioClient;
    private final StorageProperties properties;

    public MinioStorageService(MinioClient minioClient, StorageProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        validateAvatar(file);
        String contentType = String.valueOf(file.getContentType());
        String objectName = "avatar/user-" + userId + "/" + UUID.randomUUID() + resolveExtension(contentType);
        try {
            ensureBucket();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build());
            return "/api/avatar?object=" + URLEncoder.encode(objectName, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw BusinessException.internalError("上传头像到对象存储失败");
        }
    }

    @Override
    public StoredObject readObject(String objectName) {
        if (objectName == null || objectName.isBlank() || !objectName.startsWith("avatar/")) {
            throw BusinessException.badRequest("头像对象不存在");
        }
        try (GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder()
                .bucket(properties.bucket())
                .object(objectName)
                .build())) {
            String contentType = response.headers().get("Content-Type");
            return new StoredObject(response.readAllBytes(), contentType == null ? "application/octet-stream" : contentType);
        } catch (Exception e) {
            throw BusinessException.notFound("头像对象不存在");
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(properties.bucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(properties.bucket())
                    .build());
        }
    }

    private void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("头像文件不能为空");
        }
        if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw BusinessException.badRequest("头像文件过大（最大 5MB）");
        }
        resolveExtension(String.valueOf(file.getContentType()));
    }

    private String resolveExtension(String contentType) {
        if (contentType.contains("png")) {
            return ".png";
        }
        if (contentType.contains("jpeg") || contentType.contains("jpg")) {
            return ".jpg";
        }
        if (contentType.contains("webp")) {
            return ".webp";
        }
        throw BusinessException.badRequest("不支持的头像格式，仅支持 png/jpg/webp");
    }
}

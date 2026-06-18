package com.multimodal.interview.service.impl;

import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.config.StorageProperties;
import com.multimodal.interview.service.StorageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

@Service
public class MinioStorageService implements StorageService {

    private static final long MAX_AVATAR_SIZE = 5L * 1024 * 1024;

    private final MinioClient minioClient;
    private final StorageProperties storageProperties;

    public MinioStorageService(MinioClient minioClient, StorageProperties storageProperties) {
        this.minioClient = minioClient;
        this.storageProperties = storageProperties;
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (userId == null || userId <= 0) {
            throw BusinessException.badRequest("用户ID无效");
        }
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("头像文件不能为空");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw BusinessException.badRequest("头像文件过大（最大 5MB）");
        }

        String extension = resolveExtension(file.getContentType());
        String objectName = "avatar/" + userId + "/" + UUID.randomUUID() + extension;
        StorageProperties.Minio minio = storageProperties.getMinio();

        try {
            ensureBucket(minio.getBucket());
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minio.getBucket())
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
            }
        } catch (Exception e) {
            throw BusinessException.internalError("上传头像到对象存储失败");
        }

        return trimTrailingSlash(minio.getPublicUrl()) + "/" + minio.getBucket() + "/" + objectName;
    }

    private void ensureBucket(String bucket) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    private String resolveExtension(String contentType) {
        String normalized = String.valueOf(contentType).toLowerCase(Locale.ROOT);
        if (normalized.contains("png")) {
            return ".png";
        }
        if (normalized.contains("jpeg") || normalized.contains("jpg")) {
            return ".jpg";
        }
        if (normalized.contains("webp")) {
            return ".webp";
        }
        throw BusinessException.badRequest("不支持的头像格式，仅支持 png/jpg/webp");
    }

    private String trimTrailingSlash(String value) {
        return String.valueOf(value).replaceAll("/+$", "");
    }
}

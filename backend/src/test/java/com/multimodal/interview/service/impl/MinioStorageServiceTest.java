package com.multimodal.interview.service.impl;

import com.multimodal.interview.config.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MinioStorageServiceTest {

    @Test
    void uploadAvatarReturnsBackendProxyUrl() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        StorageProperties properties = new StorageProperties(
                "http://minio:9000",
                "access-key",
                "secret-key",
                "qa-report"
        );
        MinioStorageService storageService = new MinioStorageService(minioClient, properties);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        String avatarUrl = storageService.uploadAvatar(7L, file);

        assertThat(avatarUrl).startsWith("/avatar?object=");
        String encodedObjectName = avatarUrl.substring("/avatar?object=".length());
        String objectName = URLDecoder.decode(encodedObjectName, StandardCharsets.UTF_8);
        assertThat(objectName).startsWith("avatar/user-7/");
        assertThat(objectName).endsWith(".png");
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }
}

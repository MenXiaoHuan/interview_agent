package com.multimodal.interview.service.impl;

import com.multimodal.interview.config.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import okhttp3.Headers;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
                "interview-agent"
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

        assertThat(avatarUrl).startsWith("/api/avatar?object=");
        String encodedObjectName = avatarUrl.substring("/api/avatar?object=".length());
        String objectName = URLDecoder.decode(encodedObjectName, StandardCharsets.UTF_8);
        assertThat(objectName).startsWith("avatar/user-7/");
        assertThat(objectName).endsWith(".png");
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void uploadAvatarRejectsUnsupportedContentTypeBeforeCallingMinio() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        StorageProperties properties = new StorageProperties(
                "http://minio:9000",
                "access-key",
                "secret-key",
                "interview-agent"
        );
        MinioStorageService storageService = new MinioStorageService(minioClient, properties);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.gif",
                "image/gif",
                new byte[]{1, 2, 3}
        );

        assertThatThrownBy(() -> storageService.uploadAvatar(7L, file))
                .hasMessageContaining("不支持的头像格式");
        verify(minioClient, never()).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
    }

    @Test
    void readObjectReturnsStoredBytesAndContentType() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        StorageProperties properties = new StorageProperties(
                "http://minio:9000",
                "access-key",
                "secret-key",
                "interview-agent"
        );
        MinioStorageService storageService = new MinioStorageService(minioClient, properties);
        GetObjectResponse response = new GetObjectResponse(
                Headers.of("Content-Type", "image/webp"),
                "interview-agent",
                null,
                "avatar/user-7/avatar.webp",
                new ByteArrayInputStream(new byte[]{9, 8, 7})
        );

        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);

        var object = storageService.readObject("avatar/user-7/avatar.webp");

        assertThat(object.content()).containsExactly(9, 8, 7);
        assertThat(object.contentType()).isEqualTo("image/webp");
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }
}

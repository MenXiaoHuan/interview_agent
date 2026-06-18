package com.multimodal.interview.service.impl;

import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.config.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MinioStorageServiceTest {

    @Test
    void uploadsAvatarAndReturnsPublicUrl() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        StorageProperties properties = properties();
        MinioStorageService service = new MinioStorageService(minioClient, properties);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "demo".getBytes());

        String url = service.uploadAvatar(7L, file);

        assertThat(url).startsWith("http://localhost:9000/interview-agent/avatar/7/");
        assertThat(url).endsWith(".png");
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void createsBucketWhenMissing() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        MinioStorageService service = new MinioStorageService(minioClient, properties());
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "demo".getBytes());

        service.uploadAvatar(8L, file);

        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void rejectsNonImageAvatar() {
        MinioStorageService service = new MinioStorageService(mock(MinioClient.class), properties());
        MockMultipartFile file = new MockMultipartFile("file", "avatar.txt", "text/plain", "demo".getBytes());

        assertThatThrownBy(() -> service.uploadAvatar(7L, file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不支持的头像格式");
    }

    private StorageProperties properties() {
        StorageProperties properties = new StorageProperties();
        StorageProperties.Minio minio = new StorageProperties.Minio();
        minio.setEndpoint("http://minio:9000");
        minio.setPublicUrl("http://localhost:9000");
        minio.setAccessKey("minioadmin");
        minio.setSecretKey("minioadmin");
        minio.setBucket("interview-agent");
        properties.setMinio(minio);
        return properties;
    }
}

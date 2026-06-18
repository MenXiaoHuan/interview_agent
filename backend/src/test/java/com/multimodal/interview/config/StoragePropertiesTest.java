package com.multimodal.interview.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class StoragePropertiesTest {

    @Test
    void bindsMinioStorageProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("app.storage.type", "minio")
                .withProperty("app.storage.minio.endpoint", "http://minio:9000")
                .withProperty("app.storage.minio.public-url", "http://localhost:9000")
                .withProperty("app.storage.minio.access-key", "minioadmin")
                .withProperty("app.storage.minio.secret-key", "minioadmin")
                .withProperty("app.storage.minio.bucket", "interview-agent");

        StorageProperties properties = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()))
                .bind("app.storage", Bindable.of(StorageProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertThat(properties.getType()).isEqualTo("minio");
        assertThat(properties.getMinio().getEndpoint()).isEqualTo("http://minio:9000");
        assertThat(properties.getMinio().getPublicUrl()).isEqualTo("http://localhost:9000");
        assertThat(properties.getMinio().getBucket()).isEqualTo("interview-agent");
    }
}

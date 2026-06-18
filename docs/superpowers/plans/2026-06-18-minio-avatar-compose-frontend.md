# MinIO Avatar Compose Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Store avatar uploads in a public MinIO bucket and allow Docker Compose to inject the frontend API URL while preserving local `.env.development` usage.

**Architecture:** Add a focused storage abstraction between `UserServiceImpl` and object storage. Implement MinIO as the storage backend, wire it through Spring configuration, keep the existing avatar upload endpoint contract, and extend Docker Compose with MinIO and frontend services.

**Tech Stack:** Spring Boot 3.4.5, Java 17, MinIO Java SDK, MyBatis, JUnit 5, Mockito, Vue 3, uni-app, Vite, Docker Compose.

---

## File Map

- Modify `backend/pom.xml`: add MinIO Java SDK dependency.
- Create `backend/src/main/java/com/multimodal/interview/config/StorageProperties.java`: bind `app.storage.*` settings.
- Create `backend/src/main/java/com/multimodal/interview/config/MinioConfig.java`: create `MinioClient` bean.
- Create `backend/src/main/java/com/multimodal/interview/service/StorageService.java`: storage abstraction for avatar uploads.
- Create `backend/src/main/java/com/multimodal/interview/service/impl/MinioStorageService.java`: upload avatars to MinIO and return public URLs.
- Modify `backend/src/main/java/com/multimodal/interview/service/impl/UserServiceImpl.java`: delegate avatar file upload to `StorageService`.
- Modify `backend/src/main/java/com/multimodal/interview/service/UserService.java`: remove `baseUrl` parameter from avatar file upload contract.
- Modify `backend/src/main/java/com/multimodal/interview/controller/UserController.java`: stop building local base URL, update API description.
- Modify `backend/src/main/resources/application.yml`: add common storage config keys.
- Modify `backend/src/main/resources/application-dev.yml`: add local MinIO defaults.
- Modify `backend/src/main/resources/application-test.yml`: add test MinIO defaults.
- Modify `backend/src/main/resources/application-prod.yml`: require production MinIO variables.
- Create `backend/src/test/java/com/multimodal/interview/service/impl/MinioStorageServiceTest.java`: validate MinIO upload behavior.
- Create `backend/src/test/java/com/multimodal/interview/service/impl/UserServiceImplAvatarUploadTest.java`: validate avatar URL persistence.
- Modify `docker-compose.yml`: add MinIO and frontend services, inject related env vars.
- Modify `docs/deployment.md`: document MinIO, Compose frontend variables, and local access URLs.
- Modify `README.md`: point to MinIO/Compose setup.

---

### Task 1: Add Storage Configuration and MinIO Dependency

**Files:**
- Modify: `backend/pom.xml`
- Create: `backend/src/main/java/com/multimodal/interview/config/StorageProperties.java`
- Create: `backend/src/main/java/com/multimodal/interview/config/MinioConfig.java`
- Modify: backend application YAML files
- Test: `backend/src/test/java/com/multimodal/interview/config/StoragePropertiesTest.java`

- [ ] **Step 1: Write failing configuration binding test**

Create `backend/src/test/java/com/multimodal/interview/config/StoragePropertiesTest.java`:

```java
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
                .orElseThrow();

        assertThat(properties.getType()).isEqualTo("minio");
        assertThat(properties.getMinio().getEndpoint()).isEqualTo("http://minio:9000");
        assertThat(properties.getMinio().getPublicUrl()).isEqualTo("http://localhost:9000");
        assertThat(properties.getMinio().getBucket()).isEqualTo("interview-agent");
    }
}
```

- [ ] **Step 2: Run test and verify it fails**

Run:

```bash
cd backend
./mvnw -Dtest=StoragePropertiesTest test
```

Expected: compilation fails because `StorageProperties` does not exist.

- [ ] **Step 3: Add MinIO dependency**

Add to `backend/pom.xml` dependencies:

```xml
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.17</version>
</dependency>
```

- [ ] **Step 4: Create StorageProperties**

Create `backend/src/main/java/com/multimodal/interview/config/StorageProperties.java`:

```java
package com.multimodal.interview.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String type = "minio";
    private Minio minio = new Minio();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Minio getMinio() {
        return minio;
    }

    public void setMinio(Minio minio) {
        this.minio = minio;
    }

    public static class Minio {
        private String endpoint;
        private String publicUrl;
        private String accessKey;
        private String secretKey;
        private String bucket;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getPublicUrl() {
            return publicUrl;
        }

        public void setPublicUrl(String publicUrl) {
            this.publicUrl = publicUrl;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }
    }
}
```

- [ ] **Step 5: Create MinioConfig**

Create `backend/src/main/java/com/multimodal/interview/config/MinioConfig.java`:

```java
package com.multimodal.interview.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class MinioConfig {

    @Bean
    public MinioClient minioClient(StorageProperties storageProperties) {
        StorageProperties.Minio minio = storageProperties.getMinio();
        return MinioClient.builder()
                .endpoint(minio.getEndpoint())
                .credentials(minio.getAccessKey(), minio.getSecretKey())
                .build();
    }
}
```

- [ ] **Step 6: Add YAML storage config**

Add to `application.yml`:

```yaml
  storage:
    type: ${PLATFORM_STORAGE_TYPE:minio}
    minio:
      endpoint: ${PLATFORM_MINIO_ENDPOINT:http://localhost:9000}
      public-url: ${PLATFORM_MINIO_PUBLIC_URL:http://localhost:9000}
      access-key: ${PLATFORM_MINIO_ACCESS_KEY:minioadmin}
      secret-key: ${PLATFORM_MINIO_SECRET_KEY:minioadmin}
      bucket: ${PLATFORM_MINIO_BUCKET:interview-agent}
```

Add environment-specific overrides:

```yaml
app:
  storage:
    minio:
      endpoint: ${PLATFORM_MINIO_ENDPOINT:http://localhost:9000}
      public-url: ${PLATFORM_MINIO_PUBLIC_URL:http://localhost:9000}
      access-key: ${PLATFORM_MINIO_ACCESS_KEY:minioadmin}
      secret-key: ${PLATFORM_MINIO_SECRET_KEY:minioadmin}
      bucket: ${PLATFORM_MINIO_BUCKET:interview-agent}
```

For `application-prod.yml`, use required values:

```yaml
app:
  storage:
    minio:
      endpoint: ${PLATFORM_MINIO_ENDPOINT}
      public-url: ${PLATFORM_MINIO_PUBLIC_URL}
      access-key: ${PLATFORM_MINIO_ACCESS_KEY}
      secret-key: ${PLATFORM_MINIO_SECRET_KEY}
      bucket: ${PLATFORM_MINIO_BUCKET}
```

- [ ] **Step 7: Run test**

Run:

```bash
cd backend
./mvnw -Dtest=StoragePropertiesTest test
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add backend/pom.xml backend/src/main/java/com/multimodal/interview/config/StorageProperties.java backend/src/main/java/com/multimodal/interview/config/MinioConfig.java backend/src/main/resources/application.yml backend/src/main/resources/application-dev.yml backend/src/main/resources/application-test.yml backend/src/main/resources/application-prod.yml backend/src/test/java/com/multimodal/interview/config/StoragePropertiesTest.java
git commit -m "feat: add minio storage configuration"
```

### Task 2: Implement MinIO Avatar Storage Service

**Files:**
- Create: `backend/src/main/java/com/multimodal/interview/service/StorageService.java`
- Create: `backend/src/main/java/com/multimodal/interview/service/impl/MinioStorageService.java`
- Test: `backend/src/test/java/com/multimodal/interview/service/impl/MinioStorageServiceTest.java`

- [ ] **Step 1: Write failing MinIO storage tests**

Create `backend/src/test/java/com/multimodal/interview/service/impl/MinioStorageServiceTest.java`:

```java
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
```

- [ ] **Step 2: Run tests and verify they fail**

Run:

```bash
cd backend
./mvnw -Dtest=MinioStorageServiceTest test
```

Expected: compilation fails because `StorageService` and `MinioStorageService` do not exist.

- [ ] **Step 3: Create StorageService**

Create `backend/src/main/java/com/multimodal/interview/service/StorageService.java`:

```java
package com.multimodal.interview.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadAvatar(Long userId, MultipartFile file);
}
```

- [ ] **Step 4: Create MinioStorageService**

Create `backend/src/main/java/com/multimodal/interview/service/impl/MinioStorageService.java`:

```java
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
```

- [ ] **Step 5: Run tests**

Run:

```bash
cd backend
./mvnw -Dtest=MinioStorageServiceTest test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/multimodal/interview/service/StorageService.java backend/src/main/java/com/multimodal/interview/service/impl/MinioStorageService.java backend/src/test/java/com/multimodal/interview/service/impl/MinioStorageServiceTest.java
git commit -m "feat: upload avatars to minio"
```

### Task 3: Wire User Avatar Upload to StorageService

**Files:**
- Modify: `backend/src/main/java/com/multimodal/interview/service/UserService.java`
- Modify: `backend/src/main/java/com/multimodal/interview/service/impl/UserServiceImpl.java`
- Modify: `backend/src/main/java/com/multimodal/interview/controller/UserController.java`
- Test: `backend/src/test/java/com/multimodal/interview/service/impl/UserServiceImplAvatarUploadTest.java`

- [ ] **Step 1: Write failing UserService test**

Create `backend/src/test/java/com/multimodal/interview/service/impl/UserServiceImplAvatarUploadTest.java`:

```java
package com.multimodal.interview.service.impl;

import com.multimodal.interview.entity.User;
import com.multimodal.interview.mapper.UserMapper;
import com.multimodal.interview.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplAvatarUploadTest {

    @Test
    void uploadsAvatarThroughStorageServiceAndPersistsReturnedUrl() {
        UserMapper userMapper = mock(UserMapper.class);
        SensitiveWordServiceImpl sensitiveWordService = mock(SensitiveWordServiceImpl.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        StorageService storageService = mock(StorageService.class);
        UserServiceImpl service = new UserServiceImpl(userMapper, sensitiveWordService, passwordEncoder, storageService);
        User user = new User();
        user.setId(7L);
        user.setUsername("alice");
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "demo".getBytes());
        when(userMapper.findByUsername("alice")).thenReturn(user);
        when(storageService.uploadAvatar(7L, file)).thenReturn("http://localhost:9000/interview-agent/avatar/7/demo.png");
        when(userMapper.updateAvatar(7L, "http://localhost:9000/interview-agent/avatar/7/demo.png")).thenReturn(1);

        String avatarUrl = service.uploadAvatar("alice", file);

        assertThat(avatarUrl).isEqualTo("http://localhost:9000/interview-agent/avatar/7/demo.png");
        verify(storageService).uploadAvatar(7L, file);
        verify(userMapper).updateAvatar(7L, "http://localhost:9000/interview-agent/avatar/7/demo.png");
    }
}
```

- [ ] **Step 2: Run test and verify it fails**

Run:

```bash
cd backend
./mvnw -Dtest=UserServiceImplAvatarUploadTest test
```

Expected: compilation fails because `UserServiceImpl` constructor and `uploadAvatar` signature still use local storage/baseUrl.

- [ ] **Step 3: Update UserService contract**

Change `UserService`:

```java
String uploadAvatar(String username, MultipartFile file);
```

Remove the `baseUrl` JavaDoc parameter.

- [ ] **Step 4: Update UserServiceImpl constructor and upload logic**

Remove imports for `@Value`, `IOException`, `Files`, `Path`, `Paths`.

Add field:

```java
private final StorageService storageService;
```

Constructor:

```java
public UserServiceImpl(
        UserMapper userMapper,
        SensitiveWordServiceImpl sensitiveWordServiceImpl,
        PasswordEncoder passwordEncoder,
        StorageService storageService
) {
    this.userMapper = userMapper;
    this.sensitiveWordServiceImpl = sensitiveWordServiceImpl;
    this.passwordEncoder = passwordEncoder;
    this.storageService = storageService;
}
```

Replace `uploadAvatar`:

```java
@Override
@Transactional
public String uploadAvatar(String username, MultipartFile file) {
    User user = userMapper.findByUsername(username);
    if (user == null) {
        throw BusinessException.notFound("用户不存在");
    }
    String avatarUrl = storageService.uploadAvatar(user.getId(), file);
    int rows = userMapper.updateAvatar(user.getId(), avatarUrl);
    if (rows != 1) {
        throw BusinessException.internalError("更新头像失败");
    }
    return avatarUrl;
}
```

- [ ] **Step 5: Update UserController**

Remove `ServletUriComponentsBuilder` import and base URL construction.

Change:

```java
String avatarUrl = userService.uploadAvatar(username, file);
```

Update OpenAPI description:

```java
@Operation(summary = "上传头像文件并更新用户头像", description = "上传图片文件到对象存储并返回可访问 URL")
```

- [ ] **Step 6: Run tests**

Run:

```bash
cd backend
./mvnw -Dtest=UserServiceImplAvatarUploadTest,MinioStorageServiceTest test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/multimodal/interview/service/UserService.java backend/src/main/java/com/multimodal/interview/service/impl/UserServiceImpl.java backend/src/main/java/com/multimodal/interview/controller/UserController.java backend/src/test/java/com/multimodal/interview/service/impl/UserServiceImplAvatarUploadTest.java
git commit -m "refactor: store uploaded avatars through storage service"
```

### Task 4: Add MinIO and Frontend Services to Docker Compose

**Files:**
- Modify: `docker-compose.yml`
- Modify: `docs/deployment.md`
- Modify: `README.md`

- [ ] **Step 1: Update Docker Compose**

Update `docker-compose.yml`:

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: interview_agent
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-interview_agent_dev}
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./backend/src/main/resources/sql/interview_agent.sql:/docker-entrypoint-initdb.d/01-interview-agent.sql:ro

  redis:
    image: redis:7
    ports:
      - "6379:6379"

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${PLATFORM_MINIO_ACCESS_KEY:-minioadmin}
      MINIO_ROOT_PASSWORD: ${PLATFORM_MINIO_SECRET_KEY:-minioadmin}
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio-data:/data

  backend:
    image: eclipse-temurin:17
    working_dir: /app
    volumes:
      - ./backend:/app
    command: ./mvnw spring-boot:run
    environment:
      SPRING_PROFILES_ACTIVE: dev
      PLATFORM_DB_URL: jdbc:mysql://mysql:3306/interview_agent
      PLATFORM_DB_USERNAME: root
      PLATFORM_DB_PASSWORD: ${MYSQL_ROOT_PASSWORD:-interview_agent_dev}
      PLATFORM_REDIS_HOST: redis
      PLATFORM_JWT_SECRET: ${PLATFORM_JWT_SECRET:-dev-only-change-me-please-use-at-least-32-bytes}
      PLATFORM_SSL_ENABLED: "false"
      PLATFORM_CORS_ALLOWED_ORIGINS: http://localhost:5173
      PLATFORM_MINIO_ENDPOINT: http://minio:9000
      PLATFORM_MINIO_PUBLIC_URL: http://localhost:9000
      PLATFORM_MINIO_ACCESS_KEY: ${PLATFORM_MINIO_ACCESS_KEY:-minioadmin}
      PLATFORM_MINIO_SECRET_KEY: ${PLATFORM_MINIO_SECRET_KEY:-minioadmin}
      PLATFORM_MINIO_BUCKET: ${PLATFORM_MINIO_BUCKET:-interview-agent}
    ports:
      - "8442:8442"
    depends_on:
      - mysql
      - redis
      - minio

  frontend:
    image: node:20
    working_dir: /app
    volumes:
      - ./project:/app
    command: sh -c "npm install && npm run dev:h5 -- --host 0.0.0.0"
    environment:
      VITE_API_BASE_URL: http://localhost:8442
    ports:
      - "5173:5173"
    depends_on:
      - backend

volumes:
  mysql-data:
  minio-data:
```

- [ ] **Step 2: Update deployment docs**

Add to `docs/deployment.md`:

```markdown
## MinIO Avatar Storage

Local Docker Compose starts MinIO:

- API: `http://localhost:9000`
- Console: `http://localhost:9001`
- Default development user: `minioadmin`
- Default development password: `minioadmin`
- Default bucket: `interview-agent`

Production must provide:

- `PLATFORM_MINIO_ENDPOINT`
- `PLATFORM_MINIO_PUBLIC_URL`
- `PLATFORM_MINIO_ACCESS_KEY`
- `PLATFORM_MINIO_SECRET_KEY`
- `PLATFORM_MINIO_BUCKET`

Avatar URLs are stored as public MinIO URLs in `user.avatar_url`.
```

Add frontend Compose note:

```markdown
## Frontend in Docker Compose

The `frontend` service injects `VITE_API_BASE_URL=http://localhost:8442`.
This only applies when the frontend is started by Docker Compose.
When running `npm run dev:h5` directly, `project/.env.development` provides the same default.
```

- [ ] **Step 3: Update README**

Add a short note in deployment section:

```markdown
Docker Compose now includes MySQL, Redis, MinIO, backend, and frontend. MinIO stores uploaded avatars, and the frontend service receives `VITE_API_BASE_URL` from Compose.
```

- [ ] **Step 4: Validate Compose syntax**

Run:

```bash
docker compose config
```

Expected: Compose renders successfully. If Docker is unavailable, record the local environment limitation and run no destructive fallback.

- [ ] **Step 5: Commit**

```bash
git add docker-compose.yml docs/deployment.md README.md
git commit -m "chore: add minio and frontend compose services"
```

### Task 5: Final Verification

**Files:**
- No source files expected unless verification exposes a compile/test issue.

- [ ] **Step 1: Run backend tests**

Run:

```bash
cd backend
./mvnw test
```

Expected: PASS.

- [ ] **Step 2: Run frontend tests**

Run:

```bash
cd project
npm test
```

Expected: PASS.

- [ ] **Step 3: Run frontend build**

Run:

```bash
cd project
npm run build:h5
```

Expected: PASS.

- [ ] **Step 4: Confirm local disk avatar code is removed**

Run:

```bash
grep -R "Files.createDirectories\\|transferTo\\|/avatar/\" \\+ filename\\|app.file.root" backend/src/main/java/com/multimodal/interview/service/impl/UserServiceImpl.java backend/src/main/java/com/multimodal/interview/controller/UserController.java || true
```

Expected: no output.

- [ ] **Step 5: Confirm working tree**

Run:

```bash
git status --short
```

Expected: only pre-existing `project/src/pages.json` remains modified unless the user asks to handle it.

- [ ] **Step 6: Commit verification fixes if needed**

If Step 1-4 required small fixes, commit them:

```bash
git add <changed-files>
git commit -m "test: stabilize minio avatar verification"
```

If no fixes were needed, do not create an empty commit.

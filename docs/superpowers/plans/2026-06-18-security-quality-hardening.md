# Security Quality Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Harden Interview Agent across security, configuration, permissions, frontend request consistency, Agent tools, rate limiting, testing, CI, Docker, and documentation.

**Architecture:** Implement the work in four independently verifiable slices: backend security/configuration, frontend environment/request consistency, backend structural hardening, and quality/delivery assets. Keep public read APIs compatible while tightening write APIs, sensitive configuration, and production exposure.

**Tech Stack:** Spring Boot 3.4.5, Spring Security, MyBatis, Bucket4j, JUnit/Spring Boot Test, Vue 3, uni-app, Vite, Pinia, Axios, ESLint, Prettier, Docker Compose, GitHub Actions.

---

## File Map

- Modify `backend/src/main/resources/application.yml`: keep only safe shared defaults and profile activation.
- Create `backend/src/main/resources/application-dev.yml`: local development DB/Redis/SSL/CORS/Swagger settings.
- Create `backend/src/main/resources/application-test.yml`: test-safe DB/Redis/JWT/CORS/Swagger settings.
- Create `backend/src/main/resources/application-prod.yml`: production settings without sensitive defaults.
- Modify `backend/src/main/java/com/multimodal/interview/config/CorsConfig.java`: read allowed origins from config.
- Modify `backend/src/main/java/com/multimodal/interview/config/SecurityConfig.java`: enable method security, close production Swagger, protect admin write paths.
- Modify `backend/src/main/java/com/multimodal/interview/config/JwtConfig.java`: remove unsafe fallback secret.
- Modify `backend/src/main/java/com/multimodal/interview/common/filter/JwtAuthenticationFilter.java`: map `userType` to roles.
- Modify `backend/src/main/java/com/multimodal/interview/controller/ForgotPasswordController.java`: remove code-returning endpoints.
- Modify `backend/src/main/java/com/multimodal/interview/common/interceptor/RateLimitInterceptor.java`: per-user/IP rate limiting.
- Modify `backend/src/main/java/com/multimodal/interview/config/RateLimitBucketConfig.java`: provide per-key bucket factory instead of one shared bucket.
- Create `backend/src/main/java/com/multimodal/interview/reactagent/tool/JobAgentTool.java`: read-only Agent tool facade.
- Modify `backend/src/main/java/com/multimodal/interview/controller/JobController.java`: remove `@Tool` methods.
- Modify `backend/src/main/java/com/multimodal/interview/reactagent/ReactAgentConfig.java`: inject tool facade instead of controller where job tools are registered.
- Create `backend/src/main/java/com/multimodal/interview/dto/UserResponse.java`: safe user response DTO.
- Create `backend/src/main/java/com/multimodal/interview/dto/JobResponse.java`: safe job response DTO.
- Modify relevant controllers/services to return DTOs for user/job endpoints touched by this hardening pass.
- Modify `project/src/utils/api-config.js`: use `import.meta.env.VITE_API_BASE_URL`.
- Create `project/.env.development`: local backend URL.
- Create `project/.env.production.example`: production API URL example.
- Modify `project/src/utils/storage.js`: add typed helpers for token/user/job/session keys.
- Modify `project/src/utils/request.js`: consume storage helpers.
- Modify pages currently using direct `uni.request`: route through `utils/request.js` or existing API functions.
- Create backend tests under `backend/src/test/java/com/multimodal/interview/security/`.
- Create frontend tests under `project/src/utils/__tests__/`.
- Modify `project/package.json`: add lint/format/test scripts and dev dependencies.
- Create `project/eslint.config.js` and `project/.prettierrc`.
- Create `.github/workflows/ci.yml`: backend and frontend checks.
- Create `docker-compose.yml`: MySQL, Redis, backend, frontend orchestration.
- Create `docs/deployment.md`: environment variables, ports, Swagger, CORS, Docker Compose.

---

### Task 1: Backend Sensitive Configuration Split

**Files:**
- Modify: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/application-dev.yml`
- Create: `backend/src/main/resources/application-test.yml`
- Create: `backend/src/main/resources/application-prod.yml`
- Test: command-level Spring configuration load

- [ ] **Step 1: Write safe shared `application.yml`**

Replace sensitive defaults with shared non-secret configuration:

```yaml
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:interview_agent}
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  web:
    resources:
      static-locations: "${PLATFORM_STATIC_LOCATIONS:classpath:/static/,classpath:/public/,file:${app.file.root}/}"

mybatis:
  configuration:
    map-underscore-to-camel-case: ${PLATFORM_MYBATIS_MAP_UNDERSCORE_TO_CAMEL_CASE:true}
    log-impl: ${PLATFORM_MYBATIS_LOG_IMPL:}
  type-handlers-package: ${PLATFORM_MYBATIS_TYPE_HANDLERS_PACKAGE:com.multimodal.interview.util}

app:
  file:
    root: ${PLATFORM_FILE_ROOT:/home/file}
  cors:
    allowed-origins: ${PLATFORM_CORS_ALLOWED_ORIGINS:https://localhost:5173}
  swagger:
    enabled: ${PLATFORM_SWAGGER_ENABLED:false}
  rate-limit:
    capacity: ${PLATFORM_RATE_LIMIT_CAPACITY:10}
    refill-tokens: ${PLATFORM_RATE_LIMIT_REFILL_TOKENS:10}
    refill-seconds: ${PLATFORM_RATE_LIMIT_REFILL_SECONDS:1}
```

- [ ] **Step 2: Add development config**

Create `backend/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: "${PLATFORM_DB_URL:jdbc:mysql://localhost:3306/interview_agent}"
    username: ${PLATFORM_DB_USERNAME:root}
    password: ${PLATFORM_DB_PASSWORD:}
    driver-class-name: ${PLATFORM_DB_DRIVER_CLASS_NAME:com.mysql.cj.jdbc.Driver}
  data:
    redis:
      host: ${PLATFORM_REDIS_HOST:localhost}
      port: ${PLATFORM_REDIS_PORT:6379}
      password: ${PLATFORM_REDIS_PASSWORD:}
      database: ${PLATFORM_REDIS_DATABASE:0}
  servlet:
    multipart:
      max-file-size: ${PLATFORM_SERVLET_MAX_FILE_SIZE:10MB}
      max-request-size: ${PLATFORM_SERVLET_MAX_REQUEST_SIZE:10MB}
  ai:
    deepseek:
      api-key: ${PLATFORM_DEEPSEEK_API_KEY:}
      base-url: "${PLATFORM_DEEPSEEK_BASE_URL:https://api.deepseek.com}"
      chat:
        options:
          model: ${PLATFORM_DEEPSEEK_MODEL:deepseek-v4-flash}
          temperature: ${PLATFORM_DEEPSEEK_TEMPERATURE:0.5}

jwt:
  secret: ${PLATFORM_JWT_SECRET:dev-only-change-me-please-use-at-least-32-bytes}
  expiration: ${PLATFORM_JWT_EXPIRATION:604800000}

server:
  port: ${PLATFORM_SERVER_PORT:8442}
  ssl:
    enabled: ${PLATFORM_SSL_ENABLED:true}
    key-store: "${PLATFORM_SSL_KEY_STORE:classpath:springboot-local.p12}"
    key-store-password: ${PLATFORM_SSL_KEY_STORE_PASSWORD:changeit}
    key-store-type: ${PLATFORM_SSL_KEY_STORE_TYPE:PKCS12}
    key-alias: ${PLATFORM_SSL_KEY_ALIAS:1}

xunfei:
  appid: ${PLATFORM_XUNFEI_APPID:}
  api-key: ${PLATFORM_XUNFEI_API_KEY:}
  api-secret: ${PLATFORM_XUNFEI_API_SECRET:}
  tts:
    host-url: "${PLATFORM_XUNFEI_TTS_HOST_URL:https://tts-api.xfyun.cn/v2/tts}"
    tte: ${PLATFORM_XUNFEI_TTS_TTE:UTF8}
    vcn: ${PLATFORM_XUNFEI_TTS_VCN:x4_yezi}
    pitch: ${PLATFORM_XUNFEI_TTS_PITCH:50}
    speed: ${PLATFORM_XUNFEI_TTS_SPEED:50}
  ost:
    appId: "${PLATFORM_XUNFEI_OST_APP_ID:}"
    apiKey: "${PLATFORM_XUNFEI_OST_API_KEY:}"
    apiSecret: "${PLATFORM_XUNFEI_OST_API_SECRET:}"
    upload-url: "${PLATFORM_XUNFEI_OST_UPLOAD_URL:https://upload-ost-api.xfyun.cn/file/upload}"
    create-task-url: "${PLATFORM_XUNFEI_OST_CREATE_TASK_URL:https://ost-api.xfyun.cn/v2/ost/pro_create}"
    query-task-url: "${PLATFORM_XUNFEI_OST_QUERY_TASK_URL:https://ost-api.xfyun.cn/v2/ost/query}"

app:
  swagger:
    enabled: ${PLATFORM_SWAGGER_ENABLED:true}

logging:
  level:
    "[com.multimodal.interview]": ${PLATFORM_LOG_LEVEL:DEBUG}
```

- [ ] **Step 3: Add test config**

Create `backend/src/main/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: "${PLATFORM_DB_URL:jdbc:mysql://localhost:3306/interview_agent_test}"
    username: ${PLATFORM_DB_USERNAME:root}
    password: ${PLATFORM_DB_PASSWORD:}
    driver-class-name: ${PLATFORM_DB_DRIVER_CLASS_NAME:com.mysql.cj.jdbc.Driver}
  data:
    redis:
      host: ${PLATFORM_REDIS_HOST:localhost}
      port: ${PLATFORM_REDIS_PORT:6379}
      password: ${PLATFORM_REDIS_PASSWORD:}
      database: ${PLATFORM_REDIS_DATABASE:1}

jwt:
  secret: ${PLATFORM_JWT_SECRET:test-only-change-me-please-use-at-least-32-bytes}
  expiration: ${PLATFORM_JWT_EXPIRATION:3600000}

server:
  ssl:
    enabled: false

app:
  cors:
    allowed-origins: ${PLATFORM_CORS_ALLOWED_ORIGINS:http://localhost:5173}
  swagger:
    enabled: true
```

- [ ] **Step 4: Add production config**

Create `backend/src/main/resources/application-prod.yml`:

```yaml
spring:
  datasource:
    url: "${PLATFORM_DB_URL}"
    username: ${PLATFORM_DB_USERNAME}
    password: ${PLATFORM_DB_PASSWORD}
    driver-class-name: ${PLATFORM_DB_DRIVER_CLASS_NAME:com.mysql.cj.jdbc.Driver}
  data:
    redis:
      host: ${PLATFORM_REDIS_HOST}
      port: ${PLATFORM_REDIS_PORT:6379}
      password: ${PLATFORM_REDIS_PASSWORD:}
      database: ${PLATFORM_REDIS_DATABASE:0}
  ai:
    deepseek:
      api-key: ${PLATFORM_DEEPSEEK_API_KEY}
      base-url: "${PLATFORM_DEEPSEEK_BASE_URL:https://api.deepseek.com}"

jwt:
  secret: ${PLATFORM_JWT_SECRET}
  expiration: ${PLATFORM_JWT_EXPIRATION:604800000}

server:
  port: ${PLATFORM_SERVER_PORT:8442}
  ssl:
    enabled: ${PLATFORM_SSL_ENABLED:true}
    key-store: "${PLATFORM_SSL_KEY_STORE}"
    key-store-password: ${PLATFORM_SSL_KEY_STORE_PASSWORD}
    key-store-type: ${PLATFORM_SSL_KEY_STORE_TYPE:PKCS12}
    key-alias: ${PLATFORM_SSL_KEY_ALIAS}

xunfei:
  appid: ${PLATFORM_XUNFEI_APPID}
  api-key: ${PLATFORM_XUNFEI_API_KEY}
  api-secret: ${PLATFORM_XUNFEI_API_SECRET}

app:
  cors:
    allowed-origins: ${PLATFORM_CORS_ALLOWED_ORIGINS}
  swagger:
    enabled: ${PLATFORM_SWAGGER_ENABLED:false}

logging:
  level:
    "[com.multimodal.interview]": ${PLATFORM_LOG_LEVEL:INFO}
```

- [ ] **Step 5: Verify no old secrets remain**

Run:

```bash
grep -R "sk-fa7a2c9168a34bc3b38b91c8ea66b102\\|91ff9402ea218f8c8cb7e92e292eb768\\|xQ7\\$pL9\\|12345678" backend/src/main/resources || true
```

Expected: no output.

- [ ] **Step 6: Run backend test compile**

Run:

```bash
cd backend
./mvnw -DskipTests test
```

Expected: build succeeds or fails only on pre-existing external service/database assumptions that are documented before continuing.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/resources/application.yml backend/src/main/resources/application-dev.yml backend/src/main/resources/application-test.yml backend/src/main/resources/application-prod.yml
git commit -m "chore: split backend environment configuration"
```

### Task 2: CORS and Swagger Production Controls

**Files:**
- Modify: `backend/src/main/java/com/multimodal/interview/config/CorsConfig.java`
- Modify: `backend/src/main/java/com/multimodal/interview/config/SecurityConfig.java`
- Test: `backend/src/test/java/com/multimodal/interview/security/SecurityExposureConfigTest.java`

- [ ] **Step 1: Write config test**

Create `SecurityExposureConfigTest.java`:

```java
package com.multimodal.interview.security;

import com.multimodal.interview.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityExposureConfigTest {

    @Test
    void corsUsesConfiguredOriginsWithoutWildcardCredentialPattern() {
        CorsConfig config = new CorsConfig("https://app.example.com,https://admin.example.com");

        CorsConfiguration cors = config.corsConfigurationSource()
                .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/user"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactly("https://app.example.com", "https://admin.example.com");
        assertThat(cors.getAllowedOriginPatterns()).isNullOrEmpty();
        assertThat(cors.getAllowCredentials()).isTrue();
    }
}
```

- [ ] **Step 2: Run test and verify it fails**

Run:

```bash
cd backend
./mvnw -Dtest=SecurityExposureConfigTest test
```

Expected: compilation fails because `CorsConfig(String)` does not exist.

- [ ] **Step 3: Implement configurable CORS**

Replace `CorsConfig` constructor and origin setup:

```java
@Configuration
public class CorsConfig {

    private final String allowedOrigins;

    public CorsConfig(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin",
                "Access-Control-Request-Method", "Access-Control-Request-Headers", "x-user-id"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "x-user-id"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

- [ ] **Step 4: Gate Swagger in SecurityConfig**

Inject `app.swagger.enabled` and build request rules conditionally:

```java
private final boolean swaggerEnabled;

public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                      CorsConfigurationSource corsConfigurationSource,
                      @Value("${app.swagger.enabled:false}") boolean swaggerEnabled) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.corsConfigurationSource = corsConfigurationSource;
    this.swaggerEnabled = swaggerEnabled;
}
```

Inside `authorizeHttpRequests`, keep public auth/read matchers, and only permit Swagger when enabled. If the lambda becomes hard to read, extract a private method:

```java
private void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
    auth.requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/forgot/**", "/api/auth/rsa-public-key").permitAll()
            .requestMatchers("/avatar/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/job-categories/**", "/api/job/**", "/api/jobs/**", "/jobs/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/interview/questions/**", "/interview/questions/**").permitAll();
    if (swaggerEnabled) {
        auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll();
    }
    auth.anyRequest().authenticated();
}
```

- [ ] **Step 5: Run test**

Run:

```bash
cd backend
./mvnw -Dtest=SecurityExposureConfigTest test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/multimodal/interview/config/CorsConfig.java backend/src/main/java/com/multimodal/interview/config/SecurityConfig.java backend/src/test/java/com/multimodal/interview/security/SecurityExposureConfigTest.java
git commit -m "fix: restrict cors and swagger exposure"
```

### Task 3: Remove Forgot Password Code Disclosure

**Files:**
- Modify: `backend/src/main/java/com/multimodal/interview/controller/ForgotPasswordController.java`
- Test: `backend/src/test/java/com/multimodal/interview/security/ForgotPasswordControllerSecurityTest.java`

- [ ] **Step 1: Write failing controller reflection test**

Create test:

```java
package com.multimodal.interview.security;

import com.multimodal.interview.controller.ForgotPasswordController;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ForgotPasswordControllerSecurityTest {

    @Test
    void controllerDoesNotExposeGetEndpointsThatReturnResetCode() {
        boolean hasGetMapping = Arrays.stream(ForgotPasswordController.class.getDeclaredMethods())
                .anyMatch(method -> method.isAnnotationPresent(GetMapping.class));

        assertThat(hasGetMapping).isFalse();
    }
}
```

- [ ] **Step 2: Run test and verify it fails**

Run:

```bash
cd backend
./mvnw -Dtest=ForgotPasswordControllerSecurityTest test
```

Expected: FAIL because `ForgotPasswordController` still has `@GetMapping` methods.

- [ ] **Step 3: Remove code disclosure endpoints**

Delete methods `getCodeByContact`, `getCodeByEmail`, and `getCodeByPhone`. Remove imports for `ResetCodeResponse` and `PasswordResetToken`.

- [ ] **Step 4: Run test**

Run:

```bash
cd backend
./mvnw -Dtest=ForgotPasswordControllerSecurityTest test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/multimodal/interview/controller/ForgotPasswordController.java backend/src/test/java/com/multimodal/interview/security/ForgotPasswordControllerSecurityTest.java
git commit -m "fix: remove password reset code disclosure endpoints"
```

### Task 4: Role Mapping and Admin Write Protection

**Files:**
- Modify: `backend/src/main/java/com/multimodal/interview/common/filter/JwtAuthenticationFilter.java`
- Modify: `backend/src/main/java/com/multimodal/interview/config/SecurityConfig.java`
- Test: `backend/src/test/java/com/multimodal/interview/security/JwtRoleMappingTest.java`

- [ ] **Step 1: Write role mapping test**

Create test:

```java
package com.multimodal.interview.security;

import com.multimodal.interview.common.filter.JwtAuthenticationFilter;
import com.multimodal.interview.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRoleMappingTest {

    @Test
    void mapsUserTypeThreeToAdminRole() {
        User user = new User();
        user.setUserType(3);

        assertThat(JwtAuthenticationFilter.resolveAuthorities(user))
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void mapsOtherUserTypesToUserRole() {
        User user = new User();
        user.setUserType(1);

        assertThat(JwtAuthenticationFilter.resolveAuthorities(user))
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
    }
}
```

- [ ] **Step 2: Run test and verify it fails**

Run:

```bash
cd backend
./mvnw -Dtest=JwtRoleMappingTest test
```

Expected: compilation fails because `resolveAuthorities` does not exist.

- [ ] **Step 3: Implement role mapping helper**

Add to `JwtAuthenticationFilter`:

```java
public static List<SimpleGrantedAuthority> resolveAuthorities(User user) {
    if (user != null && Integer.valueOf(3).equals(user.getUserType())) {
        return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
}
```

Change user details builder:

```java
.authorities(resolveAuthorities(user))
```

- [ ] **Step 4: Protect admin write paths**

In `SecurityConfig`, add write protection before authenticated fallback:

```java
.requestMatchers(HttpMethod.POST, "/api/job-categories/**", "/api/jobs/**").hasRole("ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/job-categories/**", "/api/jobs/**").hasRole("ADMIN")
.requestMatchers(HttpMethod.DELETE, "/api/job-categories/**", "/api/jobs/**").hasRole("ADMIN")
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

Keep GET paths public for job browsing.

- [ ] **Step 5: Run role test**

Run:

```bash
cd backend
./mvnw -Dtest=JwtRoleMappingTest test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/multimodal/interview/common/filter/JwtAuthenticationFilter.java backend/src/main/java/com/multimodal/interview/config/SecurityConfig.java backend/src/test/java/com/multimodal/interview/security/JwtRoleMappingTest.java
git commit -m "fix: map jwt roles and protect admin writes"
```

### Task 5: Frontend Environment Configuration

**Files:**
- Modify: `project/src/utils/api-config.js`
- Create: `project/.env.development`
- Create: `project/.env.production.example`
- Test: manual build command

- [ ] **Step 1: Replace API config**

Use Vite env with a safe development fallback:

```js
const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL

export const BASE_URL = configuredBaseUrl || 'https://localhost:8442'
```

- [ ] **Step 2: Add development env**

Create `project/.env.development`:

```dotenv
VITE_API_BASE_URL=https://localhost:8442
```

- [ ] **Step 3: Add production example**

Create `project/.env.production.example`:

```dotenv
VITE_API_BASE_URL=https://api.example.com
```

- [ ] **Step 4: Run frontend build**

Run:

```bash
cd project
npm run build:h5
```

Expected: build succeeds.

- [ ] **Step 5: Commit**

```bash
git add project/src/utils/api-config.js project/.env.development project/.env.production.example
git commit -m "fix: configure frontend api base url by environment"
```

### Task 6: Frontend Storage and Request Unification

**Files:**
- Modify: `project/src/utils/storage.js`
- Modify: `project/src/utils/request.js`
- Modify: pages with direct `uni.request` found by grep
- Test: `project/src/utils/__tests__/storage.test.js`

- [ ] **Step 1: Add storage helpers**

Extend `storage.js`:

```js
const SESSION_KEYS = new Set(['token', 'userInfo', 'activeUserId', 'currentJobId', 'activeAiConversationChatId'])

export function getStorage(key) {
  try {
    if (SESSION_KEYS.has(key) && typeof window !== 'undefined' && window.sessionStorage) {
      const value = window.sessionStorage.getItem(key)
      if (value) return value
    }
  } catch {}
  if (typeof uni !== 'undefined') {
    return uni.getStorageSync(key)
  }
  return typeof localStorage !== 'undefined' ? localStorage.getItem(key) : null
}

export function setStorage(key, value) {
  try {
    if (SESSION_KEYS.has(key) && typeof window !== 'undefined' && window.sessionStorage) {
      window.sessionStorage.setItem(key, value)
    }
  } catch {}
  if (typeof uni !== 'undefined') {
    return uni.setStorageSync(key, value)
  }
  if (typeof localStorage !== 'undefined') {
    return localStorage.setItem(key, value)
  }
}

export function removeStorage(key) {
  try {
    if (SESSION_KEYS.has(key) && typeof window !== 'undefined' && window.sessionStorage) {
      window.sessionStorage.removeItem(key)
    }
  } catch {}
  if (typeof uni !== 'undefined') {
    return uni.removeStorageSync(key)
  }
  if (typeof localStorage !== 'undefined') {
    return localStorage.removeItem(key)
  }
}

export const getToken = () => getStorage('token')
export const setToken = (token) => setStorage('token', token)
export const removeToken = () => removeStorage('token')
export const getUserInfo = () => getStorage('userInfo')
export const setUserInfo = (userInfo) => setStorage('userInfo', typeof userInfo === 'string' ? userInfo : JSON.stringify(userInfo))
export const removeUserInfo = () => removeStorage('userInfo')
```

- [ ] **Step 2: Update request.js token handling**

Import helpers:

```js
import { getToken, removeStorage } from '@/utils/storage'
```

Remove local `getToken()` implementation. In 401 handling, replace direct `sessionStorage` and `uni.removeStorageSync` calls:

```js
removeStorage('token')
removeStorage('userInfo')
removeStorage('activeUserId')
```

- [ ] **Step 3: Replace direct uni.request calls**

Run:

```bash
grep -R "uni\\.request" project/src --include="*.vue" --include="*.js"
```

For each match, replace:

```js
const response = await uni.request({ url, method: 'GET', header, data })
```

with:

```js
const response = await request({ url, method: 'GET', headers: header, data })
```

When the original code expects `response.data`, use `response.data` from the normalized `ApiResponse`. When it expects raw payload, use `response.data` only after confirming the endpoint returns `ApiResponse`.

- [ ] **Step 4: Add Vitest only if not already present**

Run:

```bash
cd project
npm install -D vitest jsdom
```

Update `package.json` scripts:

```json
"test": "vitest run"
```

- [ ] **Step 5: Add storage test**

Create `project/src/utils/__tests__/storage.test.js`:

```js
import { describe, expect, it, beforeEach } from 'vitest'
import { getStorage, setStorage, removeStorage } from '../storage'

describe('storage helpers', () => {
  beforeEach(() => {
    window.sessionStorage.clear()
    window.localStorage.clear()
  })

  it('stores token in session storage', () => {
    setStorage('token', 'abc')
    expect(getStorage('token')).toBe('abc')
    expect(window.sessionStorage.getItem('token')).toBe('abc')
  })

  it('removes token from session storage', () => {
    setStorage('token', 'abc')
    removeStorage('token')
    expect(getStorage('token')).toBeNull()
  })
})
```

- [ ] **Step 6: Run tests and build**

Run:

```bash
cd project
npm test
npm run build:h5
```

Expected: tests and build pass.

- [ ] **Step 7: Commit**

```bash
git add project/src/utils/storage.js project/src/utils/request.js project/package.json project/package-lock.json project/src/utils/__tests__/storage.test.js
git add project/src/pages project/src/utils
git commit -m "refactor: unify frontend request and storage access"
```

### Task 7: Agent Tool Facade

**Files:**
- Create: `backend/src/main/java/com/multimodal/interview/reactagent/tool/JobAgentTool.java`
- Modify: `backend/src/main/java/com/multimodal/interview/controller/JobController.java`
- Modify: `backend/src/main/java/com/multimodal/interview/reactagent/ReactAgentConfig.java`
- Test: `backend/src/test/java/com/multimodal/interview/reactagent/tool/JobAgentToolTest.java`

- [ ] **Step 1: Add facade test**

Create test:

```java
package com.multimodal.interview.reactagent.tool;

import com.multimodal.interview.service.JobService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JobAgentToolTest {

    @Test
    void delegatesReadOnlyCategoryTreeToService() {
        JobService jobService = mock(JobService.class);
        JobAgentTool tool = new JobAgentTool(jobService);

        tool.getCategoryTree();

        verify(jobService).getCategoryTree();
    }
}
```

- [ ] **Step 2: Run test and verify it fails**

Run:

```bash
cd backend
./mvnw -Dtest=JobAgentToolTest test
```

Expected: compilation fails because `JobAgentTool` does not exist.

- [ ] **Step 3: Create JobAgentTool**

Create:

```java
package com.multimodal.interview.reactagent.tool;

import com.multimodal.interview.entity.Job;
import com.multimodal.interview.entity.JobCategory;
import com.multimodal.interview.service.JobService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobAgentTool {

    private final JobService jobService;

    public JobAgentTool(JobService jobService) {
        this.jobService = jobService;
    }

    @Tool(name = "job_get_category_tree", description = "获取岗位分类树（一级/二级分类），按层级与排序返回")
    public List<JobCategory> getCategoryTree() {
        return jobService.getCategoryTree();
    }

    @Tool(name = "job_get_jobs_by_category", description = "根据二级岗位分类ID获取该分类下的启用岗位列表")
    public List<Job> getJobsByCategory(@ToolParam(description = "二级岗位分类ID") Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return List.of();
        }
        return jobService.getJobsByCategory(categoryId);
    }

    @Tool(name = "job_get_detail", description = "根据岗位ID获取岗位详情")
    public Job getJobDetail(@ToolParam(description = "岗位ID") Long jobId) {
        if (jobId == null || jobId <= 0) {
            return null;
        }
        return jobService.getJobDetail(jobId);
    }

    @Tool(name = "job_get_all_jobs", description = "获取所有启用的岗位列表")
    public List<Job> getAllJobs() {
        return jobService.getAllJobsWithCategory();
    }

    @Tool(name = "job_get_category_name", description = "根据二级岗位分类ID查询岗位分类名称")
    public String getCategoryName(@ToolParam(description = "二级岗位分类ID") Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return "";
        }
        return jobService.getCategoryNameById(categoryId);
    }
}
```

- [ ] **Step 4: Remove tool methods from JobController**

Delete all methods under `// ==================== Agent Tools（供 ReactAgent 调用） ====================` and remove `Tool` imports.

- [ ] **Step 5: Update ReactAgentConfig**

Replace `JobController` tool injection with `JobAgentTool`. If current constructor/bean method accepts `JobController jobController`, change it to `JobAgentTool jobAgentTool` and register that object with the Agent framework.

- [ ] **Step 6: Run test**

Run:

```bash
cd backend
./mvnw -Dtest=JobAgentToolTest test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/multimodal/interview/reactagent/tool/JobAgentTool.java backend/src/main/java/com/multimodal/interview/controller/JobController.java backend/src/main/java/com/multimodal/interview/reactagent/ReactAgentConfig.java backend/src/test/java/com/multimodal/interview/reactagent/tool/JobAgentToolTest.java
git commit -m "refactor: isolate job agent tools from controller"
```

### Task 8: Per User/IP Rate Limiting

**Files:**
- Modify: `backend/src/main/java/com/multimodal/interview/config/RateLimitBucketConfig.java`
- Modify: `backend/src/main/java/com/multimodal/interview/common/interceptor/RateLimitInterceptor.java`
- Test: `backend/src/test/java/com/multimodal/interview/security/RateLimitInterceptorTest.java`

- [ ] **Step 1: Write key resolution test**

Create:

```java
package com.multimodal.interview.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.common.interceptor.RateLimitInterceptor;
import com.multimodal.interview.config.RateLimitBucketConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitInterceptorTest {

    @Test
    void usesForwardedIpForAnonymousRequestKey() {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(new RateLimitBucketConfig(10, 10, 1), new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");

        assertThat(interceptor.resolveRateLimitKey(request)).isEqualTo("ip:203.0.113.10");
    }
}
```

- [ ] **Step 2: Run test and verify it fails**

Run:

```bash
cd backend
./mvnw -Dtest=RateLimitInterceptorTest test
```

Expected: compilation fails because constructor and method signatures do not exist.

- [ ] **Step 3: Implement bucket factory**

Change `RateLimitBucketConfig`:

```java
@Configuration
public class RateLimitBucketConfig {

    private final long capacity;
    private final long refillTokens;
    private final long refillSeconds;

    public RateLimitBucketConfig(
            @Value("${app.rate-limit.capacity:10}") long capacity,
            @Value("${app.rate-limit.refill-tokens:10}") long refillTokens,
            @Value("${app.rate-limit.refill-seconds:1}") long refillSeconds) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillSeconds = refillSeconds;
    }

    public Bucket createNewBucket() {
        Refill refill = Refill.intervally(refillTokens, Duration.ofSeconds(refillSeconds));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }
}
```

- [ ] **Step 4: Implement keyed interceptor**

Change fields and constructor:

```java
private final RateLimitBucketConfig bucketConfig;
private final ObjectMapper objectMapper;
private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

public RateLimitInterceptor(RateLimitBucketConfig bucketConfig, ObjectMapper objectMapper) {
    this.bucketConfig = bucketConfig;
    this.objectMapper = objectMapper;
}
```

Use per-key bucket:

```java
String key = resolveRateLimitKey(request);
Bucket bucket = buckets.computeIfAbsent(key, ignored -> bucketConfig.createNewBucket());
if (bucket.tryConsume(1)) {
    return true;
}
```

Add key resolver:

```java
public String resolveRateLimitKey(HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
        return "user:" + authentication.getName();
    }
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
        return "ip:" + forwardedFor.split(",")[0].trim();
    }
    return "ip:" + request.getRemoteAddr();
}
```

- [ ] **Step 5: Run test**

Run:

```bash
cd backend
./mvnw -Dtest=RateLimitInterceptorTest test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/multimodal/interview/config/RateLimitBucketConfig.java backend/src/main/java/com/multimodal/interview/common/interceptor/RateLimitInterceptor.java backend/src/test/java/com/multimodal/interview/security/RateLimitInterceptorTest.java
git commit -m "fix: rate limit by user or ip"
```

### Task 9: DTO Isolation for High-Risk Responses

**Files:**
- Create: `backend/src/main/java/com/multimodal/interview/dto/UserResponse.java`
- Create: `backend/src/main/java/com/multimodal/interview/dto/JobResponse.java`
- Modify: controllers returning `User` or `Job` in endpoints touched by this pass
- Test: `backend/src/test/java/com/multimodal/interview/dto/UserResponseTest.java`

- [ ] **Step 1: Write DTO test**

Create:

```java
package com.multimodal.interview.dto;

import com.multimodal.interview.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest {

    @Test
    void excludesPasswordFromUserResponse() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("secret");

        UserResponse response = UserResponse.from(user);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("alice");
        assertThat(UserResponse.class.getDeclaredFields())
                .extracting("name")
                .doesNotContain("password");
    }
}
```

- [ ] **Step 2: Run test and verify it fails**

Run:

```bash
cd backend
./mvnw -Dtest=UserResponseTest test
```

Expected: compilation fails because `UserResponse` does not exist.

- [ ] **Step 3: Create UserResponse**

Create:

```java
package com.multimodal.interview.dto;

import com.multimodal.interview.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String nickname,
        String avatarUrl,
        Integer userType,
        String email,
        String phone,
        Integer gender,
        Integer status,
        Integer eyeCareMode,
        Integer surpriseMode,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(), user.getUsername(), user.getNickname(), user.getAvatarUrl(), user.getUserType(),
                user.getEmail(), user.getPhone(), user.getGender(), user.getStatus(), user.getEyeCareMode(),
                user.getSurpriseMode(), user.getLastLoginAt(), user.getCreatedAt(), user.getUpdatedAt()
        );
    }
}
```

- [ ] **Step 4: Create JobResponse**

Create:

```java
package com.multimodal.interview.dto;

import com.multimodal.interview.entity.Job;

public record JobResponse(
        Long id,
        Long categoryId,
        String name,
        String description,
        Integer status,
        Integer sortOrder
) {
    public static JobResponse from(Job job) {
        if (job == null) {
            return null;
        }
        return new JobResponse(
                job.getId(),
                job.getCategoryId(),
                job.getName(),
                job.getDescription(),
                job.getStatus(),
                job.getSortOrder()
        );
    }
}
```

- [ ] **Step 5: Update touched controllers**

For endpoints already edited during this plan, return `UserResponse` or `JobResponse` instead of raw `User`/`Job` where practical. Do not change unrelated controller methods in this task.

- [ ] **Step 6: Run DTO test**

Run:

```bash
cd backend
./mvnw -Dtest=UserResponseTest test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/multimodal/interview/dto/UserResponse.java backend/src/main/java/com/multimodal/interview/dto/JobResponse.java backend/src/test/java/com/multimodal/interview/dto/UserResponseTest.java
git add backend/src/main/java/com/multimodal/interview/controller
git commit -m "refactor: add safe response dtos"
```

### Task 10: CI, Lint, Docker, and Deployment Docs

**Files:**
- Modify: `project/package.json`
- Create: `project/eslint.config.js`
- Create: `project/.prettierrc`
- Create: `.github/workflows/ci.yml`
- Create: `docker-compose.yml`
- Create: `docs/deployment.md`
- Modify: `README.md`

- [ ] **Step 1: Add frontend lint and format dependencies**

Run:

```bash
cd project
npm install -D eslint prettier eslint-plugin-vue @vue/eslint-config-prettier
```

Add scripts:

```json
"lint": "eslint src --ext .js,.vue",
"format": "prettier --write \"src/**/*.{js,vue,scss,css,json}\""
```

- [ ] **Step 2: Add ESLint config**

Create `project/eslint.config.js`:

```js
import pluginVue from 'eslint-plugin-vue'
import prettier from '@vue/eslint-config-prettier'

export default [
  ...pluginVue.configs['flat/recommended'],
  prettier,
  {
    files: ['src/**/*.{js,vue}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module'
    },
    rules: {
      'vue/multi-word-component-names': 'off',
      'no-console': 'off'
    }
  }
]
```

- [ ] **Step 3: Add Prettier config**

Create `project/.prettierrc`:

```json
{
  "semi": false,
  "singleQuote": true,
  "printWidth": 120,
  "trailingComma": "none"
}
```

- [ ] **Step 4: Add CI workflow**

Create `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
  pull_request:

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - name: Backend tests
        working-directory: backend
        run: ./mvnw test

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: npm
          cache-dependency-path: project/package-lock.json
      - name: Install
        working-directory: project
        run: npm ci
      - name: Test
        working-directory: project
        run: npm test -- --runInBand
      - name: Build
        working-directory: project
        run: npm run build:h5

  secret-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Scan obvious committed secrets
        run: |
          ! grep -R "sk-[a-zA-Z0-9]\\{20,\\}\\|PLATFORM_JWT_SECRET:.*[!@#$]" . --exclude-dir=.git
```

- [ ] **Step 5: Add Docker Compose**

Create `docker-compose.yml`:

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
    ports:
      - "8442:8442"
    depends_on:
      - mysql
      - redis

volumes:
  mysql-data:
```

- [ ] **Step 6: Add deployment docs**

Create `docs/deployment.md` with these sections:

```markdown
# Deployment Guide

## Profiles

- `dev`: local development, Swagger enabled, local CORS origin.
- `test`: automated tests.
- `prod`: production, Swagger disabled by default, no sensitive defaults.

## Required Production Environment Variables

- `SPRING_PROFILES_ACTIVE=prod`
- `PLATFORM_DB_URL`
- `PLATFORM_DB_USERNAME`
- `PLATFORM_DB_PASSWORD`
- `PLATFORM_REDIS_HOST`
- `PLATFORM_JWT_SECRET`
- `PLATFORM_DEEPSEEK_API_KEY`
- `PLATFORM_XUNFEI_APPID`
- `PLATFORM_XUNFEI_API_KEY`
- `PLATFORM_XUNFEI_API_SECRET`
- `PLATFORM_CORS_ALLOWED_ORIGINS`

## Local Docker Compose

Run:

```bash
docker compose up
```

The backend listens on `http://localhost:8442` when `PLATFORM_SSL_ENABLED=false`.

## Frontend API URL

Set `VITE_API_BASE_URL` in the frontend environment file.

## Swagger

Swagger is controlled by `PLATFORM_SWAGGER_ENABLED`. Keep it disabled in production.
```

- [ ] **Step 7: Update README**

Add a short link to `docs/deployment.md` and update the port note to say frontend development uses `https://localhost:8442` by default.

- [ ] **Step 8: Run checks**

Run:

```bash
cd project
npm run lint
npm run build:h5
cd ../backend
./mvnw test
```

Expected: all checks pass, or any existing lint violations outside touched files are documented in the final handoff.

- [ ] **Step 9: Commit**

```bash
git add project/package.json project/package-lock.json project/eslint.config.js project/.prettierrc .github/workflows/ci.yml docker-compose.yml docs/deployment.md README.md
git commit -m "chore: add ci docker and deployment guidance"
```

## Final Verification

- [ ] Run backend tests:

```bash
cd backend
./mvnw test
```

- [ ] Run frontend tests and build:

```bash
cd project
npm test
npm run build:h5
```

- [ ] Confirm secrets are absent:

```bash
grep -R "sk-fa7a2c9168a34bc3b38b91c8ea66b102\\|91ff9402ea218f8c8cb7e92e292eb768\\|xQ7\\$pL9\\|12345678" . --exclude-dir=.git || true
```

- [ ] Confirm only expected files are modified:

```bash
git status --short
```

Expected: no unexpected changes; existing user-owned `project/src/pages.json` remains untouched unless the user explicitly asks to handle it.

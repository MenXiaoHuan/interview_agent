# Unified Logging Center Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a lightweight unified logging center with Loki, Promtail, Grafana, backend trace ids, and request logs.

**Architecture:** Docker Compose runs Loki, Promtail, and Grafana alongside existing MySQL, Redis, MinIO, backend, and frontend services. Promtail collects Docker stdout/stderr logs and pushes them to Loki, while Grafana queries Loki. The backend adds `X-Trace-Id`, SLF4J MDC, and one access log entry per request.

**Tech Stack:** Docker Compose, Loki, Promtail, Grafana, Spring Boot 3, Servlet filters, Logback, SLF4J MDC, JUnit 5, Mockito, AssertJ.

---

## File Structure

- Create `ops/loki/loki-config.yml`: Loki single-node local config with filesystem storage under `/loki`.
- Create `ops/promtail/promtail-config.yml`: Promtail Docker discovery config that labels logs with container metadata and pushes to `http://loki:3100/loki/api/v1/push`.
- Create `ops/grafana/provisioning/datasources/loki.yml`: Grafana datasource provisioning for Loki.
- Modify `docker-compose.yml`: add `loki`, `promtail`, `grafana`, `loki-data`, and `grafana-data` for local development.
- Modify `docker-compose.prod.yml`: add the same logging services and volumes for production Compose.
- Modify `.env`: add local logging port and Grafana credential variables. Do not commit `.env`.
- Create `backend/src/main/resources/logback-spring.xml`: standard console pattern that includes `traceId`.
- Create `backend/src/main/java/com/multimodal/interview/common/filter/TraceIdFilter.java`: one filter that creates/propagates `X-Trace-Id` and controls MDC lifecycle.
- Create `backend/src/main/java/com/multimodal/interview/common/filter/RequestLoggingFilter.java`: one filter that writes safe access logs after every request.
- Create `backend/src/test/java/com/multimodal/interview/common/filter/TraceIdFilterTest.java`: focused unit tests for trace id propagation and cleanup.
- Create `backend/src/test/java/com/multimodal/interview/common/filter/RequestLoggingFilterTest.java`: focused unit tests for request log levels, field content, query masking, and exception behavior.
- Create `docs/logging.md`: operating guide for startup, access URLs, Loki queries, trace id usage, and security notes.
- Modify `README.md`: link to the logging guide and list Grafana/Loki ports.

## Task 1: Add Logging Stack Config Files

**Files:**
- Create: `ops/loki/loki-config.yml`
- Create: `ops/promtail/promtail-config.yml`
- Create: `ops/grafana/provisioning/datasources/loki.yml`

- [ ] **Step 1: Create Loki config**

Create `ops/loki/loki-config.yml`:

```yaml
auth_enabled: false

server:
  http_listen_port: 3100
  grpc_listen_port: 9096

common:
  instance_addr: 127.0.0.1
  path_prefix: /loki
  storage:
    filesystem:
      chunks_directory: /loki/chunks
      rules_directory: /loki/rules
  replication_factor: 1
  ring:
    kvstore:
      store: inmemory

query_range:
  results_cache:
    cache:
      embedded_cache:
        enabled: true
        max_size_mb: 100

schema_config:
  configs:
    - from: 2024-01-01
      store: tsdb
      object_store: filesystem
      schema: v13
      index:
        prefix: index_
        period: 24h

ruler:
  alertmanager_url: http://localhost:9093

analytics:
  reporting_enabled: false
```

- [ ] **Step 2: Create Promtail config**

Create `ops/promtail/promtail-config.yml`:

```yaml
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: docker
    docker_sd_configs:
      - host: unix:///var/run/docker.sock
        refresh_interval: 5s
    relabel_configs:
      - source_labels: [__meta_docker_container_name]
        regex: '/(.*)'
        target_label: container_name
      - source_labels: [__meta_docker_container_label_com_docker_compose_service]
        target_label: service
      - source_labels: [__meta_docker_container_label_com_docker_compose_project]
        target_label: compose_project
      - source_labels: [__meta_docker_container_log_stream]
        target_label: stream
```

- [ ] **Step 3: Create Grafana Loki datasource**

Create `ops/grafana/provisioning/datasources/loki.yml`:

```yaml
apiVersion: 1

datasources:
  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
    isDefault: true
    editable: true
```

- [ ] **Step 4: Commit logging stack config files**

Run:

```bash
git add ops/loki/loki-config.yml ops/promtail/promtail-config.yml ops/grafana/provisioning/datasources/loki.yml
git commit -m "chore: add logging stack configs"
```

Expected: commit includes only the three `ops/` config files.

## Task 2: Wire Logging Stack Into Compose

**Files:**
- Modify: `docker-compose.yml`
- Modify: `docker-compose.prod.yml`
- Modify local only: `.env`

- [ ] **Step 1: Add logging variables to `.env`**

Append this group near the backend infrastructure variables in the root `.env`:

```env
# Backend - Logging
PLATFORM_LOKI_HOST_PORT=3100
PLATFORM_GRAFANA_HOST_PORT=3000
PLATFORM_GRAFANA_ADMIN_USER=admin
PLATFORM_GRAFANA_ADMIN_PASSWORD=admin
```

Do not run `git add .env` because `.env` contains local secrets.

- [ ] **Step 2: Update development Compose services**

In `docker-compose.yml`, add these services after `minio` and before `server`:

```yaml
  loki:
    image: grafana/loki:3.2.1
    command: -config.file=/etc/loki/loki-config.yml
    ports:
      - "${PLATFORM_LOKI_HOST_PORT}:3100"
    volumes:
      - ./ops/loki/loki-config.yml:/etc/loki/loki-config.yml:ro
      - loki-data:/loki

  promtail:
    image: grafana/promtail:3.2.1
    command: -config.file=/etc/promtail/promtail-config.yml
    volumes:
      - ./ops/promtail/promtail-config.yml:/etc/promtail/promtail-config.yml:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
    depends_on:
      - loki

  grafana:
    image: grafana/grafana:11.2.2
    environment:
      GF_SECURITY_ADMIN_USER: ${PLATFORM_GRAFANA_ADMIN_USER}
      GF_SECURITY_ADMIN_PASSWORD: ${PLATFORM_GRAFANA_ADMIN_PASSWORD}
    ports:
      - "${PLATFORM_GRAFANA_HOST_PORT}:3000"
    volumes:
      - grafana-data:/var/lib/grafana
      - ./ops/grafana/provisioning/datasources:/etc/grafana/provisioning/datasources:ro
    depends_on:
      - loki
```

Add these volumes under the existing `volumes:` block:

```yaml
  loki-data:
  grafana-data:
```

- [ ] **Step 3: Update production Compose services**

Apply the same `loki`, `promtail`, and `grafana` service definitions to `docker-compose.prod.yml`. Add the same `loki-data` and `grafana-data` volumes under its `volumes:` block. Keep service names and internal URLs identical to development Compose.

- [ ] **Step 4: Validate Compose syntax**

Run:

```bash
docker compose config --quiet
docker compose -f docker-compose.prod.yml config --quiet
```

Expected: both commands exit with code `0` and print no errors.

- [ ] **Step 5: Commit Compose changes**

Run:

```bash
git add docker-compose.yml docker-compose.prod.yml
git commit -m "chore: wire logging services into compose"
```

Expected: commit excludes `.env`.

## Task 3: Add Trace Id Filter With Tests

**Files:**
- Create: `backend/src/test/java/com/multimodal/interview/common/filter/TraceIdFilterTest.java`
- Create: `backend/src/main/java/com/multimodal/interview/common/filter/TraceIdFilter.java`

- [ ] **Step 1: Write failing trace id tests**

Create `backend/src/test/java/com/multimodal/interview/common/filter/TraceIdFilterTest.java`:

```java
package com.multimodal.interview.common.filter;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void usesIncomingTraceIdAndClearsMdcAfterRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/rsa-public-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "client-trace-1");
        AtomicReference<String> traceIdInsideChain = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                traceIdInsideChain.set(MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY)));

        assertThat(traceIdInsideChain).hasValue("client-trace-1");
        assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).isEqualTo("client-trace-1");
        assertThat(MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY)).isNull();
    }

    @Test
    void generatesTraceIdWhenIncomingHeaderIsBlank() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/rsa-public-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(TraceIdFilter.TRACE_ID_HEADER, " ");
        AtomicReference<String> traceIdInsideChain = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                traceIdInsideChain.set(MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY)));

        assertThat(traceIdInsideChain.get()).isNotBlank();
        assertThat(traceIdInsideChain.get()).matches("[0-9a-f\\-]{36}");
        assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).isEqualTo(traceIdInsideChain.get());
        assertThat(MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY)).isNull();
    }
}
```

- [ ] **Step 2: Run tests and verify failure**

Run:

```bash
cd backend
./mvnw -Dtest=TraceIdFilterTest test
```

Expected: compile fails because `TraceIdFilter` does not exist.

- [ ] **Step 3: Implement trace id filter**

Create `backend/src/main/java/com/multimodal/interview/common/filter/TraceIdFilter.java`:

```java
package com.multimodal.interview.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String MDC_TRACE_ID_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request.getHeader(TRACE_ID_HEADER));
        MDC.put(MDC_TRACE_ID_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_ID_KEY);
        }
    }

    private String resolveTraceId(String incomingTraceId) {
        if (incomingTraceId == null || incomingTraceId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return incomingTraceId.trim();
    }
}
```

- [ ] **Step 4: Run trace id tests and verify pass**

Run:

```bash
cd backend
./mvnw -Dtest=TraceIdFilterTest test
```

Expected: `Tests run: 2, Failures: 0, Errors: 0`.

- [ ] **Step 5: Commit trace id filter**

Run:

```bash
git add backend/src/main/java/com/multimodal/interview/common/filter/TraceIdFilter.java backend/src/test/java/com/multimodal/interview/common/filter/TraceIdFilterTest.java
git commit -m "feat: add backend trace id filter"
```

Expected: commit contains only the trace id filter and its tests.

## Task 4: Add Request Logging Filter With Tests

**Files:**
- Create: `backend/src/test/java/com/multimodal/interview/common/filter/RequestLoggingFilterTest.java`
- Create: `backend/src/main/java/com/multimodal/interview/common/filter/RequestLoggingFilter.java`

- [ ] **Step 1: Write failing request logging tests**

Create `backend/src/test/java/com/multimodal/interview/common/filter/RequestLoggingFilterTest.java`:

```java
package com.multimodal.interview.common.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        MDC.put(TraceIdFilter.MDC_TRACE_ID_KEY, "trace-123");
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void logsWarnForClientErrorAndMasksSensitiveQueryValues() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/login");
        request.setQueryString("username=alice&password=secret&token=abc");
        request.addHeader("User-Agent", "JUnit");
        request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("alice", "n/a"));

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                ((MockHttpServletResponse) servletResponse).setStatus(401));

        assertThat(appender.list).hasSize(1);
        ILoggingEvent event = appender.list.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.WARN);
        assertThat(event.getFormattedMessage())
                .contains("access traceId=trace-123")
                .contains("method=GET")
                .contains("path=/api/auth/login")
                .contains("query=username=alice&password=***&token=***")
                .contains("status=401")
                .contains("clientIp=10.0.0.1")
                .contains("user=alice")
                .contains("userAgent=JUnit")
                .contains("error=-");
    }

    @Test
    void logsErrorAndRethrowsWhenRequestProcessingFails() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/interview/start");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new ServletException("boom");
        })).isInstanceOf(ServletException.class)
                .hasMessageContaining("boom");

        assertThat(appender.list).hasSize(1);
        ILoggingEvent event = appender.list.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getFormattedMessage())
                .contains("access traceId=trace-123")
                .contains("method=POST")
                .contains("path=/api/interview/start")
                .contains("status=500")
                .contains("user=anonymous")
                .contains("error=boom");
    }
}
```

- [ ] **Step 2: Run tests and verify failure**

Run:

```bash
cd backend
./mvnw -Dtest=RequestLoggingFilterTest test
```

Expected: compile fails because `RequestLoggingFilter` does not exist.

- [ ] **Step 3: Implement request logging filter**

Create `backend/src/main/java/com/multimodal/interview/common/filter/RequestLoggingFilter.java`:

```java
package com.multimodal.interview.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final Set<String> SENSITIVE_QUERY_KEYS = Set.of(
            "password", "pwd", "token", "access_token", "refresh_token", "jwt",
            "secret", "api_key", "apikey", "key", "authorization"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        Throwable failure = null;
        try {
            filterChain.doFilter(request, response);
        } catch (Throwable ex) {
            failure = ex;
            throw ex;
        } finally {
            writeAccessLog(request, response, startedAt, failure);
        }
    }

    private void writeAccessLog(HttpServletRequest request,
                                HttpServletResponse response,
                                long startedAt,
                                Throwable failure) {
        int status = failure == null ? response.getStatus() : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        long costMs = System.currentTimeMillis() - startedAt;
        String traceId = valueOrFallback(MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY), "-");
        String error = failure == null ? "-" : valueOrFallback(failure.getMessage(), failure.getClass().getSimpleName());
        String message = String.format(
                "access traceId=%s method=%s path=%s query=%s status=%d costMs=%d clientIp=%s user=%s userAgent=%s error=%s",
                traceId,
                request.getMethod(),
                request.getRequestURI(),
                maskQuery(request.getQueryString()),
                status,
                costMs,
                resolveClientIp(request),
                resolveUser(),
                valueOrFallback(request.getHeader("User-Agent"), "-"),
                error
        );

        if (failure != null || status >= 500) {
            log.error(message);
        } else if (status >= 400) {
            log.warn(message);
        } else {
            log.info(message);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return valueOrFallback(request.getRemoteAddr(), "-");
    }

    private String resolveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        String name = authentication.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
            return "anonymous";
        }
        return name;
    }

    private String maskQuery(String query) {
        if (query == null || query.isBlank()) {
            return "-";
        }
        return Arrays.stream(query.split("&"))
                .map(this::maskQueryPart)
                .reduce((left, right) -> left + "&" + right)
                .orElse("-");
    }

    private String maskQueryPart(String part) {
        int separatorIndex = part.indexOf('=');
        if (separatorIndex < 0) {
            return part;
        }
        String rawKey = part.substring(0, separatorIndex);
        String key = decode(rawKey).toLowerCase(Locale.ROOT);
        if (SENSITIVE_QUERY_KEYS.contains(key)) {
            return rawKey + "=***";
        }
        return rawKey + "=" + URLEncoder.encode(decode(part.substring(separatorIndex + 1)), StandardCharsets.UTF_8);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
```

- [ ] **Step 4: Run request logging tests and verify pass**

Run:

```bash
cd backend
./mvnw -Dtest=RequestLoggingFilterTest test
```

Expected: `Tests run: 2, Failures: 0, Errors: 0`.

- [ ] **Step 5: Commit request logging filter**

Run:

```bash
git add backend/src/main/java/com/multimodal/interview/common/filter/RequestLoggingFilter.java backend/src/test/java/com/multimodal/interview/common/filter/RequestLoggingFilterTest.java
git commit -m "feat: add backend request logging"
```

Expected: commit contains only the request logging filter and its tests.

## Task 5: Standardize Backend Console Log Pattern

**Files:**
- Create: `backend/src/main/resources/logback-spring.xml`

- [ ] **Step 1: Create logback config**

Create `backend/src/main/resources/logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <property name="CONSOLE_LOG_PATTERN"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} traceId=%X{traceId:-} - %msg%n"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>

    <logger name="com.multimodal.interview" level="${PLATFORM_LOG_LEVEL:-DEBUG}"/>
</configuration>
```

- [ ] **Step 2: Run backend tests**

Run:

```bash
cd backend
./mvnw test
```

Expected: all backend tests pass and test output lines include the `traceId=` field when logs are emitted.

- [ ] **Step 3: Commit logback config**

Run:

```bash
git add backend/src/main/resources/logback-spring.xml
git commit -m "chore: include trace id in backend logs"
```

Expected: commit contains only `logback-spring.xml`.

## Task 6: Add Logging Documentation

**Files:**
- Create: `docs/logging.md`
- Modify: `README.md`

- [ ] **Step 1: Create logging guide**

Create `docs/logging.md`:

```markdown
# Logging Center

The project uses `Loki + Promtail + Grafana` as a lightweight unified logging center.

## Services

- `loki`: stores log streams and serves queries on `http://localhost:${PLATFORM_LOKI_HOST_PORT}`.
- `promtail`: reads Docker container stdout/stderr logs and pushes them to Loki.
- `grafana`: provides the log query UI on `http://localhost:${PLATFORM_GRAFANA_HOST_PORT}`.

## Start

```bash
docker compose up -d --build
```

## Access

- Grafana: `http://localhost:3000`
- Loki readiness: `http://localhost:3100/ready`
- Default Grafana user: value of `PLATFORM_GRAFANA_ADMIN_USER`
- Default Grafana password: value of `PLATFORM_GRAFANA_ADMIN_PASSWORD`

Change the Grafana password before any non-local deployment.

## Common Loki Queries

```logql
{container_name=~".*server.*"}
{container_name=~".*web.*"}
{container_name=~".*mysql.*"}
{container_name=~".*redis.*"}
{container_name=~".*minio.*"}
{container_name=~".*server.*"} |= "ERROR"
{container_name=~".*server.*"} |= "traceId=<value>"
```

## Trace Id

The backend reads incoming `X-Trace-Id` and returns it in the response. If the request does not provide one, the backend generates a UUID trace id.

Use this command to inspect the response header:

```bash
curl -k -i https://localhost:5172/api/auth/rsa-public-key
```

Then search Grafana with:

```logql
{container_name=~".*server.*"} |= "traceId=<copied-trace-id>"
```

## Security Notes

- Request bodies are not logged.
- Multipart file contents are not logged.
- Query parameters named `password`, `token`, `jwt`, `secret`, `api_key`, `apikey`, `key`, or `authorization` are masked.
- Docker socket is mounted read-only for Promtail.
- `.env` contains local secrets and must not be committed.
```

- [ ] **Step 2: Update README logging section**

Add this concise section to `README.md` near Docker/deployment documentation:

```markdown
### 统一日志中心

项目通过 `Loki + Promtail + Grafana` 管理 Docker 容器日志。默认端口由根目录 `.env` 控制：

- Loki: `http://localhost:3100`
- Grafana: `http://localhost:3000`

后端响应会返回 `X-Trace-Id`，后端日志会包含 `traceId=`，可在 Grafana 中按 trace id 搜索单次请求链路。详细使用方式见 [docs/logging.md](docs/logging.md)。
```

- [ ] **Step 3: Commit docs**

Run:

```bash
git add docs/logging.md README.md
git commit -m "docs: add logging center guide"
```

Expected: commit contains only logging docs and README changes.

## Task 7: Run Full Verification

**Files:**
- No source edits expected.

- [ ] **Step 1: Validate Compose files**

Run:

```bash
docker compose config --quiet
docker compose -f docker-compose.prod.yml config --quiet
```

Expected: both commands exit with code `0`.

- [ ] **Step 2: Run backend tests**

Run:

```bash
cd backend
./mvnw test
```

Expected: all backend tests pass.

- [ ] **Step 3: Start full Docker stack**

Run from the repository root:

```bash
docker compose up -d --build
```

Expected: Docker starts `mysql`, `redis`, `minio`, `loki`, `promtail`, `grafana`, `server`, and `web`.

- [ ] **Step 4: Check container status**

Run:

```bash
docker compose ps
```

Expected: all services are `running` or `healthy`. If `server` or `web` is still downloading dependencies in dev containers, wait until startup completes before continuing.

- [ ] **Step 5: Check Loki and Grafana**

Run:

```bash
curl -fsS http://localhost:3100/ready
curl -fsSI http://localhost:3000/login
```

Expected: Loki returns ready text, and Grafana returns an HTTP success or redirect response.

- [ ] **Step 6: Check backend trace header through frontend proxy**

Run:

```bash
curl -k -i https://localhost:5172/api/auth/rsa-public-key
```

Expected: response status is `200` and headers include `X-Trace-Id: <value>`.

- [ ] **Step 7: Check Loki receives backend logs**

Replace `<trace-id>` with the header value from the previous step and run:

```bash
curl -G -s "http://localhost:3100/loki/api/v1/query" \
  --data-urlencode 'query={container_name=~".*server.*"} |= "traceId=<trace-id>"'
```

Expected: JSON response contains at least one log line for the backend request.

## Task 8: Final Git Review And Push

**Files:**
- No source edits expected unless verification exposed a defect.

- [ ] **Step 1: Review working tree**

Run:

```bash
git status --short
```

Expected: only `.env` may remain modified and untracked runtime artifacts must not be present. Do not add `.env`.

- [ ] **Step 2: Inspect commit history**

Run:

```bash
git log --oneline -5
```

Expected: recent commits include logging config, Compose wiring, backend trace id, request logging, logback config, and docs.

- [ ] **Step 3: Push committed changes**

Run:

```bash
git push origin main
```

Expected: push succeeds to `https://github.com/MenXiaoHuan/interview_agent.git`.

## Self-Review

- Spec coverage: the plan covers Loki, Promtail, Grafana, Compose changes, `.env` variables, `X-Trace-Id`, MDC, request logs, logback pattern, docs, and runtime verification.
- Placeholder scan: the plan contains concrete file paths, code snippets, commands, and expected outcomes for each implementation task.
- Type consistency: filter constants use `TraceIdFilter.TRACE_ID_HEADER` and `TraceIdFilter.MDC_TRACE_ID_KEY` consistently across tests and implementation.
- Security review: the plan avoids body logging, masks sensitive query parameters, mounts Docker socket read-only, keeps Grafana credentials configurable, and excludes `.env` from commit commands.

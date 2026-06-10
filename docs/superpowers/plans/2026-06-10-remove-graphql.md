# Remove GraphQL And Switch To RESTful Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让前后端彻底移除 GraphQL 运行时和桥接层，统一改为真实 RESTful 请求链路，同时保持现有页面与业务流程可用。

**Architecture:** 后端继续复用现有 `controller -> service -> mapper` 结构，只补齐 GraphQL 独占能力的 REST 出口，并在最后删除 GraphQL 适配层。前端保留现有 `api.js` 业务调用面，重写 `request.js` 为纯 REST 客户端，拆掉 `graphql-bridge.js`、`GRAPHQL_URL` 和运行时 patch。迁移按“盘点 -> 补口 -> 切请求层 -> 回归 -> 清理”推进。

**Tech Stack:** Spring Boot 3.4、Spring Security、MyBatis、Swagger/OpenAPI、Uni-App Vue 3、Axios、Maven、npm

---

## File Structure

### Backend

- Modify: `backend/src/main/java/com/multimodal/interview/controller/AuthController.java`
  - 增加 RSA 公钥 REST 接口，替代 GraphQL `rsaPublicKey` 查询。
- Modify: `backend/src/main/java/com/multimodal/interview/config/SecurityConfig.java`
  - 放行新的 RSA 公钥 REST 接口，删除 `/graphql` 放行。
- Modify: `backend/src/main/resources/application.yml`
  - 删除 `spring.graphql.path` 配置。
- Modify: `backend/pom.xml`
  - 删除 GraphQL 相关依赖。
- Delete: `backend/src/main/java/com/multimodal/interview/graphql/GraphQlApiController.java`
- Delete: `backend/src/main/java/com/multimodal/interview/graphql/GraphQlSupport.java`
- Delete: `backend/src/main/java/com/multimodal/interview/config/GraphQlConfig.java`
- Delete: `backend/src/main/java/com/multimodal/interview/config/GraphQlExceptionResolver.java`
- Delete: `backend/src/main/resources/graphql/schema.graphqls`
- Create: `backend/src/test/java/com/multimodal/interview/controller/AuthControllerRestMigrationTest.java`
  - 验证 RSA 公钥 REST 接口与登录/注册接口在无 GraphQL 情况下仍可走通。
- Create: `backend/src/test/java/com/multimodal/interview/config/SecurityConfigRestMigrationTest.java`
  - 验证 `/graphql` 不再暴露，新 REST 入口可访问。

### Frontend

- Modify: `project/src/utils/request.js`
  - 改成真正的 REST 请求执行器，保持现有 `code/message/data` 消费方式。
- Modify: `project/src/utils/api.js`
  - 删除 GraphQL patch，补充 RSA 公钥获取函数，必要时补充会话子资源 REST 方法。
- Modify: `project/src/utils/api-config.js`
  - 删除 `GRAPHQL_URL`。
- Delete: `project/src/utils/graphql-bridge.js`
- Optional Modify: `project/src/pages/interview-ai/index.vue`
  - 如果恢复历史会话需要拆分查询消息/事件/记忆，则接入真实 REST 子接口。

### Docs

- Create: `docs/superpowers/artifacts/2026-06-10-graphql-rest-mapping.md`
  - 记录桥接规则、REST 落点、缺口与验证状态。

---

### Task 1: 产出接口迁移清单并冻结改造范围

**Files:**
- Create: `docs/superpowers/artifacts/2026-06-10-graphql-rest-mapping.md`
- Modify: `docs/superpowers/specs/2026-06-10-remove-graphql-design.md`
- Test: `project/src/utils/graphql-bridge.js`

- [ ] **Step 1: 写出迁移清单文档骨架**

```md
# GraphQL -> REST Mapping

| Method | Frontend URL | GraphQL Operation | Backend REST Target | Gap | Status |
| --- | --- | --- | --- | --- | --- |
| POST | /api/auth/login | login | AuthController#login | none | pending |
| POST | /api/auth/register | register | AuthController#register | none | pending |
| GET | /api/user/profile | me | UserController#getProfile | none | pending |
| GET | /api/auth/rsa-public-key | rsaPublicKey | AuthController#getRsaPublicKey | create | pending |
```

- [ ] **Step 2: 手工对照桥接规则补全首批高风险接口**

```md
| Method | Frontend URL | GraphQL Operation | Backend REST Target | Gap | Status |
| --- | --- | --- | --- | --- | --- |
| GET | /api/agent-conversations | agentConversations | AgentConversationController#listCurrentUserConversations | none | pending |
| POST | /api/agent-conversations/session | upsertAgentConversation | AgentConversationController#upsertCurrentUserConversation | none | pending |
| DELETE | /api/agent-conversations/{chatId} | deleteAgentConversation | AgentConversationController#deleteCurrentUserConversation | none | pending |
| GET | /api/transcription/query-task/{taskId} | queryTranscriptionTask | AudioTranscriptionController#queryTaskResult | verify | pending |
```

- [ ] **Step 3: 校验清单覆盖全部桥接规则**

Run: `grep -n "operationName:" /Users/bytedance/Desktop/interview_agent/project/src/utils/graphql-bridge.js`
Expected: 输出所有 GraphQL operation 名称，且都能在清单中找到对应记录

- [ ] **Step 4: 提交清单文档**

```bash
git add docs/superpowers/artifacts/2026-06-10-graphql-rest-mapping.md docs/superpowers/specs/2026-06-10-remove-graphql-design.md
git commit -m "docs: add graphql to rest migration mapping"
```

### Task 2: 用 REST 替代 GraphQL 的 RSA 公钥能力

**Files:**
- Modify: `backend/src/main/java/com/multimodal/interview/controller/AuthController.java`
- Modify: `backend/src/main/java/com/multimodal/interview/config/SecurityConfig.java`
- Create: `backend/src/test/java/com/multimodal/interview/controller/AuthControllerRestMigrationTest.java`
- Test: `backend/src/main/java/com/multimodal/interview/common/security/RsaPasswordCryptoService.java`

- [ ] **Step 1: 先写失败的控制器测试**

```java
@WebMvcTest(AuthController.class)
class AuthControllerRestMigrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private RsaPasswordCryptoService rsaPasswordCryptoService;

    @Test
    void shouldReturnRsaPublicKey() throws Exception {
        when(rsaPasswordCryptoService.getPublicKeyPem()).thenReturn("-----BEGIN PUBLIC KEY-----demo");

        mockMvc.perform(get("/api/auth/rsa-public-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("-----BEGIN PUBLIC KEY-----demo"));
    }
}
```

- [ ] **Step 2: 运行测试，确认接口尚未实现**

Run: `cd /Users/bytedance/Desktop/interview_agent/backend && ./mvnw -Dtest=AuthControllerRestMigrationTest test`
Expected: FAIL，提示 `No mapping for GET /api/auth/rsa-public-key` 或编译缺少构造注入

- [ ] **Step 3: 在认证控制器中实现新 REST 接口**

```java
private final RsaPasswordCryptoService rsaPasswordCryptoService;

public AuthController(AuthService authService, RsaPasswordCryptoService rsaPasswordCryptoService) {
    this.authService = authService;
    this.rsaPasswordCryptoService = rsaPasswordCryptoService;
}

@Operation(summary = "获取 RSA 公钥", description = "前端在登录、注册、重置密码前获取 RSA 公钥")
@GetMapping("/rsa-public-key")
public ApiResponse<String> getRsaPublicKey() {
    return ApiResponse.success(rsaPasswordCryptoService.getPublicKeyPem());
}
```

- [ ] **Step 4: 放行新的 RSA 公钥接口并收紧 GraphQL**

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers(
                "/api/auth/login",
                "/api/auth/register",
                "/api/auth/forgot/**",
                "/api/auth/rsa-public-key"
        ).permitAll()
        .requestMatchers("/avatar/**").permitAll()
        .requestMatchers("/api/job-categories/**").permitAll()
        .requestMatchers("/api/jobs/**", "/jobs/**").permitAll()
        .requestMatchers("/api/interview/questions/**", "/interview/questions/**").permitAll()
        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
        .anyRequest().authenticated())
```

- [ ] **Step 5: 重新运行测试确认通过**

Run: `cd /Users/bytedance/Desktop/interview_agent/backend && ./mvnw -Dtest=AuthControllerRestMigrationTest test`
Expected: PASS

- [ ] **Step 6: 提交后端基础替换**

```bash
git add backend/src/main/java/com/multimodal/interview/controller/AuthController.java \
  backend/src/main/java/com/multimodal/interview/config/SecurityConfig.java \
  backend/src/test/java/com/multimodal/interview/controller/AuthControllerRestMigrationTest.java
git commit -m "feat: expose rsa public key via rest"
```

### Task 3: 前端改成纯 REST 请求链路

**Files:**
- Modify: `project/src/utils/request.js`
- Modify: `project/src/utils/api.js`
- Modify: `project/src/utils/api-config.js`
- Delete: `project/src/utils/graphql-bridge.js`
- Test: `project/package.json`

- [ ] **Step 1: 在前端请求层先写一个最小失败验证**

```js
// 临时验证目标：request.js 不再 import graphql-bridge.js
import axios from 'axios'
```

Run: `grep -n "graphql-bridge\\|executeRestLikeRequest\\|patchGlobalRequestApis\\|GRAPHQL_URL" /Users/bytedance/Desktop/interview_agent/project/src/utils/request.js /Users/bytedance/Desktop/interview_agent/project/src/utils/api.js /Users/bytedance/Desktop/interview_agent/project/src/utils/api-config.js`
Expected: 仍能看到旧引用，说明切换尚未完成

- [ ] **Step 2: 用 Axios 重写请求核心，保持返回契约兼容**

```js
import axios from 'axios'

const client = axios.create({
  timeout: DEFAULT_TIMEOUT
})

async function coreRequest(config = {}) {
  const headers = {
    ...DEFAULT_HEADERS,
    ...(config.headers || {})
  }

  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await client.request({
    url: config.url,
    method: config.method || 'get',
    params: config.params,
    data: config.data,
    headers,
    timeout: config.timeout || DEFAULT_TIMEOUT,
    responseType: config.responseType || 'json'
  })

  const payload = response.data
  if (payload.code === 200) {
    return payload
  }

  handleErrorPayload(payload.code, payload)
  const error = new Error(payload.message || '请求失败')
  error.response = { status: payload.code, data: payload }
  throw error
}
```

- [ ] **Step 3: 把 RSA 公钥读取从 GraphQL 切到 REST**

```js
export const API = {
  AUTH: {
    RSA_PUBLIC_KEY: `${BASE_URL}/api/auth/rsa-public-key`,
    LOGIN: `${BASE_URL}/api/auth/login`,
    REGISTER: `${BASE_URL}/api/auth/register`
  }
}

export function fetchRsaPublicKey() {
  return request({
    url: API.AUTH.RSA_PUBLIC_KEY,
    method: 'get'
  })
}
```

- [ ] **Step 4: 删除 GraphQL 专属配置与 patch**

```js
// api-config.js
export const BASE_URL = 'https://localhost:8442'

// api.js
import request from '@/utils/request'
import { BASE_URL } from '@/utils/api-config'

export { BASE_URL }
```

- [ ] **Step 5: 删除桥接文件并确认源码无引用**

Run: `grep -R "graphql-bridge\\|executeRestLikeRequest\\|patchGlobalRequestApis\\|GRAPHQL_URL" /Users/bytedance/Desktop/interview_agent/project/src`
Expected: 无输出

- [ ] **Step 6: 构建前端确认基础编译通过**

Run: `cd /Users/bytedance/Desktop/interview_agent/project && npm run build:h5`
Expected: build 成功，无 `graphql-bridge.js` 或 `GRAPHQL_URL` 缺失错误

- [ ] **Step 7: 提交前端链路切换**

```bash
git add project/src/utils/request.js project/src/utils/api.js project/src/utils/api-config.js project/src/utils/graphql-bridge.js
git commit -m "refactor: remove graphql bridge from frontend"
```

### Task 4: 补齐 GraphQL 独占或隐式依赖的 REST 缺口

**Files:**
- Modify: `project/src/utils/api.js`
- Optional Modify: `project/src/pages/interview-ai/index.vue`
- Modify: `backend/src/main/java/com/multimodal/interview/controller/AgentConversationController.java`
- Create: `backend/src/test/java/com/multimodal/interview/config/SecurityConfigRestMigrationTest.java`

- [ ] **Step 1: 为 Agent 会话子资源补全前端 API 常量**

```js
AGENT_CONVERSATIONS: {
  LIST: `${BASE_URL}/api/agent-conversations`,
  UPSERT: `${BASE_URL}/api/agent-conversations/session`,
  DELETE_BY_ID: (chatId) => `${BASE_URL}/api/agent-conversations/${encodeURIComponent(chatId)}`,
  MESSAGES: (chatId) => `${BASE_URL}/api/agent-conversations/${encodeURIComponent(chatId)}/messages`,
  EVENTS: (chatId) => `${BASE_URL}/api/agent-conversations/${encodeURIComponent(chatId)}/events`,
  MEMORY: (chatId) => `${BASE_URL}/api/agent-conversations/${encodeURIComponent(chatId)}/memory`
}
```

- [ ] **Step 2: 如页面需要，补全会话子资源读取函数**

```js
export function fetchAgentConversationMessages(chatId) {
  return request({
    url: API.AGENT_CONVERSATIONS.MESSAGES(chatId),
    method: 'get'
  })
}

export function fetchAgentConversationMemory(chatId) {
  return request({
    url: API.AGENT_CONVERSATIONS.MEMORY(chatId),
    method: 'get'
  })
}
```

- [ ] **Step 3: 加一个安全层回归测试，确保 `/graphql` 已不可访问**

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigRestMigrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldNotExposeGraphQlEndpoint() throws Exception {
        mockMvc.perform(post("/graphql").contentType(MediaType.APPLICATION_JSON).content("{\"query\":\"{ rsaPublicKey }\"}"))
                .andExpect(status().is4xxClientError());
    }
}
```

- [ ] **Step 4: 运行后端回归测试**

Run: `cd /Users/bytedance/Desktop/interview_agent/backend && ./mvnw -Dtest=AuthControllerRestMigrationTest,SecurityConfigRestMigrationTest test`
Expected: PASS

- [ ] **Step 5: 提交 REST 缺口补齐**

```bash
git add project/src/utils/api.js project/src/pages/interview-ai/index.vue \
  backend/src/main/java/com/multimodal/interview/controller/AgentConversationController.java \
  backend/src/test/java/com/multimodal/interview/config/SecurityConfigRestMigrationTest.java
git commit -m "feat: complete rest endpoints for graphql removal"
```

### Task 5: 删除后端 GraphQL 运行时与依赖

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/resources/application.yml`
- Delete: `backend/src/main/java/com/multimodal/interview/graphql/GraphQlApiController.java`
- Delete: `backend/src/main/java/com/multimodal/interview/graphql/GraphQlSupport.java`
- Delete: `backend/src/main/java/com/multimodal/interview/config/GraphQlConfig.java`
- Delete: `backend/src/main/java/com/multimodal/interview/config/GraphQlExceptionResolver.java`
- Delete: `backend/src/main/resources/graphql/schema.graphqls`

- [ ] **Step 1: 删除 Maven 中的 GraphQL 依赖**

```xml
<!-- remove these dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>
<dependency>
    <groupId>com.graphql-java</groupId>
    <artifactId>graphql-java-extended-scalars</artifactId>
    <version>22.0</version>
</dependency>
<dependency>
    <groupId>name.nkonev.multipart-spring-graphql</groupId>
    <artifactId>multipart-spring-graphql</artifactId>
    <version>1.2.1</version>
</dependency>
```

- [ ] **Step 2: 删除应用配置中的 GraphQL 路径**

```yml
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:interview_agent}
  web:
    resources:
      static-locations: "${PLATFORM_STATIC_LOCATIONS:classpath:/static/,classpath:/public/,file:${app.file.root}/}"
```

- [ ] **Step 3: 删除 GraphQL Java 文件与 schema**

Run: `rm /Users/bytedance/Desktop/interview_agent/backend/src/main/java/com/multimodal/interview/graphql/GraphQlApiController.java /Users/bytedance/Desktop/interview_agent/backend/src/main/java/com/multimodal/interview/graphql/GraphQlSupport.java /Users/bytedance/Desktop/interview_agent/backend/src/main/java/com/multimodal/interview/config/GraphQlConfig.java /Users/bytedance/Desktop/interview_agent/backend/src/main/java/com/multimodal/interview/config/GraphQlExceptionResolver.java /Users/bytedance/Desktop/interview_agent/backend/src/main/resources/graphql/schema.graphqls`
Expected: 文件删除成功

- [ ] **Step 4: 运行后端测试与编译**

Run: `cd /Users/bytedance/Desktop/interview_agent/backend && ./mvnw test`
Expected: PASS

Run: `cd /Users/bytedance/Desktop/interview_agent/backend && ./mvnw -q -DskipTests package`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交 GraphQL 清理**

```bash
git add backend/pom.xml backend/src/main/resources/application.yml \
  backend/src/main/java/com/multimodal/interview/config/SecurityConfig.java \
  backend/src/test/java/com/multimodal/interview/controller/AuthControllerRestMigrationTest.java \
  backend/src/test/java/com/multimodal/interview/config/SecurityConfigRestMigrationTest.java
git add -u backend/src/main/java/com/multimodal/interview/graphql backend/src/main/java/com/multimodal/interview/config backend/src/main/resources/graphql
git commit -m "refactor: remove graphql runtime from backend"
```

### Task 6: 全链路验证与最终收尾

**Files:**
- Modify: `docs/superpowers/artifacts/2026-06-10-graphql-rest-mapping.md`
- Test: `backend`
- Test: `project`

- [ ] **Step 1: 验证仓库中已无 GraphQL 运行时代码引用**

Run: `grep -R "spring-boot-starter-graphql\\|GraphQl\\|/graphql\\|GRAPHQL_URL\\|graphql-bridge" /Users/bytedance/Desktop/interview_agent/backend /Users/bytedance/Desktop/interview_agent/project`
Expected: 仅剩设计文档、计划文档或注释中的历史说明；源码中无有效运行时引用

- [ ] **Step 2: 启动后端并验证关键 REST 接口**

Run: `cd /Users/bytedance/Desktop/interview_agent/backend && ./mvnw spring-boot:run`
Expected: 应用启动成功，监听 `https://localhost:8442`

Run: `curl -k https://localhost:8442/api/auth/rsa-public-key`
Expected: 返回 `{"code":200,"message":"success","data":"-----BEGIN PUBLIC KEY-----..."}`

- [ ] **Step 3: 启动前端并手工验证主流程**

Run: `cd /Users/bytedance/Desktop/interview_agent/project && npm run dev:h5`
Expected: H5 开发服务启动成功

手工验证清单：

```md
- 登录页打开后网络面板中不出现 /graphql
- 注册成功后 token 与 userInfo 正常写入 sessionStorage
- 个人中心修改昵称、绑定邮箱、修改密码正常
- 岗位分类与岗位列表可查
- 选择题、场景题、简历提取、综合报告正常
- AI 会话列表与保存/删除正常
```

- [ ] **Step 4: 更新清单状态并完成最终提交**

```md
| Method | Frontend URL | GraphQL Operation | Backend REST Target | Gap | Status |
| --- | --- | --- | --- | --- | --- |
| GET | /api/auth/rsa-public-key | rsaPublicKey | AuthController#getRsaPublicKey | resolved | done |
```

```bash
git add docs/superpowers/artifacts/2026-06-10-graphql-rest-mapping.md docs/superpowers/plans/2026-06-10-remove-graphql.md
git commit -m "docs: finalize graphql removal verification"
```

## Self-Review

- Spec coverage:
  - 设计文档中的“接口盘点与分类”由 Task 1 覆盖。
  - “补齐和校准后端 REST 接口”由 Task 2、Task 4 覆盖。
  - “前端请求层切换”由 Task 3 覆盖。
  - “模块化联调与回归”由 Task 6 覆盖。
  - “删除 GraphQL 栈”由 Task 5 覆盖。
- Placeholder scan:
  - 计划中没有使用任何占位式任务描述，所有步骤都已细化为可执行动作。
  - 每个实现步骤都给出文件、代码片段或精确命令。
- Type consistency:
  - RSA 公钥 REST 接口统一命名为 `getRsaPublicKey()` / `/api/auth/rsa-public-key`。
  - 前端请求层统一保持 `code/message/data` 契约，不引入第二套响应结构。
  - GraphQL 删除统一落在 `pom.xml`、`application.yml`、GraphQL Java 文件和 schema 四类资产上。

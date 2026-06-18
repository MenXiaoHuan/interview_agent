# Interview Agent 安全与工程质量改造设计

## 背景

当前项目已经具备较完整的 AI 面试训练业务能力，但在生产化、安全治理、配置管理、权限控制、前端请求一致性、测试与交付工程方面存在明显短板。本设计覆盖 P0 到 P3 优化项，采用分阶段连续推进方式，保证每个阶段完成后项目仍可运行、可测试、可回滚。

## 目标

- 移除敏感默认配置，降低密钥泄露与账号接管风险。
- 统一后端环境配置、前端 API 配置和联调端口。
- 建立更清晰的认证、授权、CORS、Swagger 暴露策略。
- 收敛前端请求和存储入口，减少绕过统一拦截器的代码路径。
- 将 Agent Tool 与 Web Controller 解耦，降低模型触发副作用接口的风险。
- 将限流从全局共享桶升级为用户/IP 粒度，减少误伤。
- 补充测试、CI、Docker Compose、代码风格和部署文档，提升交付可靠性。

## 非目标

- 不重写业务页面 UI。
- 不更换前后端主技术栈。
- 不一次性重构所有 Controller、Service 和页面大文件。
- 不引入外部密钥管理平台实现，只预留环境变量/部署注入方式。

## 分阶段方案

### 阶段 1：后端安全与配置治理

- 移除 `application.yml` 中数据库密码、JWT Secret、DeepSeek Key、讯飞 Key 等敏感默认值。
- 拆分 `application-dev.yml`、`application-test.yml`、`application-prod.yml`，默认配置只保留非敏感公共项。
- 删除忘记密码验证码查询接口，保留发送验证码和重置密码流程。
- 将 CORS 改为从配置读取允许来源，开发环境允许本地前端，生产环境必须显式配置域名。
- 将 Swagger/OpenAPI 暴露行为绑定到配置项或 profile，生产环境默认关闭。
- 在 JWT 认证过滤器中根据用户类型映射 `ROLE_USER`、`ROLE_ADMIN`。
- 对岗位、题库、反馈等管理写接口增加角色校验，优先使用 Spring Security 方法级鉴权或路径级匹配。

### 阶段 2：前端环境与请求治理

- 将 `BASE_URL` 从硬编码改为 `import.meta.env.VITE_API_BASE_URL`。
- 增加 `.env.development`、`.env.production.example`，默认开发地址与后端 `8442` 对齐。
- 迁移页面中的直接 `uni.request` 调用，统一走 `utils/request.js` 或领域 API 模块。
- 将 token、用户信息、岗位 ID、会话 ID 等读写逐步收敛到 `utils/storage.js`。
- 保留已有业务行为，不改变页面跳转和接口协议。

### 阶段 3：后端结构治理

- 为高风险实体返回增加 Response DTO，优先处理 `User`、`Job`、`Feedback` 等可能泄露字段或耦合数据库结构的接口。
- 新增 Agent Tool Facade，只暴露只读、幂等、参数受控的方法给 Agent。
- 从 `ReactAgentConfig` 中移除直接注入 Controller 作为工具的方式。
- 将限流器改为按认证用户 ID 或客户端 IP 创建独立 Bucket。
- 保留当前限流阈值配置能力，后续可扩展为 Redis 分布式限流。

### 阶段 4：质量与交付治理

- 补充后端安全回归测试：验证码不返回、管理接口鉴权、角色映射、CORS 配置、Swagger 生产关闭、限流粒度。
- 补充前端基础测试：API base URL 配置、请求封装、storage 封装。
- 增加 CI 配置，至少包含后端测试/打包、前端安装/构建、密钥扫描和依赖检查入口。
- 增加 Docker Compose，提供 MySQL、Redis、后端、前端本地编排模板。
- 增加 ESLint/Prettier 基础配置，先以可落地的轻量规则为主。
- 更新接口文档和部署文档，说明环境变量、端口、HTTPS、Swagger、Docker Compose 使用方式。

## 配置设计

后端配置分为公共配置和环境配置：

- `application.yml`：应用名、公共 MyBatis 配置、配置项占位符、默认 profile。
- `application-dev.yml`：本地数据库、Redis、端口、SSL、CORS、本地 Swagger 开启。
- `application-test.yml`：测试环境配置，尽量使用内存或测试替身配置。
- `application-prod.yml`：生产环境配置，敏感项无默认值，Swagger 默认关闭，CORS 必须显式配置。

前端配置分为：

- `.env.development`：`VITE_API_BASE_URL=https://localhost:8442`。
- `.env.production.example`：提供生产接口地址示例，不提交真实域名或密钥。
- `api-config.js`：只负责读取和校验环境变量。

## 权限设计

- 登录态仍使用 JWT。
- JWT 认证过滤器查询用户后，根据 `userType` 生成角色。
- 管理员角色统一为 `ROLE_ADMIN`，普通用户为 `ROLE_USER`。
- 管理类写接口要求 `ROLE_ADMIN`。
- 公开读接口保持匿名可访问，例如岗位分类树、岗位列表、岗位详情。
- Swagger 在生产环境默认不可访问，避免接口结构暴露。

## Agent Tool 设计

- 新增专用 Tool Facade，例如 `JobAgentTool`、`ResumeAgentTool`、`QuestionAgentTool`。
- Tool Facade 调用 Service，不调用 Controller。
- Tool 只保留只读或评估类能力，不暴露创建、更新、删除等管理操作。
- Tool 入参做基本校验，避免模型传入异常参数直接穿透到业务层。

## 限流设计

- 请求优先使用认证用户作为限流 key。
- 未认证请求使用客户端 IP 作为限流 key。
- 令牌桶按 key 独立创建和缓存。
- 初期使用本地内存实现，保持实现简单。
- 如果部署多实例，后续再迁移为 Redis/Redisson 分布式限流。

## 测试设计

- 后端使用 JUnit 与 Spring Boot Test。
- 安全测试优先覆盖 P0/P1 变更，确保高危问题不回归。
- 前端测试优先覆盖纯工具函数和请求配置，避免一开始引入大量 UI 测试成本。
- CI 以构建通过和关键测试通过为最低门槛。

## 风险与缓解

- 权限收紧可能导致前端管理后台请求失败：通过明确管理员 token 和接口路径测试缓解。
- 配置拆分可能导致本地启动失败：保留开发环境示例和 README 更新。
- 前端请求迁移可能改变错误处理：优先迁移少量高频路径并保持响应结构不变。
- Agent Tool 解耦可能影响 AI 能力：先迁移只读岗位工具，确认后再迁移其他工具。
- 限流按用户/IP 可能产生代理 IP 误判：保留 `X-Forwarded-For` 解析，并在部署文档中说明反向代理配置。

## 验收标准

- 仓库中不再包含真实或可用的第三方 API Key、JWT Secret、数据库密码默认值。
- 忘记密码接口不再返回验证码。
- 生产环境 Swagger 默认关闭。
- 生产环境 CORS 不允许通配任意来源携带凭证。
- 管理写接口非管理员不可访问。
- 前端默认 API 地址与后端开发端口一致。
- 直接 `uni.request` 调用被逐步替换或明确纳入统一封装。
- Agent 不再直接通过 Controller 暴露工具能力。
- 限流按用户/IP 生效。
- 后端和前端至少具备基础自动化测试与 CI 入口。
- Docker Compose 和部署文档可以支持本地一键联调。

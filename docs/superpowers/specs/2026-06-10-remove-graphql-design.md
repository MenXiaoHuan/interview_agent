# Interview Agent GraphQL 下线与 RESTful 迁移设计

## 1. 背景与目标

当前项目同时存在两套“接口表象”：

- 后端存在一套 GraphQL 运行时，入口为 `/graphql`。
- 前端业务代码大多以 `/api/**`、`/scenario/**` 形式发起“REST 风格请求”。
- 但前端请求层并未真正直连这些 REST 接口，而是通过 `project/src/utils/graphql-bridge.js` 在运行时把请求翻译成 GraphQL 查询和变更，再统一发送到 `/graphql`。

因此，本次工作的真实目标不是简单删除 GraphQL 文件，而是完成如下迁移：

- 前端只保留真实 RESTful 请求链路。
- 后端只保留 REST Controller 作为对外接口层。
- 删除 GraphQL 适配层、Schema、运行时配置和依赖。
- 保持现有业务逻辑、鉴权方式、统一返回结构和大部分前端调用方式尽量不变。

## 2. 现状结论

### 2.1 后端 GraphQL 的定位

后端 GraphQL 不是独立业务实现，而是一层适配层。

`backend/src/main/java/com/multimodal/interview/graphql/GraphQlApiController.java` 通过注入现有 REST Controller，将 GraphQL Query/Mutation 直接转发到：

- `AuthController`
- `UserController`
- `JobController`
- `ChoiceQuestionController`
- `ScenarioQuestionController`
- `ResumeController`
- `ComprehensiveHistoryController`
- `FeedbackController`
- `BlessingController`
- `ChatController`
- `AgentController`
- `AgentConversationController`
- `SpeechSynthesisController`
- `AudioTranscriptionController`
- `FacialAnalysisController`
- 排行榜相关 Controller

这意味着：

- 绝大多数业务逻辑已经存在于 Service 和现有 Controller 中。
- 迁移的核心成本在“接口出入口调整”，而不是“业务重写”。
- GraphQL 下线的前提，是前端请求真实命中 REST Controller，并且返回契约与页面预期保持兼容。

### 2.2 前端 GraphQL 的定位

前端不是直接写 GraphQL 页面逻辑，而是把 REST 风格 API 调用桥接到 GraphQL。

关键文件：

- `project/src/utils/api.js`
- `project/src/utils/request.js`
- `project/src/utils/graphql-bridge.js`
- `project/src/utils/api-config.js`

其中：

- `api.js` 暴露的是 REST 风格 URL 常量和业务 API 方法。
- `request.js` 并没有直接走标准 HTTP 客户端，而是调用 `executeRestLikeRequest()`。
- `graphql-bridge.js` 负责判断 URL 是否需要桥接、构造 GraphQL operation、发往 `/graphql`、把结果重新包装为现有 `ApiResponse` 风格。

这说明：

- 页面层多数无需感知 GraphQL 语法。
- 前端迁移的关键是移除桥接逻辑，并确保这些 URL 对应的真实 REST 接口全部存在。

### 2.3 GraphQL 相关技术资产

本次最终需要移除或停用的 GraphQL 资产包括：

- `backend/src/main/resources/graphql/schema.graphqls`
- `backend/src/main/java/com/multimodal/interview/graphql/GraphQlApiController.java`
- `backend/src/main/java/com/multimodal/interview/graphql/GraphQlSupport.java`
- `backend/src/main/java/com/multimodal/interview/config/GraphQlConfig.java`
- `backend/src/main/java/com/multimodal/interview/config/GraphQlExceptionResolver.java`
- `backend/src/main/resources/application.yml` 中 `spring.graphql.path`
- `backend/src/main/java/com/multimodal/interview/config/SecurityConfig.java` 中 `/graphql` 放行
- `backend/pom.xml` 中 GraphQL 相关依赖

## 3. 迁移原则

本次迁移遵循以下原则：

1. 先补齐 REST，再切前端，不做无落点的直接删除。
2. 优先复用现有 Controller、Service、DTO、统一返回结构。
3. 页面代码尽量少改，主要改请求层和少量接口参数适配。
4. 分阶段推进，每个阶段都能独立验证。
5. 在全部页面与流程验证通过前，不删除 GraphQL 代码。
6. 不顺带做与迁移无关的大规模重构。

## 4. 推荐迁移方案

推荐采用“分阶段切换”。

### 方案 A：分阶段切换（推荐）

步骤：

1. 盘点 GraphQL 与 REST 的映射关系。
2. 补齐真实 REST 接口。
3. 让前端请求层切到直连 REST。
4. 逐模块回归验证。
5. 最后删除 GraphQL 代码和依赖。

优点：

- 风险最低。
- 可分模块验收。
- 问题定位更清晰。
- 后端和前端可以交错推进。

缺点：

- 阶段更多，周期略长。
- 迁移中短期内会同时存在 GraphQL 与 REST 两套入口。

### 方案 B：一次性切换

步骤：

- 同时改后端接口与前端请求层，直接彻底切换到 REST。

优点：

- 迁移周期短。
- 不会长期保留双入口。

缺点：

- 联调风险高。
- 一旦接口清单遗漏，会造成多个页面同时故障。
- 回滚和排障成本高。

### 方案 C：长期保留 GraphQL 兼容层

优点：

- 改动最小。

缺点：

- 不符合本次“去除 GraphQL”的目标。
- 技术债保留，维护成本持续存在。

结论：采用方案 A。

## 5. 目标架构

迁移完成后的目标架构如下：

### 5.1 后端

- 所有对外能力由 REST Controller 暴露。
- Controller 继续调用现有 Service 层。
- 返回统一维持 `ApiResponse` 风格，避免页面错误处理逻辑大改。
- 鉴权继续沿用 JWT + Spring Security。
- Swagger/OpenAPI 作为后续接口联调与文档来源。

### 5.2 前端

- 页面和 store 只调用 REST API。
- `request.js` 使用纯 HTTP 请求链路。
- 删除 `graphql-bridge.js` 的 URL 转译、GraphQL operation 构造、GraphQL multipart 上传处理。
- 删除 `GRAPHQL_URL` 与相关运行时 patch 逻辑。

### 5.3 删除项

- `/graphql` 入口
- GraphQL schema
- GraphQL scalar 配置
- GraphQL 异常解析器
- GraphQL multipart 适配
- GraphQL 依赖和安全放行

## 6. 分阶段实施设计

### 阶段 1：接口盘点与分类

目标：确认前端所有“看起来是 REST”的接口，哪些是真 REST，哪些只是桥接产物。

输出物：

- 一份完整接口迁移清单。
- 每条记录至少包含：
  - 前端调用路径
  - 请求方法
  - 当前 GraphQL operation
  - 当前实际落到的 Controller 方法
  - 是否已有真实 REST 端点
  - 返回结构差异
  - 所属业务模块

接口来源：

- `project/src/utils/graphql-bridge.js`
- `project/src/utils/api.js`
- `backend/src/main/resources/graphql/schema.graphqls`
- `backend/src/main/java/com/multimodal/interview/graphql/GraphQlApiController.java`

验收标准：

- 所有桥接规则都有清单记录。
- 不存在“前端有调用，但后端真实 REST 不明确”的遗漏项。

### 阶段 2：补齐和校准后端 REST 接口

目标：让前端所有 URL 都能真实命中 REST Controller。

实施策略：

- 能复用现有 Controller 的，直接复用。
- GraphQL 目前能做、但 REST 尚未暴露的能力，新增或补齐 REST 端点。
- 保持参数命名与前端已有调用尽量兼容，优先减少页面改动。
- 返回结构统一包装为 `ApiResponse`。

重点检查模块：

- 认证与用户
- 岗位与分类
- 选择题与场景题
- 简历分析与综合评估
- 反馈、祝福、聊天大厅
- Agent 会话与大模型能力
- 语音合成、音频转写、人脸分析
- 排行榜

验收标准：

- `graphql-bridge.js` 中每一条桥接规则都能找到一个真实 REST 落点。
- 本阶段完成后，理论上可以在不依赖 GraphQL 的情况下提供全部功能。

### 阶段 3：前端请求层切换

目标：让前端停止把 REST 风格请求转成 GraphQL。

改造点：

- `project/src/utils/request.js`
  - 移除 `executeRestLikeRequest()` 依赖。
  - 改成真正的 HTTP 请求实现。
- `project/src/utils/api.js`
  - 移除 `patchGlobalRequestApis()` 调用。
  - 保留现有 API 常量与业务方法，优先不改页面调用层。
- `project/src/utils/api-config.js`
  - 删除 `GRAPHQL_URL`。
- 删除 `project/src/utils/graphql-bridge.js`。

说明：

- 页面层的目标是“无感迁移”，尽量不改业务页面逻辑。
- 如果某些接口过去依赖 GraphQL 的字段整形，则只对这些点做小范围兼容调整。

验收标准：

- 前端运行时不再向 `/graphql` 发起请求。
- 页面仍能按原方式通过 `api.js` 提供的方法完成业务。

### 阶段 4：模块化联调与回归

推荐按以下顺序验证：

1. `auth/user`
2. `job/category`
3. `choice-question/scenario-question`
4. `resume/comprehensive`
5. `feedback/blessing/chat`
6. `agent/agent-conversation`
7. `speech/transcription/face`
8. `ranking`

每个模块验证内容：

- 列表查询
- 详情查询
- 新增/更新/删除
- 权限控制
- 错误提示
- 文件上传下载
- 长耗时接口超时与失败处理

验收标准：

- 浏览器网络面板中无 `/graphql` 请求。
- 主流程可用。
- 错误码、Toast、登录失效跳转逻辑正常。

### 阶段 5：删除 GraphQL 栈

删除内容：

- `backend/src/main/resources/graphql/`
- `backend/src/main/java/com/multimodal/interview/graphql/`
- GraphQL 配置类
- GraphQL 依赖
- `/graphql` 安全放行
- `application.yml` 中 GraphQL 配置

验收标准：

- 后端编译通过。
- 前端联调通过。
- 搜索全仓库不再存在有效 GraphQL 运行时依赖。

## 7. 接口迁移重点清单

以下为首批应重点关注的接口族：

### 7.1 认证与用户

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/forgot/send-code`
- `POST /api/auth/forgot/reset`
- `GET /api/auth/forgot/code`
- `GET /api/user/profile`
- `PUT /api/user/eyemode`
- `PUT /api/user/surprisemode`
- `PUT /api/user/nickname`
- `POST /api/user/avatar`
- `PUT /api/user/password`
- `PUT /api/user/email`
- `PUT /api/user/phone`
- 用户管理相关更新与删除接口

特殊点：

- 当前 RSA 公钥通过 GraphQL `rsaPublicKey` 获取。
- 迁移后需提供独立 REST 接口，例如 `GET /api/auth/rsa-public-key`，用于前端密码加密流程。

### 7.2 岗位与分类

- 分类树、分类名、分类增删改
- 岗位列表、岗位详情、岗位增删改

特殊点：

- 需确认 `/api/job` 与 `/api/jobs` 的职责边界，避免命名混乱长期保留。

### 7.3 题库与面试记录

- 选择题列表、按岗位查询、增删改
- 面试提交
- 面试结果
- 用户历史记录
- 场景题列表、按岗位查询、增删改

特殊点：

- 某些删除接口当前使用 `POST`，迁移时可分两步：
  - 第一阶段先兼容原路径和原方法，确保页面不改。
  - 第二阶段再规范为 `DELETE`。

### 7.4 简历与综合评估

- 简历上传
- 简历提取
- 简历历史保存/查询/详情/恢复
- 综合评估的简历、问题、场景、报告写入与查询

特殊点：

- 上传和恢复接口要重点验证 multipart、超时、返回格式。

### 7.5 Agent 与 AI 能力

- 统一 Agent 调用
- Agent 会话列表、会话保存、会话删除
- 场景分析、综合报告分析、问题分析等上层 AI 能力

特殊点：

- 这类接口超时时间较长。
- 结构化响应当前部分依赖字符串转对象的前端兼容逻辑，REST 返回建议统一成 JSON 对象。

### 7.6 媒体能力

- 语音合成
- 音频上传
- 创建转写任务
- 查询转写任务
- 人脸分析

特殊点：

- GraphQL multipart 删除后，需确保 REST multipart 流程完全可替代。

## 8. 数据与契约兼容要求

为避免页面大面积改动，迁移时要求：

1. REST 接口继续返回 `code`、`message`、`data` 结构。
2. 鉴权失败继续返回 401，权限不足返回 403。
3. 上传类接口继续按前端现有消费方式返回。
4. AI 接口尽量直接返回结构化 JSON，减少前端再次 `JSON.parse` 的兜底逻辑。
5. 时间字段格式保持稳定，避免因 GraphQL scalar 改为 REST 后出现时区或格式差异。

## 9. 风险与应对

### 风险 1：前端 URL 存在“假 REST”

表现：

- 代码里看起来调的是 `/api/**`，但后端并无真实接口。

应对：

- 以 `graphql-bridge.js` 为准进行全量盘点。
- 删除桥接前必须确认每条规则都有 REST 落点。

### 风险 2：返回结构差异导致页面异常

表现：

- 列表、详情、字段命名、嵌套对象和时间格式变化导致页面渲染出错。

应对：

- 先以兼容为主，保持返回结构稳定。
- 如果要顺手规范接口结构，放在迁移完成后单独治理。

### 风险 3：上传能力回归失败

表现：

- 简历提取、音频上传、人脸分析失败。

应对：

- 单独设计上传接口回归用例。
- 优先完成这些接口的真实 REST 验证后，再删除 GraphQL multipart 依赖。

### 风险 4：鉴权与错误处理退化

表现：

- 登录失效不跳转、错误 Toast 文案异常、权限控制不一致。

应对：

- 保持现有 `request.js` 对错误码的处理规则不变。
- REST 层遵循当前错误码约定。

### 风险 5：一次性删得过早

表现：

- 删掉 GraphQL 后才发现仍有页面暗中依赖桥接逻辑。

应对：

- 必须在网络层确认 `/graphql` 请求已归零后，再执行最终删除。

## 10. 验收标准

迁移完成的验收标准如下：

1. 前端运行时不再发送任何 `/graphql` 请求。
2. 所有页面主流程可用。
3. 所有上传与长耗时能力可用。
4. 后端不再包含 GraphQL Controller、Schema、配置和依赖。
5. 安全配置中不再存在 `/graphql` 放行。
6. 编译通过，联调通过，关键流程通过人工回归。

## 11. 推荐执行顺序

推荐按以下顺序实施：

1. 输出正式接口迁移清单。
2. 后端补齐 REST 落点。
3. 前端请求层切换。
4. 按模块逐步回归。
5. 删除 GraphQL 栈。

## 12. 非目标

本次迁移不包含以下内容：

- 全量重命名所有历史接口路径
- 统一重构所有 Controller 风格
- 重写 Service、Mapper、数据库结构
- 顺带替换 JWT、权限系统、AI 调用框架
- 与 GraphQL 下线无直接关系的页面重构

## 13. 下一步产物

在本设计确认后，下一步应输出一份可执行实现计划，至少包含：

- 详细任务拆分
- 每阶段改动文件范围
- 接口迁移清单模板
- 联调与回归清单
- 删除 GraphQL 的最终检查项

当前已补充的配套产物：

- `docs/superpowers/artifacts/2026-06-10-graphql-rest-mapping.md`

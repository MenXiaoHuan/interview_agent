# MinIO 头像存储与 Compose 前端环境设计

## 背景

当前头像上传由后端将文件保存到本地目录 `app.file.root/avatar`，再返回 `/avatar/{filename}` 形式的本地静态资源地址。该方式在单机本地开发可用，但在 Docker、服务器、多实例部署和数据持久化场景下不够稳定。用户已确认改为 MinIO 方案 A：公共读 bucket，数据库保存公开 URL，前端直接展示。

前端当前通过 `.env.development` 提供 `VITE_API_BASE_URL`，适合本地直接运行 `npm run dev:h5`。用户希望也能将前端变量放入 `docker-compose.yml`，用于一键启动全套环境。

## 目标

- 将头像文件从本地磁盘存储迁移到 MinIO。
- 使用公共读 bucket，上传成功后返回公开可访问 URL。
- 保持数据库仍保存 `user.avatar_url`，前端展示逻辑尽量不变。
- 在 `docker-compose.yml` 中新增 MinIO 服务。
- 在 `docker-compose.yml` 中新增前端服务，并通过 Compose 注入 `VITE_API_BASE_URL`。
- 保留 `.env.development`，支持不通过 Docker 直接运行前端。
- 更新部署文档，说明 MinIO、前端环境变量和本地一键启动方式。

## 非目标

- 不迁移简历、音频等其他上传能力。
- 不实现私有 bucket、预签名 URL 或后端图片代理。
- 不做历史本地头像文件迁移。
- 不改动前端头像展示 UI。
- 不删除 `.env.development`，因为它仍服务于非 Docker 本地开发。

## 配置设计

后端新增存储配置：

- `app.storage.type`：存储类型，当前支持 `minio`，保留未来扩展空间。
- `app.storage.minio.endpoint`：后端访问 MinIO 的内部地址，例如 `http://minio:9000`。
- `app.storage.minio.public-url`：前端访问 MinIO 的公开地址，例如 `http://localhost:9000`。
- `app.storage.minio.access-key`：MinIO Access Key。
- `app.storage.minio.secret-key`：MinIO Secret Key。
- `app.storage.minio.bucket`：bucket 名，例如 `interview-agent`。

配置来源：

- `application.yml` 放非敏感默认结构和占位符。
- `application-dev.yml` 提供本地开发默认值。
- `application-test.yml` 提供测试假值。
- `application-prod.yml` 强制从环境变量读取敏感项。
- `docker-compose.yml` 注入本地 Docker 所需变量。

## 后端设计

新增存储抽象：

- `StorageService`：定义头像上传接口，例如 `uploadAvatar(Long userId, MultipartFile file)`。
- `MinioStorageService`：实现 MinIO 上传、bucket 初始化、对象名生成、公开 URL 拼接。
- `StorageProperties`：绑定 `app.storage.*` 配置。

头像上传流程：

1. `UserController.uploadAvatarFile` 接收 multipart 文件和认证用户。
2. `UserServiceImpl.uploadAvatar` 校验用户存在。
3. `UserServiceImpl` 调用 `StorageService.uploadAvatar(userId, file)`。
4. `MinioStorageService` 校验文件类型和大小。
5. `MinioStorageService` 上传到对象路径 `avatar/{userId}/{uuid}.{ext}`。
6. 上传成功后返回 `${public-url}/${bucket}/avatar/{userId}/{uuid}.{ext}`。
7. `UserServiceImpl` 将 URL 写入 `user.avatar_url`。
8. 前端继续从接口响应中的 `avatarUrl` 更新本地用户信息。

错误处理：

- 空文件返回业务异常。
- 非图片类型返回业务异常。
- 文件过大返回业务异常。
- MinIO 上传失败返回业务异常并记录日志。
- bucket 不存在时自动创建 bucket。
- 公共读策略在开发环境自动设置；生产环境可由运维提前设置，代码尽量避免覆盖已有策略。

## 前端设计

保持当前头像上传入口：

- 仍使用 `/api/user/avatar/upload`。
- 仍读取响应中的 `data.avatarUrl`。
- 不改页面 UI。

环境变量：

- 保留 `project/.env.development`，支持本地直接运行 `npm run dev:h5`。
- `docker-compose.yml` 新增 `frontend` 服务，通过 `environment` 注入 `VITE_API_BASE_URL`。
- Compose 启动时，前端 API 地址使用浏览器可访问的后端地址，例如 `http://localhost:8442`。

## Docker Compose 设计

新增 `minio` 服务：

- 镜像：`minio/minio`
- API 端口：`9000`
- Console 端口：`9001`
- 数据卷：`minio-data`
- 默认开发账号通过环境变量提供。
- 启动命令：`server /data --console-address ":9001"`。

更新 `backend` 服务：

- 依赖 `minio`。
- 注入 MinIO endpoint、public URL、bucket、access key、secret key。
- 本地 Compose 下后端使用 `http://minio:9000` 访问 MinIO。

新增 `frontend` 服务：

- 使用 Node 镜像。
- 挂载 `./project:/app`。
- 启动 `npm install && npm run dev:h5 -- --host 0.0.0.0`。
- 注入 `VITE_API_BASE_URL=http://localhost:8442`。
- 暴露 `5173`。

## 测试设计

后端：

- 新增 `MinioStorageServiceTest`，使用 mock MinIO client 或可替换 client wrapper 验证：
  - 正常图片上传返回公开 URL。
  - 空文件被拒绝。
  - 非图片文件被拒绝。
  - 对象名包含 `avatar/{userId}/`。
- 更新 `UserServiceImpl` 测试或新增测试，验证上传成功后更新 `avatar_url`。

前端：

- 不强制新增 UI 测试。
- 如改动 API 解析逻辑，则补充工具级测试。

集成验证：

- `docker compose up` 后可访问：
  - 前端：`http://localhost:5173`
  - 后端：`http://localhost:8442`
  - MinIO Console：`http://localhost:9001`
- 登录后上传头像，数据库保存 MinIO 公开 URL，页面刷新后头像仍可访问。

## 文件影响

预计新增：

- `backend/src/main/java/com/multimodal/interview/config/StorageProperties.java`
- `backend/src/main/java/com/multimodal/interview/service/StorageService.java`
- `backend/src/main/java/com/multimodal/interview/service/impl/MinioStorageService.java`
- `backend/src/test/java/com/multimodal/interview/service/impl/MinioStorageServiceTest.java`

预计修改：

- `backend/pom.xml`
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-dev.yml`
- `backend/src/main/resources/application-test.yml`
- `backend/src/main/resources/application-prod.yml`
- `backend/src/main/java/com/multimodal/interview/service/impl/UserServiceImpl.java`
- `backend/src/main/java/com/multimodal/interview/controller/UserController.java`
- `docker-compose.yml`
- `docs/deployment.md`
- `README.md`

## 风险与缓解

- bucket 公共读会让头像 URL 可公开访问：头像属于公开展示资源，符合用户确认的方案 A。
- Docker Compose 前端变量只在 Compose 启动前端时生效：保留 `.env.development` 解决非 Docker 本地开发。
- MinIO 初始化和公共读策略可能因权限失败：失败时记录日志，并在文档中说明可在 Console 手动设置。
- 生产环境对象存储域名可能不是 MinIO 原始端口：通过 `public-url` 配置解决。
- 历史本地头像 URL 不迁移：旧 URL 仍依赖原本本地文件目录；后续如需要再单独做迁移脚本。

## 验收标准

- 后端头像上传不再写入本地 `app.file.root/avatar`。
- 上传头像后，接口返回 MinIO 公开 URL。
- 数据库 `avatar_url` 保存 MinIO 公开 URL。
- 前端个人中心上传头像后能立即显示新头像。
- `docker-compose.yml` 可以启动 MySQL、Redis、MinIO、后端和前端。
- 通过 Compose 启动前端时，`VITE_API_BASE_URL` 来自 Compose 环境变量。
- 直接本地运行前端时，仍可通过 `.env.development` 获取 API 地址。
- 后端测试、前端测试和前端构建通过。

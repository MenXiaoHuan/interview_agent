# Docker 镜像与 Compose 说明

本文说明本项目 Dockerfile、Dockerfile.dev、.dockerignore 与 Compose 的职责划分。

## 文件职责

- `backend/Dockerfile.dev`：后端本地开发镜像，默认执行 `./mvnw spring-boot:run`。
- `backend/Dockerfile`：后端生产镜像，多阶段构建 jar，最终只运行 `java -jar /app/app.jar`。
- `backend/.dockerignore`：过滤 `target/`、日志、IDE 配置、临时文件和本地环境变量。
- `project/Dockerfile.dev`：前端本地开发镜像，默认执行 `npm run dev:h5`。
- `project/Dockerfile`：前端生产镜像，多阶段构建 H5 静态资源，最终用 nginx 托管。
- `project/.dockerignore`：过滤 `node_modules/`、`dist/`、缓存、日志和本地环境变量。
- `docker-compose.yml`：默认本地开发编排，使用前后端 `Dockerfile.dev`。
- `docker-compose.prod.yml`：生产编排，使用前后端 `Dockerfile`。

## 环境变量

Compose 会自动读取项目根目录 `.env` 做变量插值，并通过 `env_file` 注入前后端容器。

关键变量：

- `PLATFORM_SERVER_HOST_PORT`：后端宿主机端口，默认当前为 `8442`。
- `PLATFORM_WEB_HOST_PORT`：前端宿主机端口，默认当前为 `5172`。
- `PLATFORM_SSL_ENABLED`：后端 HTTPS 开关，当前默认应为 `true`。
- `PLATFORM_WEB_API_PROXY_TARGET`：前端代理后端地址，Compose 内推荐为 `https://server:8442`。
- `PLATFORM_DB_URL`：Compose 内推荐使用 `jdbc:mysql://mysql:3306/interview_agent?...`。
- `PLATFORM_REDIS_HOST`：Compose 内推荐使用 `redis`。
- `PLATFORM_MINIO_INTERNAL_ENDPOINT`：Compose 内推荐使用 `http://minio:9000`。

## 本地开发启动

```bash
docker compose up --build
```

开发编排特点：

- 后端源码挂载到 `/app`，使用 Maven wrapper 启动。
- 前端源码挂载到 `/app`，使用 Vite/uni-app 开发服务启动。
- Maven 依赖缓存保存在 `backend-maven-repo` volume。
- 前端依赖保存在 `web-node-modules` volume。

访问地址：

- 前端：`https://localhost:${PLATFORM_WEB_HOST_PORT}`
- 后端：`https://localhost:${PLATFORM_SERVER_HOST_PORT}`
- MinIO Console：`http://localhost:${PLATFORM_MINIO_CONSOLE_HOST_PORT}`

## 生产镜像启动

```bash
docker compose -f docker-compose.prod.yml up --build -d
```

生产编排特点：

- 后端镜像在构建阶段执行 Maven package，运行阶段只保留 JRE 和 jar。
- 前端镜像在构建阶段执行 `npm run build:h5`，运行阶段使用 nginx 托管 H5 静态资源。
- 生产编排不挂载前后端源码。
- nginx 会把 `/api`、`/scenario` 代理到 `PLATFORM_WEB_API_PROXY_TARGET`，头像读取接口统一走 `/api/avatar`。

## 停止服务

```bash
docker compose down
```

生产编排停止：

```bash
docker compose -f docker-compose.prod.yml down
```

如需同时清理数据卷：

```bash
docker compose down -v
docker compose -f docker-compose.prod.yml down -v
```

## 常见问题

- 如果端口被占用，先停止占用端口的旧进程，或调整根目录 `.env` 中的端口变量。
- 如果前端请求后端失败，检查 `PLATFORM_WEB_API_PROXY_TARGET` 是否为容器内可访问地址。
- 如果后端无法连接 MySQL/Redis/MinIO，检查 `.env` 中服务地址是否使用 Compose 服务名：`mysql`、`redis`、`minio`。
- 如果 HTTPS 证书是本地自签名证书，浏览器首次访问可能需要手动信任。

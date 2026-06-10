# Interview Agent

一个面向求职训练场景的 AI 面试平台，覆盖岗位选择、题目作答、简历评测、场景化演练、综合测评报告与管理后台等完整业务闭环。项目采用前后端分离架构，前端基于 `uni-app + Vue 3`，后端基于 `Spring Boot 3`，并集成大模型、GraphQL、WebSocket、MySQL 与 Redis，用于构建可扩展的智能面试训练体验。

## 项目简介

`Interview Agent` 聚焦 AI 面试训练与能力评估场景，面向求职者、校园招聘训练以及职业教育等方向，提供从岗位选择、专项练习到综合评测与报告输出的一体化能力。仓库当前已包含用户端、管理端与智能能力接入的核心实现，适合用于面试训练平台建设、教学演示或相关产品原型开发。

## 核心亮点

- 多维训练闭环：覆盖岗位选择、AI 面试、专项问答、场景模拟、简历评测与综合报告。
- AI 驱动交互：接入大模型与语音相关能力，支持问答生成、过程分析和互动式面试体验。
- 双端业务体系：同时包含用户端训练流程与管理员后台管理能力。
- 混合接口形态：提供 REST API 与 GraphQL 两类接口，适配不同前端交互场景。
- 可扩展后端基础：集成 JWT 鉴权、限流、WebSocket、文件上传与技能配置机制。

## 适用场景

- 面向求职者的 AI 模拟面试训练平台
- 面向校园招聘或职业教育的练习与评估系统
- 面向企业内部培训的题库训练与能力测评平台

## 系统架构

```mermaid
flowchart LR
    A[uni-app Frontend] --> B[Spring Boot Backend]
    B --> C[MySQL]
    B --> D[Redis]
    B --> E[GraphQL API]
    B --> F[REST API]
    B --> G[WebSocket]
    B --> H[LLM / AI Services]
    B --> I[Speech / Audio Services]
```

## 功能概览

### 用户端

- 登录、注册、找回密码、个人信息维护
- 岗位选择与多岗位面试训练入口
- AI 面试、模拟面试、专项题目作答
- 简历评测、场景评测与综合测评流程
- 综合报告、历史记录、排行榜查看
- 聊天大厅、祝福墙等互动模块

### 管理端

- 用户列表与权限管理
- 岗位分类与岗位管理
- 专项题库与场景题管理
- 用户行为与训练记录管理

### 智能能力

- AI 问答与对话式面试流程
- 简历内容提取与评估
- 场景题生成、评分与报告输出
- 音频转写、语音合成及相关多模态能力接入

## 技术栈

### 前端

- `uni-app`
- `Vue 3`
- `Vite`
- `Pinia`
- `Element Plus`
- `Chart.js` / `ECharts`

### 后端

- `Spring Boot 3`
- `Spring Security`
- `Spring GraphQL`
- `MyBatis`
- `JWT`
- `Bucket4j`
- `WebSocket`
- `Spring AI Alibaba Agent Framework`

### 基础设施

- `MySQL`
- `Redis`
- `Maven`
- `HTTPS` 本地证书配置

## 项目结构

```text
interview_agent/
├── backend/                      # Spring Boot 后端服务
│   ├── src/main/java/com/multimodal/interview/
│   │   ├── controller/           # REST 控制器
│   │   ├── service/              # 业务服务
│   │   ├── mapper/               # MyBatis Mapper
│   │   ├── entity/               # 数据实体
│   │   ├── dto/                  # 请求/响应对象
│   │   ├── config/               # 安全、GraphQL、WebSocket 等配置
│   │   └── reactagent/           # Agent 能力与技能路由
│   └── src/main/resources/
│       ├── application.yml       # 本地配置
│       ├── application-prd.yml   # 生产配置模板
│       ├── graphql/              # GraphQL Schema
│       ├── skills/               # Agent 技能定义
│       └── sql/                  # 数据库初始化脚本
└── project/                      # uni-app 前端工程
    └── src/
        ├── pages/                # 用户端与管理端页面
        ├── components/           # 通用组件
        ├── stores/               # 状态管理
        ├── utils/                # API、GraphQL、请求封装
        └── static/               # 静态资源
```

## 快速开始

### 环境要求

- `JDK 17`
- `Node.js 18+`
- `MySQL`
- `Redis`
- `Maven 3.9+`

### 1. 克隆项目

```bash
git clone https://github.com/MenXiaoHuan/interview_agent.git
cd interview_agent
```

### 2. 初始化数据库

执行数据库脚本：

```text
backend/src/main/resources/sql/interview_agent.sql
```

并确保本地已创建对应数据库。

### 3. 启动后端服务

```bash
cd backend
./mvnw spring-boot:run
```

默认本地访问地址：

- 服务地址：`https://localhost:8442`
- GraphQL 入口：`https://localhost:8442/graphql`
- Swagger UI：`https://localhost:8442/swagger-ui/index.html`

### 4. 启动前端工程

```bash
cd project
npm install
npm run dev:h5
```

前端当前默认请求后端地址：

```text
https://localhost:8442
```

对应配置文件：

```text
project/src/utils/api-config.js
```

## 配置说明

后端默认配置文件位于：

```text
backend/src/main/resources/application.yml
```

当前项目依赖以下关键配置项：

- MySQL 数据源
- Redis 连接信息
- JWT 密钥与过期时间
- 本地 HTTPS 证书
- AI 模型服务配置
- 语音与音频能力服务配置
- 文件上传根目录

建议在实际部署或团队协作时：

- 将数据库、Redis、JWT、AI Key 等敏感配置迁移至环境变量或独立密钥管理系统
- 区分本地、测试、生产环境配置
- 对上传目录、证书与第三方接口超时进行独立运维配置

## 接口与能力概览

从当前仓库结构与模块划分来看，后端已覆盖以下主要能力域：

- 用户认证与账户管理
- 岗位与岗位分类管理
- AI 面试与会话管理
- 音频转写与语音合成
- 简历上传、解析与历史管理
- 场景评测、专项问答与综合评测历史
- 排行榜、反馈、聊天消息、祝福墙
- 管理员侧用户与记录管理

## 开发建议

- 前后端联调时，优先确认本地 HTTPS 证书是否已被浏览器信任
- 当前端请求失败时，先检查 `project/src/utils/api-config.js` 中的后端地址配置
- 当后端启动失败时，优先排查数据库、Redis 与第三方 AI 服务配置
- 若需初始化业务数据，建议先执行 SQL 脚本再启动服务

## Roadmap

- 补充环境变量化配置，减少明文敏感信息暴露
- 增加 Docker / Docker Compose 一键部署方案
- 完善自动化测试与 CI/CD 流程
- 补充 README 中的系统截图、接口示例与部署文档

## License

当前仓库尚未声明开源许可证。如计划公开发布，建议补充 `LICENSE` 文件并明确使用条款。

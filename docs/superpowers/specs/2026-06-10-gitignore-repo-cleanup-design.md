# 仓库清理与 .gitignore 对齐设计

## 目标

为 `interview_agent` 仓库补充根目录 `.gitignore`，让 GitHub 展示与当前项目结构对齐，屏蔽本地开发噪音文件，同时避免把当前未确认的业务源码改动一并提交。

## 范围

本次只处理“仓库卫生”相关内容：

- 新增根目录 `.gitignore`
- 忽略 Java / Spring Boot 构建产物
- 忽略 uni-app / Node.js 依赖与前端构建产物
- 忽略 macOS 与 IDE 本地文件
- 忽略日志、临时文件、常见本地证书与私钥文件
- 只提交上述清理相关改动并推送到 `origin/main`

本次明确不处理：

- 当前后端 Java 源码改动
- 当前 SQL、GraphQL、技能定义等业务内容改动
- 对已有 Git 历史进行重写
- 大范围删除已跟踪业务文件

## 背景判断

当前仓库没有根目录 `.gitignore`，同时工作区存在以下典型噪音来源：

- `project/node_modules/`
- `backend/target/`
- `.DS_Store`
- `.vscode/`
- 本地证书与密钥文件

这些内容会影响 GitHub 仓库整洁度，也容易在后续提交时误入版本控制。

## 方案选择

### 方案 A：只补 `.gitignore` 并提交清理相关内容

优点：

- 风险最低
- 能立即减少后续误提交
- 不干扰当前业务开发分支状态

缺点：

- 对已经被跟踪并进入历史的无关文件，只能后续按需再单独清理

### 方案 B：补 `.gitignore` 并顺手删除所有已跟踪无关文件

优点：

- GitHub 仓库会更干净

缺点：

- 风险较高，容易误删当前仍有用途的本地文件
- 需要逐项甄别已跟踪文件是否属于真实项目资产

## 采用方案

采用方案 A。

## `.gitignore` 设计原则

- 覆盖通用但不过度激进
- 优先忽略可重建文件与纯本地文件
- 不忽略源码、SQL、配置模板、静态素材等真实项目资产
- 同时兼顾 `backend/` 与 `project/` 两个子工程

## 预期提交内容

- 新增：`.gitignore`
- 如有必要，仅包含与 `.gitignore` 直接相关的清理提交

## 风险控制

- 提交前仅暂存 `.gitignore`
- 不使用全量 `git add .`
- 推送前再次检查 `git status` 与 `git diff --cached`

## 验证方式

- `git status --short` 中不再出现被 `.gitignore` 命中的新噪音文件
- 本次 commit 不包含业务源码改动
- 成功 push 到 `origin/main`

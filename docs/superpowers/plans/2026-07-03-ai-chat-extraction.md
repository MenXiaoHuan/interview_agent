# AI Chat Extraction Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract the assistant conversation from `interview-ai` into a dedicated `ai-chat` page, retarget the smart interview module to it, remove the legacy `interview-ai` page, and refresh job-selection copy to emphasize the AI interview within the broader training loop.

**Architecture:** Keep `interview-interface` as the stable module entry page, introduce a single-purpose `ai-chat` page for assistant conversation, and remove all route/build references to `interview-ai`. Use focused source-based tests to pin navigation strings, page registration, and job-selection copy before moving implementation.

**Tech Stack:** Vue 3, Uni-app, Pinia, Vitest/Jest-style source assertions, Vite H5 build

---

### Task 1: Lock Route and Copy Expectations with Failing Tests

**Files:**
- Modify: `project/src/pages/job-selection/job-selection-layout.test.js`
- Create: `project/src/pages/interview-interface/interview-interface-routing.test.js`
- Test: `project/src/pages/job-selection/job-selection-layout.test.js`
- Test: `project/src/pages/interview-interface/interview-interface-routing.test.js`

- [ ] **Step 1: Write the failing test for the new smart interview route**

```js
import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

describe("interview interface routing", () => {
  it("routes the smart interview module to the dedicated ai-chat page", () => {
    const filePath = resolve(__dirname, "index.vue");
    const source = readFileSync(filePath, "utf8");

    expect(source).toContain("goToInterviewAI");
    expect(source).toContain("/pages/ai-chat/index");
    expect(source).not.toContain("/pages/interview-ai/index?jobId=");
  });
});
```

- [ ] **Step 2: Write the failing test for the updated job-selection copy**

```js
import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

describe("job selection layout", () => {
  it("highlights the ai interview while preserving the training loop narrative", () => {
    const filePath = resolve(__dirname, "index.vue");
    const source = readFileSync(filePath, "utf8");

    expect(source).toContain("智能体面试主入口");
    expect(source).toContain("专项评测与结果复盘");
    expect(source).not.toContain("先锁定目标岗位，再开启后续 AI 面试、专项题、简历评测与综合报告沉淀");
  });
});
```

- [ ] **Step 3: Run the targeted tests to verify they fail**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/interview-interface/interview-interface-routing.test.js src/pages/job-selection/job-selection-layout.test.js`

Expected: FAIL because `/pages/ai-chat/index` and the new copy do not exist yet.

- [ ] **Step 4: Commit the failing tests**

```bash
git add project/src/pages/interview-interface/interview-interface-routing.test.js project/src/pages/job-selection/job-selection-layout.test.js
git commit -m "test: pin ai chat extraction behavior"
```

### Task 2: Create the Dedicated AI Chat Page and Retarget Navigation

**Files:**
- Create: `project/src/pages/ai-chat/index.vue`
- Modify: `project/src/pages/interview-interface/index.vue`
- Modify: `project/src/pages.json`
- Modify: `project/src/main.js`
- Test: `project/src/pages/interview-interface/interview-interface-routing.test.js`

- [ ] **Step 1: Write the failing registration assertion**

```js
import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

describe("page registration", () => {
  it("registers the ai-chat page and removes the legacy interview-ai page", () => {
    const pagesJson = readFileSync(resolve(__dirname, "../../pages.json"), "utf8");

    expect(pagesJson).toContain('"path": "pages/ai-chat/index"');
    expect(pagesJson).not.toContain('"path": "pages/interview-ai/index"');
  });
});
```

- [ ] **Step 2: Run the registration test to verify it fails**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/interview-interface/interview-interface-routing.test.js`

Expected: FAIL because `pages.json` still declares `interview-ai` and does not declare `ai-chat`.

- [ ] **Step 3: Implement the minimal route and page extraction**

```vue
// project/src/pages/interview-interface/index.vue
const goToInterviewAI = () => {
  const jobId = currentJobId.value;
  if (!jobId) {
    uni.showToast({ title: "请先选择岗位", icon: "none", duration: 2000 });
    router.replace("/pages/job-selection/index");
    return;
  }

  uni.setStorageSync("currentJobId", jobId);
  isTransitioning.value = true;
  setTimeout(() => {
    uni.navigateTo({
      url: `/pages/ai-chat/index?jobId=${jobId}`,
    });
  }, 180);
};
```

```js
// project/src/main.js
import(/* webpackPrefetch: true */ "@/pages/ai-chat/index.vue");
```

```json
// project/src/pages.json
{
  "path": "pages/ai-chat/index",
  "style": {
    "navigationStyle": "custom"
  }
}
```

```vue
<!-- project/src/pages/ai-chat/index.vue -->
<template>
  <div class="ai-chat-page">...</div>
</template>
```

- [ ] **Step 4: Run the targeted routing test to verify it passes**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/interview-interface/interview-interface-routing.test.js`

Expected: PASS

- [ ] **Step 5: Commit the route extraction step**

```bash
git add project/src/pages/interview-interface/index.vue project/src/pages/ai-chat/index.vue project/src/pages.json project/src/main.js project/src/pages/interview-interface/interview-interface-routing.test.js
git commit -m "feat: route smart interview to ai chat"
```

### Task 3: Remove Legacy Interview-AI Dependencies

**Files:**
- Modify: `project/src/pages/interview-resume/index.vue`
- Modify: `project/src/pages/interview-questions/index.vue`
- Modify: `project/src/pages/interview-scenario/index.vue`
- Modify: `project/src/pages/comprehensive-scenario/index.vue`
- Modify: `project/src/pages/comprehensive-report/index.vue`
- Delete: `project/src/pages/interview-ai/index.vue`
- Test: `project/src/pages/interview-interface/interview-interface-routing.test.js`

- [ ] **Step 1: Write the failing source assertion for legacy route cleanup**

```js
import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

describe("legacy interview-ai cleanup", () => {
  it("removes all interview-ai route references from active source files", () => {
    const files = [
      resolve(__dirname, "../interview-resume/index.vue"),
      resolve(__dirname, "../interview-questions/index.vue"),
      resolve(__dirname, "../interview-scenario/index.vue"),
      resolve(__dirname, "../comprehensive-scenario/index.vue"),
      resolve(__dirname, "../comprehensive-report/index.vue"),
    ];

    files.forEach((file) => {
      const source = readFileSync(file, "utf8");
      expect(source).not.toContain("/pages/interview-ai/index");
    });
  });
});
```

- [ ] **Step 2: Run the cleanup test to verify it fails**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/interview-interface/interview-interface-routing.test.js`

Expected: FAIL because the listed files still reference `/pages/interview-ai/index`.

- [ ] **Step 3: Replace legacy return/build helpers and delete the old page**

```js
// Example helper shape in affected pages
return `/pages/ai-chat/index${query.length ? `?${query.join("&")}` : ""}`;
```

```bash
rm project/src/pages/interview-ai/index.vue
```

- [ ] **Step 4: Run the cleanup test to verify it passes**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/interview-interface/interview-interface-routing.test.js`

Expected: PASS

- [ ] **Step 5: Commit the dependency cleanup**

```bash
git add project/src/pages/interview-resume/index.vue project/src/pages/interview-questions/index.vue project/src/pages/interview-scenario/index.vue project/src/pages/comprehensive-scenario/index.vue project/src/pages/comprehensive-report/index.vue project/src/pages/interview-interface/interview-interface-routing.test.js
git rm project/src/pages/interview-ai/index.vue
git commit -m "refactor: remove legacy interview ai route usage"
```

### Task 4: Refresh Job Selection Messaging

**Files:**
- Modify: `project/src/pages/job-selection/index.vue`
- Test: `project/src/pages/job-selection/job-selection-layout.test.js`

- [ ] **Step 1: Implement the minimal copy update**

```vue
<h1>岗位确认与训练入口</h1>
<p>
  先锁定目标岗位，再进入模拟面试模块入口。其中智能体面试是最核心的沉浸式主入口，
  后续可继续衔接专项评测与结果复盘，形成完整训练闭环。
</p>
```

```vue
<div class="job-selection-feature-item">
  <span class="job-selection-feature-dot"></span>
  <span>选定岗位后，优先进入智能体面试主入口</span>
</div>
<div class="job-selection-feature-item">
  <span class="job-selection-feature-dot"></span>
  <span>围绕岗位上下文展开专项评测与结果复盘</span>
</div>
```

- [ ] **Step 2: Run the layout test to verify it passes**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/job-selection/job-selection-layout.test.js`

Expected: PASS

- [ ] **Step 3: Commit the copy refresh**

```bash
git add project/src/pages/job-selection/index.vue project/src/pages/job-selection/job-selection-layout.test.js
git commit -m "feat: refresh job selection ai interview copy"
```

### Task 5: Full Verification

**Files:**
- Test: `project/src/pages/interview-interface/interview-interface-routing.test.js`
- Test: `project/src/pages/job-selection/job-selection-layout.test.js`

- [ ] **Step 1: Run the focused regression suite**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/interview-interface/interview-interface-routing.test.js src/pages/job-selection/job-selection-layout.test.js`

Expected: PASS

- [ ] **Step 2: Run the full frontend test suite**

Run: `cd /Users/bytedance/interview_agent/project && npm test`

Expected: PASS

- [ ] **Step 3: Run the H5 build**

Run: `cd /Users/bytedance/interview_agent/project && npm run build:h5`

Expected: PASS

- [ ] **Step 4: Commit the verified integration**

```bash
git add project/src
git commit -m "feat: extract dedicated ai chat page"
```

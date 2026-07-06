# Comprehensive AI Orchestration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the AI interview experience around a comprehensive-assessment-only flow where `ai-chat` orchestrates stage feedback, gating, and final summary, while stage modules remain execution pages and the full session is archived as one comprehensive history record.

**Architecture:** Keep `ai-chat` as the user-facing conversational shell, strip out legacy `interview-ai` dead branches, and drive all comprehensive flow through a single assessment-session model shared by frontend and backend. Stage pages become thin execution modules that submit results and return to `ai-chat`, while backend validations enforce the stage order, resume threshold, and one-vote rejection rules.

**Tech Stack:** Vue 3, Uni-app, Pinia, Vitest, Spring Boot / Java backend, existing comprehensive assessment utilities

---

## File Structure

**Primary frontend files**

- Modify: `project/src/pages/ai-chat/index.vue`
  - Remove legacy dead branches
  - Keep current shell
  - Add comprehensive-only AI orchestration logic and recommended actions
- Modify: `project/src/pages/interview-interface/index.vue`
  - Narrow the entry page toward comprehensive flow only
- Modify: `project/src/pages/comprehensive-resume/index.vue`
  - Thin execution page, return to `ai-chat`
- Modify: `project/src/pages/comprehensive-questions/index.vue`
  - Thin execution page, return to `ai-chat`
- Modify: `project/src/pages/comprehensive-scenario/index.vue`
  - Rework into round 1 / round 2 / round 3 modes
- Modify/Create: `project/src/utils/comprehensive-assessment.js`
  - Session model and stage gating helpers
- Create/Modify tests:
  - `project/src/pages/ai-chat/ai-chat-layout.test.js`
  - `project/src/pages/interview-interface/interview-interface-routing.test.js`
  - add new focused tests for comprehensive flow gating

**Likely backend files**

- Modify: comprehensive assessment controller/service/entity files under `backend/src/main/java/...`
- Add/update DTOs for stage result submission and final archive generation

---

### Task 1: Lock the New Comprehensive-Only AI Flow with Failing Frontend Tests

**Files:**
- Modify: `project/src/pages/ai-chat/ai-chat-layout.test.js`
- Create: `project/src/pages/ai-chat/comprehensive-ai-flow.test.js`
- Test: `project/src/pages/ai-chat/ai-chat-layout.test.js`
- Test: `project/src/pages/ai-chat/comprehensive-ai-flow.test.js`

- [ ] **Step 1: Write the failing test for comprehensive-only AI guidance**

```js
import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const source = readFileSync(path.join(currentDir, "index.vue"), "utf8");

describe("comprehensive ai flow", () => {
  it("removes special-assessment guidance from ai-chat", () => {
    expect(source).not.toContain("专项测评");
    expect(source).not.toContain("choose_special");
    expect(source).toContain("简历评测");
    expect(source).toContain("试题作答");
    expect(source).toContain("一面");
    expect(source).toContain("二面");
    expect(source).toContain("三面");
  });
});
```

- [ ] **Step 2: Write the failing test for AI-driven stage analysis**

```js
import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const source = readFileSync(path.join(currentDir, "index.vue"), "utf8");

describe("comprehensive ai stage analysis", () => {
  it("keeps stage feedback in ai-chat instead of standalone analysis copy", () => {
    expect(source).toContain("推荐操作");
    expect(source).toContain("最终总结");
    expect(source).toContain("归档");
  });
});
```

- [ ] **Step 3: Run the focused tests to verify they fail**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/ai-chat/ai-chat-layout.test.js src/pages/ai-chat/comprehensive-ai-flow.test.js`

Expected: FAIL because `ai-chat` still contains old dead branches and mixed comprehensive/special semantics.

- [ ] **Step 4: Commit the failing tests**

```bash
git add project/src/pages/ai-chat/ai-chat-layout.test.js project/src/pages/ai-chat/comprehensive-ai-flow.test.js
git commit -m "test: pin comprehensive ai orchestration behavior"
```

### Task 2: Remove Legacy Dead Branches from `ai-chat`

**Files:**
- Modify: `project/src/pages/ai-chat/index.vue`
- Test: `project/src/pages/ai-chat/ai-chat-layout.test.js`

- [ ] **Step 1: Write the failing cleanup assertions**

```js
import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const source = readFileSync(path.join(currentDir, "index.vue"), "utf8");

describe("legacy branch cleanup", () => {
  it("removes the dead non-dedicated ai page branches", () => {
    expect(source).not.toContain('v-if="!isDedicatedAiChatPage" class="cyber-background"');
    expect(source).not.toContain('v-if="!isDedicatedAiChatPage" class="main-content"');
    expect(source).not.toContain("featureMap =");
    expect(source).not.toContain("showFeatureModal");
  });
});
```

- [ ] **Step 2: Run the cleanup test to verify it fails**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/ai-chat/ai-chat-layout.test.js`

Expected: FAIL because the dead branches are still present.

- [ ] **Step 3: Implement the minimal cleanup**

```vue
<!-- remove the old !isDedicatedAiChatPage template branches entirely -->
```

```js
// remove featureMap / showFeatureModal / openFeature / closeFeature
```

```css
/* remove module-hologram, about-section, feature-modal, and other dead style blocks */
```

- [ ] **Step 4: Run the cleanup test to verify it passes**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/ai-chat/ai-chat-layout.test.js`

Expected: PASS

- [ ] **Step 5: Commit the cleanup**

```bash
git add project/src/pages/ai-chat/index.vue project/src/pages/ai-chat/ai-chat-layout.test.js
git commit -m "refactor: remove legacy ai chat dead branches"
```

### Task 3: Reframe `ai-chat` as Comprehensive-Only Orchestrator

**Files:**
- Modify: `project/src/pages/ai-chat/index.vue`
- Modify: `project/src/utils/comprehensive-assessment.js`
- Test: `project/src/pages/ai-chat/comprehensive-ai-flow.test.js`

- [ ] **Step 1: Write the failing stage-sequence assertion**

```js
import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const source = readFileSync(path.join(currentDir, "index.vue"), "utf8");

describe("comprehensive stage sequence", () => {
  it("documents the canonical stage order in the ai flow", () => {
    expect(source).toContain("resume");
    expect(source).toContain("questions");
    expect(source).toContain("round_1");
    expect(source).toContain("round_2");
    expect(source).toContain("round_3");
  });
});
```

- [ ] **Step 2: Run the focused test to verify it fails**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/ai-chat/comprehensive-ai-flow.test.js`

Expected: FAIL because the AI page still uses old stage/action semantics.

- [ ] **Step 3: Implement the comprehensive-only action model**

```js
const AVAILABLE_ASSISTANT_ACTION_TYPES = [
  "go_resume",
  "go_questions",
  "go_round_1",
  "go_round_2",
  "go_round_3",
  "restart_comprehensive",
  "view_comprehensive_history",
];
```

```js
const ASSISTANT_ACTION_LABELS = {
  go_resume: "开始简历评测",
  go_questions: "进入试题作答",
  go_round_1: "进入技术一面",
  go_round_2: "进入技术二面",
  go_round_3: "进入 HR 三面",
  restart_comprehensive: "重新开始综合测评",
  view_comprehensive_history: "查看综合测评历史",
};
```

- [ ] **Step 4: Run the focused AI flow tests to verify they pass**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/ai-chat/comprehensive-ai-flow.test.js`

Expected: PASS

- [ ] **Step 5: Commit the orchestration model**

```bash
git add project/src/pages/ai-chat/index.vue project/src/utils/comprehensive-assessment.js project/src/pages/ai-chat/comprehensive-ai-flow.test.js
git commit -m "feat: reframe ai chat as comprehensive orchestrator"
```

### Task 4: Enforce Resume Score Gating and Stage Order

**Files:**
- Modify: `project/src/utils/comprehensive-assessment.js`
- Modify: relevant backend comprehensive service files
- Create/Modify: frontend/backend tests for gating

- [ ] **Step 1: Write the failing frontend gating helper test**

```js
import { describe, expect, it } from "vitest";
import { canAdvanceFromResume } from "@/utils/comprehensive-assessment";

describe("resume gating", () => {
  it("requires a resume score of at least 70", () => {
    expect(canAdvanceFromResume({ score: 69 })).toBe(false);
    expect(canAdvanceFromResume({ score: 70 })).toBe(true);
  });
});
```

- [ ] **Step 2: Run the helper test to verify it fails**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/utils/__tests__/comprehensive-assessment.test.js`

Expected: FAIL because the helper does not yet exist or enforce the threshold.

- [ ] **Step 3: Implement the minimal gating helper**

```js
export const RESUME_PASS_SCORE = 70;

export const canAdvanceFromResume = ({ score = 0 } = {}) => {
  return Number(score) >= RESUME_PASS_SCORE;
};
```

- [ ] **Step 4: Implement backend enforcement**

```java
if (currentStage == Stage.RESUME && resumeScore < 70) {
    session.fail("resume_below_threshold");
    return session;
}
```

```java
if (nextStage == Stage.QUESTIONS && !session.resumePassed()) {
    throw new IllegalStateException("Resume gate not satisfied");
}
```

- [ ] **Step 5: Run frontend helper tests and backend tests**

Run frontend: `cd /Users/bytedance/interview_agent/project && npm test -- src/utils/__tests__/comprehensive-assessment.test.js`

Expected: PASS

Run backend: `cd /Users/bytedance/interview_agent/backend && ./mvnw test -Dtest=*Comprehensive*`

Expected: PASS

- [ ] **Step 6: Commit the gating logic**

```bash
git add project/src/utils/comprehensive-assessment.js project/src/utils/__tests__/comprehensive-assessment.test.js backend/src/main/java backend/src/test/java
git commit -m "feat: enforce comprehensive resume threshold"
```

### Task 5: Rework Questions and Scenario Pages into Thin Execution Modules

**Files:**
- Modify: `project/src/pages/comprehensive-questions/index.vue`
- Modify: `project/src/pages/comprehensive-scenario/index.vue`
- Test: targeted page/source tests

- [ ] **Step 1: Write the failing source assertions**

```js
import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

describe("comprehensive execution pages", () => {
  const questionsSource = readFileSync(resolve(__dirname, "../comprehensive-questions/index.vue"), "utf8");
  const scenarioSource = readFileSync(resolve(__dirname, "../comprehensive-scenario/index.vue"), "utf8");

  it("keeps questions as an execution page and returns to ai-chat", () => {
    expect(questionsSource).toContain("/pages/ai-chat/index");
  });

  it("models scenario as round_1 / round_2 / round_3", () => {
    expect(scenarioSource).toContain("round_1");
    expect(scenarioSource).toContain("round_2");
    expect(scenarioSource).toContain("round_3");
  });
});
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/ai-chat/comprehensive-ai-flow.test.js`

Expected: FAIL because the scenario page does not yet expose the three-round mode model.

- [ ] **Step 3: Implement the minimal page behavior**

```js
// comprehensive-questions/index.vue
const returnToAiChat = () => {
  uni.redirectTo({
    url: `/pages/ai-chat/index?jobId=${currentJobId.value}&assessmentId=${assessmentId.value}&completedType=questions_completed&mode=COMPREHENSIVE&score=${score.value}&autoOpen=1`,
  });
};
```

```js
// comprehensive-scenario/index.vue
const currentRound = computed(() => String(route.query.round || "round_1"));
const ROUND_PROMPT_MAP = {
  round_1: "technical_initial",
  round_2: "technical_deep",
  round_3: "hr_final",
};
```

- [ ] **Step 4: Run the focused tests to verify they pass**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/ai-chat/comprehensive-ai-flow.test.js`

Expected: PASS

- [ ] **Step 5: Commit the stage page refactor**

```bash
git add project/src/pages/comprehensive-questions/index.vue project/src/pages/comprehensive-scenario/index.vue project/src/pages/ai-chat/comprehensive-ai-flow.test.js
git commit -m "feat: align comprehensive stage pages with ai orchestration"
```

### Task 6: Archive the Whole Comprehensive Session as One History Record

**Files:**
- Modify: frontend history integration files
- Modify: backend persistence / archive generation logic
- Test: history assertions

- [ ] **Step 1: Write the failing archive test**

```js
import { describe, expect, it } from "vitest";
import { buildComprehensiveHistoryRecord } from "@/utils/comprehensive-assessment";

describe("comprehensive history archive", () => {
  it("stores the whole flow as one history item", () => {
    const record = buildComprehensiveHistoryRecord({
      jobId: 1,
      status: "PASSED",
      stageResults: {
        resume: { score: 78, passed: true },
        questions: { score: 82, passed: true },
        round_1: { score: 80, passed: true },
        round_2: { score: 76, passed: true },
        round_3: { score: 74, passed: true },
      },
      finalSummary: "通过",
    });

    expect(record.stageResults.round_3.score).toBe(74);
    expect(record.finalSummary).toBe("通过");
    expect(record.status).toBe("PASSED");
  });
});
```

- [ ] **Step 2: Run the archive test to verify it fails**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/utils/__tests__/comprehensive-assessment.test.js`

Expected: FAIL because the archive helper does not yet model the whole flow.

- [ ] **Step 3: Implement the minimal archive builder**

```js
export const buildComprehensiveHistoryRecord = ({
  jobId,
  status,
  stageResults,
  finalSummary,
  finalSuggestion = "",
}) => ({
  type: "comprehensive_flow",
  jobId,
  status,
  stageResults,
  finalSummary,
  finalSuggestion,
  archivedAt: Date.now(),
});
```

- [ ] **Step 4: Persist the final record on backend completion/failure**

```java
historyService.archiveComprehensiveFlow(session);
```

- [ ] **Step 5: Run frontend and backend archive tests**

Run frontend: `cd /Users/bytedance/interview_agent/project && npm test -- src/utils/__tests__/comprehensive-assessment.test.js`

Expected: PASS

Run backend: `cd /Users/bytedance/interview_agent/backend && ./mvnw test -Dtest=*History*`

Expected: PASS

- [ ] **Step 6: Commit the history redesign**

```bash
git add project/src/utils/comprehensive-assessment.js project/src/utils/__tests__/comprehensive-assessment.test.js backend/src/main/java backend/src/test/java
git commit -m "feat: archive comprehensive assessment as unified history"
```

### Task 7: Full Verification

**Files:**
- Test: `project/src/pages/ai-chat/*.test.js`
- Test: `project/src/utils/__tests__/comprehensive-assessment.test.js`

- [ ] **Step 1: Run focused frontend regression tests**

Run: `cd /Users/bytedance/interview_agent/project && npm test -- src/pages/ai-chat/ai-chat-layout.test.js src/pages/ai-chat/comprehensive-ai-flow.test.js src/utils/__tests__/comprehensive-assessment.test.js src/pages/interview-interface/interview-interface-routing.test.js`

Expected: PASS

- [ ] **Step 2: Run the full frontend test suite**

Run: `cd /Users/bytedance/interview_agent/project && npm test`

Expected: PASS

- [ ] **Step 3: Run the H5 build**

Run: `cd /Users/bytedance/interview_agent/project && npm run build:h5`

Expected: PASS

- [ ] **Step 4: Run targeted backend tests**

Run: `cd /Users/bytedance/interview_agent/backend && ./mvnw test -Dtest=*Comprehensive*,*History*`

Expected: PASS

- [ ] **Step 5: Commit the verified integration**

```bash
git add project/src backend/src
git commit -m "feat: orchestrate comprehensive assessment through ai chat"
```

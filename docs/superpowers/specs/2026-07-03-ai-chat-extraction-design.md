# AI Chat Extraction Design

## Background

The current interview flow has two separate levels after job selection:

1. `job-selection/index.vue` sends the user to `interview-interface/index.vue`.
2. The "智能体面试" card inside `interview-interface/index.vue` sends the user to `interview-ai/index.vue`.

`interview-ai/index.vue` has grown into a mixed-responsibility page. It contains:

- the actual assistant chat experience,
- a module hub for resume/questions/scenario/report navigation,
- back-navigation and URL builders that assume `interview-ai` is a central hub.

This no longer matches the desired product shape. The user expectation is:

- "开始面试" still enters the module entry page (`interview-interface/index.vue`),
- clicking only the "智能体面试" module should enter a dedicated AI chat page,
- the old `interview-ai` page should be removed entirely,
- the left-side copy on `job-selection/index.vue` should explain the training loop while strongly prioritizing the AI interview as the main immersive experience.

## Goals

- Introduce a dedicated `ai-chat` page extracted from the existing chat portion of `interview-ai`.
- Keep `interview-interface` as the stable module-entry page.
- Retarget the "智能体面试" module to the new `ai-chat` page.
- Remove all remaining route references and preload references to `interview-ai`.
- Delete `src/pages/interview-ai/index.vue`.
- Update `job-selection` left-side copy to describe both the training loop and the AI interview priority.

## Non-Goals

- Redesigning the overall `interview-interface` layout.
- Reworking the specialized module pages (`interview-resume`, `interview-questions`, `interview-scenario`, `comprehensive-*`) beyond route/back-link cleanup required by this change.
- Refactoring the assistant API contract.

## Current Constraints

- Existing page structure is Uni-app/Vue-based and routes are declared in `src/pages.json`.
- `interview-ai/index.vue` is a very large file, so extraction must be conservative and responsibility-driven instead of broad refactoring.
- Existing pages already depend on `jobId`, event-channel state, and route query parameters. The extracted page must preserve the job context and return flow.
- The desktop auth/training-entry pages in this project have strict layout expectations; copy updates must preserve one-screen layout and existing left/right composition.

## Proposed Architecture

### 1. Dedicated AI Chat Page

Create `src/pages/ai-chat/index.vue` as a focused page that owns only the assistant interview conversation.

It will keep:

- session list and active-session switching,
- chat welcome state,
- message rendering and streaming behavior,
- input area and assistant quick actions,
- persistence and history restoration logic that is strictly required for chat continuity,
- `jobId` handling and any assistant-side conversation metadata needed for job-specific context,
- return navigation back to `interview-interface/index.vue` with `jobId`.

It will remove:

- the module hologram hub,
- `handleModuleClick`-based module dispatching,
- UI sections that market or summarize other modules,
- URL builders and back-links that assume `interview-ai` remains as a reusable hub route.

### 2. Stable Module Entry Page

`src/pages/interview-interface/index.vue` remains the page reached from job selection.

Only one behavior changes:

- `goToInterviewAI()` will navigate to `/pages/ai-chat/index?jobId=...` instead of `/pages/interview-ai/index?...`.

Other module cards keep their current destinations and status handling.

### 3. Route and Back-Link Cleanup

Every route helper or return link that still targets `/pages/interview-ai/index` must be reviewed and updated.

The expected cleanup categories are:

- direct navigation from `interview-interface`,
- helper functions on module pages that currently build `interview-ai` return URLs,
- route declarations in `src/pages.json`,
- preload/import declarations in `src/main.js`.

Where a page used `interview-ai` as the return destination for "back to AI interview", it should now return to `/pages/ai-chat/index` when the user came from the assistant conversation; otherwise it should continue to use `interview-interface` when that is the correct parent page.

### 4. Job Selection Copy Refresh

`src/pages/job-selection/index.vue` left-side showcase copy should be updated to:

- explain that users first lock in a target role,
- make clear that the AI interview is the primary immersive entry,
- still describe downstream resume evaluation, targeted assessment, and result review as a training loop rather than isolated modules.

The copy should strengthen the AI-interview identity without implying that "开始面试" skips `interview-interface`.

## Data and Interaction Design

### Input Context

The new `ai-chat` page receives:

- `jobId` from route query,
- any existing session identifiers that are still necessary for restoring the active conversation,
- persisted assistant conversation state already used by the old chat section.

### Navigation Rules

- `job-selection -> interview-interface` stays unchanged.
- `interview-interface -> ai-chat` becomes the new assistant route.
- `ai-chat -> interview-interface` becomes the default back path.
- Any special module page that previously returned to `interview-ai` for assistant-led continuation should return to `ai-chat` instead.

### Compatibility Handling

Because `interview-ai/index.vue` currently mixes hub behavior and chat behavior, extraction should preserve chat-specific state names where possible to reduce regression risk, but route names and page-level wrappers must be updated to match the new single-purpose page.

## Risks

### Risk 1: Hidden route dependencies

Several module pages currently build URLs pointing at `interview-ai`. Missing one would create a dead route after deletion.

Mitigation:

- search for every `/pages/interview-ai/index` reference before deletion,
- update all return-url builders in the same change set,
- add tests that assert route registration cleanup.

### Risk 2: Over-extracting large-file logic

Copying the entire old file would recreate the mixed-responsibility problem under a new name.

Mitigation:

- extract only the chat UI and chat state management,
- remove module hub logic instead of preserving it behind dead code,
- keep file-local names where they are required for functionality, not because they existed before.

### Risk 3: Broken back navigation

The old page explicitly re-launched back to `interview-interface`. That contract must survive the move.

Mitigation:

- preserve `jobId` storage and fallback logic,
- verify back navigation from the new page returns to `interview-interface`.

### Risk 4: Copy update causing layout drift

The job-selection page has tight spacing constraints.

Mitigation:

- keep the same left-side structure and only replace copy content,
- update or add layout assertions without widening the page scope.

## Testing Strategy

### Static/structure tests

- Update or add tests that assert `interview-interface` no longer navigates to `interview-ai`.
- Add or update tests that assert `pages.json` registers `ai-chat` and no longer registers `interview-ai`.
- Add or update tests that assert the job-selection showcase copy now references the AI interview-first training flow.

### Targeted regression verification

- Run the affected page tests first to drive the change.
- Run the broader frontend test suite after implementation.
- Run a production build for H5 to catch route/import issues introduced by page deletion.

## File Impact Summary

- Create: `project/src/pages/ai-chat/index.vue`
- Modify: `project/src/pages/interview-interface/index.vue`
- Modify: `project/src/pages/job-selection/index.vue`
- Modify: `project/src/pages.json`
- Modify: `project/src/main.js`
- Modify: any page still building `/pages/interview-ai/index` return URLs
- Delete: `project/src/pages/interview-ai/index.vue`

## Decision Summary

The system will keep `interview-interface` as the post-job-selection module entry page, but the "智能体面试" module will lead directly to a new dedicated `ai-chat` page extracted from the current assistant chat implementation. The old `interview-ai` hub page will be fully removed, and the job-selection copy will be updated to explain the broader training loop while making the AI interview the clearest primary experience.

# Comprehensive AI Orchestration Design

## Background

The current interview experience still carries legacy assumptions from the old `interview-ai` hub:

- the AI page mixes UI experimentation, stage orchestration, and residual module-hub logic,
- comprehensive assessment and special assessment concepts are both exposed,
- stage feedback is not consistently centralized,
- the overall flow does not yet reflect a realistic gated interview process.

The desired direction is now clear:

1. `ai-chat` remains the primary AI interface and should keep its current overall layout direction.
2. The AI module should focus only on **comprehensive assessment**.
3. Stage-level analysis should happen in the AI conversation itself instead of separate standalone analysis pages.
4. The comprehensive flow becomes:

   `resume -> questions -> technical round 1 -> technical round 2 -> HR round 3`

5. Resume score must be at least `70` to continue.
6. At the end of the entire process, the AI gives a final summary whether the user passed or failed.
7. The complete comprehensive-assessment history is archived as one coherent flow record.

## Goals

- Reframe `ai-chat` as the comprehensive-assessment orchestration center.
- Remove special-assessment guidance and actions from the AI assistant flow.
- Ensure every stage returns to `ai-chat`, where AI provides the score, analysis, and next-step action.
- Enforce a backend-validated resume threshold of `70` before subsequent stages unlock.
- Rework scenario assessment into three interview rounds: technical round 1, technical round 2, HR round 3.
- Preserve stage modules as execution pages, but move interpretation and flow control to the AI conversation.
- Archive the whole comprehensive session as one history item with stage results and final AI summary.

## Non-Goals

- Rebuilding the entire site navigation.
- Reworking unrelated modules such as chat hall, leaderboard, or auth pages.
- Introducing a separate historical-summary agent in this phase.
- Keeping the current “special assessment” AI path alive inside `ai-chat`.

## Product Direction

### 1. AI Chat Remains the Primary Surface

`src/pages/ai-chat/index.vue` remains the user-facing AI surface. The overall shell, side panel, conversation layout, and input-centered experience stay intact. The page should not become a heavy dashboard-style orchestration console.

Instead, the AI conversation gains one focused capability:

- contextual recommended actions for the current comprehensive-assessment stage,
- stage score explanations,
- failure/pass guidance,
- final summary and archive confirmation.

### 2. AI Focuses Only on Comprehensive Assessment

Inside `ai-chat`, remove special-assessment language, branching, button labels, and action semantics. The AI should frame the experience as one guided comprehensive assessment flow only.

The user should no longer be asked to choose between special and comprehensive modes in the AI assistant flow.

### 3. Stage Pages Remain, Stage Analysis Pages Do Not

Each assessment stage still needs its own execution page:

- resume assessment,
- questions answering,
- interview round 1,
- interview round 2,
- interview round 3.

However, those pages should focus on:

- collecting inputs,
- executing the stage,
- submitting results,
- returning control to `ai-chat`.

They should no longer present standalone “analysis result pages” as the main explanatory layer. Once a stage is complete, users return to `ai-chat`, where AI provides:

- the stage score,
- the interpretation,
- the recommendation,
- the next action.

### 4. Comprehensive Report Becomes Background Output

The comprehensive report no longer needs to be a prominent front-stage module in this flow.

Instead:

- the backend should still generate and persist the full comprehensive result,
- `ai-chat` communicates that the report/history has been archived,
- history becomes the primary retrieval surface for completed comprehensive sessions.

## Flow Design

### Canonical Flow

The comprehensive-assessment flow is:

1. Resume assessment
2. Questions answering
3. Technical interview round 1
4. Technical interview round 2
5. HR interview round 3
6. Final AI summary
7. Archive whole session to history

### Resume Threshold Rule

Resume assessment is the admission gate.

- If score `< 70`:
  - the comprehensive session fails immediately,
  - no later stage is unlocked,
  - user returns to `ai-chat`,
  - AI provides failure analysis and resume-improvement suggestions,
  - AI offers restart / retry actions.

- If score `>= 70`:
  - the session continues,
  - questions answering is unlocked,
  - user returns to `ai-chat`,
  - AI explains the pass result and offers the next-step action.

### Questions Stage

Questions answering should feel closer to a real technical screening than a generic quiz.

It should be reworked around realistic interview-style question groups such as:

- foundations,
- project depth,
- scenario reasoning,
- open-ended explanation.

When complete:

- results are submitted,
- page returns to `ai-chat`,
- AI explains performance and unlocks round 1 if allowed.

### Scenario Assessment Reworked into Three Rounds

The current generic scenario assessment should be reworked into three interview rounds:

1. **Round 1: Technical initial interview**
   - foundational competence
   - project understanding
   - role fit

2. **Round 2: Technical deep interview**
   - depth
   - design tradeoffs
   - hard follow-up questions
   - pressure handling in technical reasoning

3. **Round 3: HR interview**
   - communication
   - motivation
   - stability
   - teamwork
   - behavior and values

Each round has:

- its own stage score,
- its own passing threshold,
- its own AI prompt framing,
- the possibility of one-vote rejection.

### One-Vote Rejection

Each interview round can end the overall flow if a critical fail condition is hit.

This means:

- a user may still have decent prior scores,
- but a critical failure in round 1, 2, or 3 can cause the entire comprehensive session to fail,
- AI then returns the user to `ai-chat` with explicit failure reasoning and overall next-step guidance.

## State Model

The flow should be backed by a single comprehensive-assessment session object.

Suggested core fields:

- `assessmentId`
- `userId`
- `jobId`
- `status`
  - `IN_PROGRESS`
  - `PASSED`
  - `FAILED`
- `currentStage`
  - `resume`
  - `questions`
  - `round_1`
  - `round_2`
  - `round_3`
  - `completed`
- `stageResults`
  - score
  - pass/fail
  - rejection flag
  - summary
  - completedAt
- `finalSummary`
- `finalSuggestion`
- `eliminationReason`
- `archivedHistoryId`

This session object becomes the source of truth for:

- unlock rules,
- redirect logic,
- AI context,
- archive generation.

## Frontend Responsibilities

### `ai-chat`

`ai-chat` is responsible for:

- showing the current comprehensive stage,
- receiving users back from stage pages,
- presenting AI analysis for each completed stage,
- showing recommended action buttons,
- showing final summary,
- confirming archive completion.

### Stage Pages

Each stage page is responsible for:

- rendering the task UI,
- collecting answers or uploads,
- submitting results,
- returning to `ai-chat`.

Stage pages should not remain the primary place for interpretive analysis.

### Entry Page

`src/pages/interview-interface/index.vue` should be simplified toward a comprehensive-assessment entry, with the AI interview path leading into this consolidated comprehensive flow.

## Backend Responsibilities

Backend validation must enforce the flow regardless of frontend state.

Required validations include:

- reject entry to questions if resume score is below 70,
- reject round 1 unless questions stage is complete,
- reject round 2 unless round 1 passed,
- reject round 3 unless round 2 passed,
- reject any later writes once the session is already failed or passed,
- enforce one-vote rejection semantics on round results.

The backend should also generate the final archived comprehensive result once the session completes or fails definitively.

## History Design

History should move away from separate fragmented module records and toward one integrated comprehensive-assessment record.

Each archived history item should include:

- job information,
- start and end time,
- final pass/fail status,
- stage-by-stage scores and outcomes,
- elimination point if any,
- AI final summary,
- AI improvement suggestions.

This gives the user one coherent retrospective object instead of multiple disconnected module snapshots.

## Prompt Strategy

Different interview rounds should use different prompt frames:

- round 1 prompt: baseline technical fit and project familiarity,
- round 2 prompt: depth, architecture, tradeoff reasoning, stress handling,
- round 3 prompt: HR behavior, communication, motivation, stability, collaboration.

The AI in `ai-chat` should not impersonate these rounds directly during execution. Instead, it should interpret stage results after the fact and guide the next move.

## Recommendation on Historical Summary Agent

Do **not** add a separate historical-summary agent in phase one.

Reasoning:

- the flow redesign itself is already large,
- the session model, gating rules, stage rewrites, and history redesign are enough complexity,
- if the system stores comprehensive history in a clean structure first, a summary agent can be added later without reworking core flow semantics.

Phase one should store history well; phase two can decide whether to inject historical summaries into AI orchestration.

## File Impact Direction

Expected primary files to change include:

- `project/src/pages/ai-chat/index.vue`
- `project/src/pages/interview-interface/index.vue`
- `project/src/pages/comprehensive-resume/index.vue`
- `project/src/pages/comprehensive-questions/index.vue`
- `project/src/pages/comprehensive-scenario/index.vue`
- `project/src/utils/comprehensive-assessment.*`
- backend comprehensive-assessment services / entities / controllers

Likely cleanup targets inside `ai-chat` include the old dead `interview-ai` remnants and obsolete UI branches already identified.

## Decision Summary

The project should pivot to a comprehensive-assessment-only AI flow. `ai-chat` remains the main conversational UI and becomes the orchestration layer for stage guidance, score interpretation, and final conclusions. Stage modules still exist as execution pages, but all analysis and flow decisions return to the AI conversation. Resume assessment blocks progression below 70, scenario assessment becomes three realistic interview rounds with distinct prompt strategies and rejection thresholds, and the entire process is archived as one comprehensive history record.

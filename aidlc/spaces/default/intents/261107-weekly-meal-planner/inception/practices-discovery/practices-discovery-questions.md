# Practices Discovery Questions

**Project:** Weekly Meal Planner (Android MVP, greenfield)  
**Purpose:** Confirm how the team wants to work for this project. Suggested answers are based on framework defaults and the MVP context; they are not established team facts.

Please answer each question by filling in the `[Answer]:` tag.

---

## 1. Way of Working

**Question:** How do you want to manage day-to-day changes for this project (where you commit, when to use separate branches, and how changes get merged)?

**Suggested default (adapted):** Keep work on a simple main line. Make commits on the branch you are already using unless you choose to isolate a change. Use short-lived branches for focused work when helpful, and merge changes after checks pass.

A. Use the suggested default (simple main line, short-lived branches when helpful, merge after checks pass).
B. Work mostly on one main branch with direct commits; use separate branches only occasionally.
C. Use feature branches for most changes, with a quick review/check before merging to the main branch.
X. Other (please specify)

[Answer]: A — Use the suggested default (simple main line, short-lived branches when helpful, merge after checks pass).

---

## 2. Build a thin end-to-end slice first?

**Question:** Build a thin end-to-end slice first? A walking skeleton is a minimal version that runs the whole way through, built first to prove the pieces connect before the real features go in. Do you want to start with that kind of slice for this meal-planner MVP?

**Suggested default (to discuss):** Consider a thin slice that proves the Android app can create/show a weekly plan and exercise one integration boundary or its fallback (for example, recipe data source, store selection, or shopping-list export), because third-party access is uncertain.

A. Yes — build a thin end-to-end slice first to prove the main flow and integration/fallback boundaries connect.
B. No — skip a thin end-to-end slice first and build feature by feature within the MVP scope.
C. Decide later, after requirements/user stories clarify the smallest useful slice.
X. Other (please specify)

[Answer]: C — Decide later, after requirements/user stories clarify the smallest useful slice.

---

## 3. Testing Posture

**Question:** How should we approach testing for this MVP (when tests are written relative to implementation, and what level of test coverage/checking do you expect before merging)?

**Suggested default (MVP):** `test-after` methodology. Implement each applicable testable layer, then write and run that layer’s tests. Target an 80% line-coverage floor, run tests in CI before merge, and keep existing tests green. Focus on core planning/shopping-list logic, integration adapters/fallbacks, storage, and critical Android user flows.

A. Use the suggested MVP default (`test-after`, per-layer tests after implementation, 80% coverage floor, CI before merge, existing tests green).
B. Use the suggested approach but relax the 80% coverage floor for this learning/ASAP MVP (keep critical tests and CI, set a lighter or no numeric floor).
C. Write tests before implementation for key behavior (TDD/BDD style where practical), then implement to satisfy them.
X. Other (please specify)

[Answer]: C — Write tests before implementation for key behavior (TDD/BDD style where practical), then implement to satisfy them.

**Optional details (if useful):** Preferred Android test tools/frameworks, CI provider, and any areas that must always have tests:

[Answer]: 

---

## 4. Deployment / Release

**Question:** Since this is an Android MVP, how do you want to produce and share builds during development and for early release?

**Suggested default (adapted):** Produce verified builds from the main line, run checks before sharing, and use internal testing distribution before any wider release. If a small backend/service is added later, define separate checks and require explicit approval before any production-facing release.

A. Use the suggested default (verified builds from main line, checks before sharing, internal testing before wider release).
B. Local/device/emulator builds only for now; formal internal distribution can wait until the MVP is more complete.
C. Set up internal testing distribution early (for example, a testing track) and treat each shared build as a checked release candidate.
X. Other (please specify)

[Answer]: A — Use the suggested default (verified builds from main line, checks before sharing, internal testing before wider release).

---

## 5. Code Style

**Question:** What coding conventions and automated checks should we follow for the Android/Kotlin codebase?

**Suggested default:** Follow idiomatic Android/Kotlin conventions and defer to project tooling. Use consistent formatting and lint/static analysis in CI. Keep integration boundaries explicit, avoid hardcoded secrets or personal data, and document fallback behavior for Sydänmerkki/PRISMA/S-ostoslista integrations.

A. Use the suggested default (idiomatic Kotlin/Android style, formatter + lint/static analysis in CI, explicit integration/privacy handling).
B. Use Android Studio/IDE defaults initially and add stricter formatter/lint/static-analysis rules once the project structure stabilizes.
C. Adopt specific tools now (please name them in “Other”, e.g., ktlint, detekt, Compose lint rules, or other checks).
X. Other (please specify)

[Answer]: A — Use the suggested default (idiomatic Kotlin/Android style, formatter + lint/static analysis in CI, explicit integration/privacy handling).

---

## Consolidated Summary Confirmation

- Looks correct
- Request changes

[Answer]: Looks correct

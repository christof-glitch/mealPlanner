# NFR Requirements Questions — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Context
U3 completes C4/C5/C6 domain behavior (locked-snapshot generation/re-resolve/merge/edit/regeneration confirmation, PRISMA store/presence best effort name/presence only, S-ostoslista attempt + mandatory share/copy fallback, stable grocery/export extraction seam). Stack and privacy/testing posture inherit U1/U2 decisions. OQ2–OQ4 remain investigational; contracts must degrade safely.

## Questions

**Q1. PRISMA/S-ostoslista adapter execution model (OQ2–OQ4)**
How should U3 NFRs treat live PRISMA/S-ostoslista behavior in MVP?
- A) (Recommended) Treat live PRISMA/S-ostoslista calls as investigational adapter boundaries: bounded/timeout-capable, non-blocking to unrelated local list/edit/export-fallback flows, returning typed `UNKNOWN|UNAVAILABLE|DENIED|PARTIAL|FAILED_MAPPED|NOT_SUPPORTED` outcomes; manual store/checklist and share/copy fallback mandatory.
- B) Require live PRISMA stock/guaranteed availability and official S-ostoslista export in MVP.
- C) Remove PRISMA/S-ostoslista from MVP.

[Answer]: A

**Q2. Shopping-list scale/performance baseline (NFR5)**
What MVP baseline should U3 target for on-device list operations?
- A) (Recommended) Responsive local operations for typical MVP scale (selected week, roughly tens to low hundreds of ingredient lines/items): deterministic merge/generation/applyPresenceCheckBatch complete without blocking unrelated UI interactions; no hard ms SLO in this artifact, defer precise thresholds to NFR Design/testing if needed.
- B) Define strict millisecond SLOs now without measurement data.
- C) Optimize for thousands of items with background indexing.

[Answer]: A

**Q3. Observability/diagnostics (NFR2, NFR3, NFR5)**
What diagnostics should U3 expose?
- A) (Recommended) Typed status/reason fields in domain results (`GenerationResult`, `PresenceCheckBatchResult`, `ExportResult`, stale/availability reasons) suitable for U4 presentation and tests; no external telemetry required; avoid logging PII/location coordinates/secrets.
- B) Add analytics/crash telemetry with item/location payloads.
- C) No status reasons.

[Answer]: A

**Q4. Security/privacy boundaries for U3 (NFR3)**
What privacy constraints apply?
- A) (Recommended) Inherit U1/NFR3: no MealPlanner account, minimal local data, no secrets in source, location optional/purpose-limited handled outside U3 domain core with typed denied/unavailable inputs; presence checks are name/presence only (never stock/guaranteed availability); diagnostics avoid sensitive personal payloads.
- B) Store raw location coordinates in shopping-list domain state.
- C) Require account/auth.

[Answer]: A

**Q5. Tech stack alignment (inception NFR1/NFR4 context)**
What stack decisions apply to U3?
- A) (Recommended) Follow U1/U2 baseline: Kotlin library code within Android Gradle project, AndroidX/toolchain as established, typed contracts/ports for adapters, Gradle/JUnit/Kotlin test tooling consistent with project; UI toolkit/persistence specifics remain U4/NFR Design where applicable. No backend/public API.
- B) Introduce a separate backend service and new persistence stack for U3.
- C) Change language/build system.

[Answer]: A

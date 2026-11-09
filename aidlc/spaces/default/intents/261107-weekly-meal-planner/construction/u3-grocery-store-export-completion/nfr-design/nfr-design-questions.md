# NFR Design Questions — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Date:** 2026-11-09

All questions resolved with Recommended defaults based on NFR requirements, functional design, and ADR-006/R-02.

**Q1. Resilience/fallback for PRISMA/S-ostoslista (OQ2–OQ4)**
- A) (Recommended) Adapter ports return typed outcomes (`PRESENT|NOT_FOUND|UNKNOWN`, `FALLBACK_USED|SUCCESS|FAILED_MAPPED|NOT_SUPPORTED`/equivalent) with reasons/instructions; manual store/checklist and share/copy fallback always available; no retry storms or fabricated success.
- B) Hard-fail on external unavailability.
[Answer]: A

**Q2. Availability projection safety (R-02/BR3.5)**
- A) (Recommended) ShoppingList owns `applyPresenceCheckBatch` as a pure/explicit application step: match by stable item/query key scoped by list+store/scope; preserve unrelated edits/checks; ambiguous identity after edit/merge/split/regenerate/manual add => `UNKNOWN`/`NOT_CHECKED` with reason. StoreAvailability never mutates items.
- B) Best-effort fuzzy remap that may keep stale `PRESENT`/`NOT_FOUND`.
[Answer]: A

**Q3. Export extraction seam (BR3.9)**
- A) (Recommended) Use-case builds `ShoppingListExportDto` from current `ShoppingListAggregate`; platform/share adapter port consumes DTO/request and returns mapped `ExportResult`; dependencies point inward. No Android intent details in domain.
- B) Domain calls Android share intents directly.
[Answer]: A

**Q4. Privacy/diagnostics (NFR3)**
- A) (Recommended) Typed reason codes/messages only; no raw GPS coords, secrets, tokens, or personal payloads in domain diagnostics; location permission/coordinates stay outside U3 core.
- B) Log full adapter payloads and coordinates for debugging.
[Answer]: A

**Q5. Performance/testability (NFR4, NFR5)**
- A) (Recommended) Deterministic local algorithms for MVP scale; pure merge/projection functions testable with fakes; adapter timeouts/batching deferred to implementation but contracts support mapped partial/failure states.
- B) Add caching/indexing infrastructure now.
[Answer]: A
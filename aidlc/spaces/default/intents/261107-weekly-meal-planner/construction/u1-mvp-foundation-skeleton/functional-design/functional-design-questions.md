# Functional Design Questions — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Stage:** Functional Design (construction, per-unit)  
**Unit:** `u1-mvp-foundation-skeleton` — MVP foundation + integration slice  
**Date:** 2026-11-08  
**Answer mode:** Guide me

Answer by filling each `[Answer]:` tag. Keep IDs/enums/status literals as written; add detail after the selected option when useful. If you choose `X. Other (please specify)`, describe the alternative.

## Scope reminder (settled)
- U1 owns C1 baseline; establishes C2–C6 baseline contracts in-app (Kotlin interfaces/use cases/data classes) with typed results, explicit fallback states, and no fabricated data.
- C4 owns shopping-list generation/regeneration orchestration. R-02 requires an explicit C4 operation or clearly named U3-owned orchestrating use case for applying C5 batch presence projection/remap and Unknown-after-edit/merge-regenerate behavior.
- ADR-006/R-01: lock stores recipe IDs/selections + plan context snapshot; generation re-resolves ingredients from C2; stale warning exact text: `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."`; no auto-regeneration.
- PRISMA checks are best-effort name/presence only. S-ostoslista official-compatible export is investigational; share/copy fallback mandatory.

## Batch 1 — U1 baseline contract shapes and ownership boundaries

### Q1. Minimum required DTO/result shapes for U1 baseline contracts (R-01)
For U1 Functional Design, how should we treat minimum required shapes for C2–C6/A1–A3 types that U1 must establish as baselines before dependent Construction relies on them?

A. Define minimum required fields/status values now for all U1-touched baseline types (C1 baseline state, C2 recipe/ingredient query DTOs used by C1/C4, C3 season/override DTOs, C4 lock/snapshot/generation result types including ADR-006 snapshot context, C5 store/presence result types, C6 share payload/fallback result types, A1–A3 reason/status shapes), and mark any intentionally incomplete area as a named defer with owner + minimum fields/status values.  
B. Define only C1 + C4 + C6 minimum shapes now (plan/lock/snapshot/regeneration/share-copy baseline) and defer C2/C3/C5 details to U2 with named owners and required minimum fields/status values.  
C. Define minimum shapes only at Kotlin interface signatures/use-case inputs/outputs level (no exhaustive DTO catalog) and rely on NFR Design/code-generation to refine.  
X. Other (please specify)

[Answer]: A


### Q2. C4 operation/use-case for batch presence projection and remap (R-02)
What explicit C4 operation or clearly named U3-owned orchestrating use case should Functional Design define for applying C5 batch presence results, remapping presence after ingredient/list changes, and Unknown-after-edit/merge-regenerate behavior?

A. Define a U1/C4 baseline operation name now (for example `ApplyStorePresenceBatchUseCase` / `applyBatchPresenceToShoppingList`) with inputs `ShoppingList`, batch `StorePresenceResult`s, and mapping context; state it is invoked by U3 store/check flow and returns updated items with unknown/reason where mapping is ambiguous. U3 may refine implementation but not rename the contract without coordination.  
B. Define only the contract responsibility in C4 now and defer the concrete operation name/signature to U3 Functional Design as an explicit carry-forward with U3 owner.  
C. Split into two operations: C4-owned `ApplyBatchPresenceToList` and C5-owned `MapIngredientsToStorePresenceBatch`; document coordination and unknown-remap rules.  
X. Other (please specify)

[Answer]: C


### Q3. Grocery/export extraction seam in U1 baseline (R-03)
R-03 asks U3 Functional Design to identify the minimum stable grocery/export extraction seam. How much of that seam should U1 Functional Design establish now, given C6 share/copy fallback is mandatory in U1 baseline?

A. U1 defines the minimum stable C6-facing seam now: `ExportShoppingListUseCase` (or equivalent), `ShoppingExportPayload` DTO, and `ShoppingExportAdapter`/platform share port with dependencies pointing inward; include share-sheet/copy-text fallback as the default adapter path. U3 may add official-compatible S-ostoslista adapter behind the same port.  
B. U1 defines only C6 in-app result/fallback types and defers the extraction seam interfaces/ports to U3 Functional Design with explicit U3 owner.  
C. No extraction seam in MVP; keep export/share logic behind a simple C6 service boundary only.  
X. Other (please specify)

[Answer]: B


### Q4. On-device state persistence baseline for U1 foundation
U1 establishes local-first foundation behavior (NFR1/NFR4). What minimum persistence baseline should Functional Design specify for U1 without over-designing storage technology?

A. Specify a logical local persistence baseline for plan state, ADR-006 lock snapshot context (recipe IDs/selections and plan context as defined), shopping-list content/edits, selected store, season override, and source/fallback/status metadata needed for continuity; defer concrete technology choice to NFR Design/Infrastructure Design.  
B. Specify persistence only for plan state and shopping-list lock/snapshot/list content; treat store/season/source metadata as in-memory where acceptable and document risks.  
C. Defer persistence model to NFR Design/Infrastructure Design except for ADR-006 lock snapshot fields, which must be persisted.  
X. Other (please specify)

[Answer]: A

### Q5. US2.2 AC2.2.3 explanation-data ownership in U1
Contract summary states U1/U2 own explanation data/contracts; U4 owns presentation. For U1 Functional Design, what minimum explanation contract should be defined?

A. Define a reusable `ExclusionOrWarningReason` (or equivalent) contract with stable reason code/category, user-safe message key/text source marker, affected entity refs (recipe/slot/ingredient as applicable), and source/fallback context; require it for weekday-time exclusion and ambiguous-time explanation paths U1 establishes, extensible by U2/U3.  
B. Define only weekday-time exclusion explanation fields in plan/recipe candidate results; generalize later in U2.  
C. Keep explanations as plain strings in U1 baseline and defer structured reason contracts to U2/U4.  
X. Other (please specify)

[Answer]: B




## Batch 1 — Analysis / follow-ups

Recorded answers: `Q1=A, Q2=C, Q3=B, Q4=A, Q5=B`.

- **Q2=C (split operations):** compatible with R-02 if responsibilities are explicit. Proposed resolution for artifacts: **C4 owns `ApplyBatchPresenceToList`** (U1 baseline contract name) taking current list/items + mapped presence projections and returning updated items with explicit unknown/reason/remap state; **C5 owns `MapIngredientsToStorePresenceBatch`** taking ingredient refs/query context + selected store + best-effort lookup result and returning batch `StorePresenceResult` projections. C4 never performs PRISMA lookup; C5 never owns list merge/stale semantics. Unknown-after-edit/merge-regenerate behavior remains C4-owned and must be described in C4 workflow/rules; U3 orchestrates the two operations. Confirm in Batch 2.
- **Q3=B (defer seam, U1 keeps fallback types):** workable with mandatory C6 share/copy fallback if U1 defines a simple C6 in-app service/use-case boundary sufficient for share-sheet/copy-text now (e.g., `ShareShoppingListUseCase` or `ExportShoppingListFallbackUseCase` returning typed `ShareResult`/copiable payload) and records an explicit defer for the extraction seam (`ShoppingExportPayload` DTO, platform adapter port, dependencies inward, official-compatible S-ostoslista adapter behind same port) owned by **U3 Functional Design** with minimum required fields/status values. Confirm in Batch 2.
- **Q5=B (weekday-time exclusion fields only):** acceptable if fields are extensible enough for U1 and do not block later generalization by U2. Proposed minimum fields in plan/recipe candidate exclusion results: stable `reasonCode` (e.g., `WEEKDAY_TIME_OVER_30`, `WEEKDAY_TIME_AMBIGUOUS`), user-safe message/text key, affected `slotRef`/recipe candidate ref, and source marker; U2 may extract/generalize to reusable `ExclusionOrWarningReason`. Confirm in Batch 2.

## Batch 2 — Precision follow-ups

### Q6. Q2=C coordination details
Do you accept the proposed split/ownership?

A. Yes: C4 owns `ApplyBatchPresenceToList`; C5 owns `MapIngredientsToStorePresenceBatch`; C4 never does PRISMA lookup; C5 never owns list merge/stale semantics; U3 orchestrates; unknown-after-edit/merge-regenerate remains C4-owned and documented in C4 workflow/rules.  
B. Yes, but use different operation names (specify).  
C. No, prefer a single C4-owned operation after all (return to Q2=A semantics) because C5 mapping should be internal to the store/check flow.  
X. Other (please specify)

[Answer]: A

### Q7. Q3=B U1 fallback boundary + explicit U3 defer
Do you accept this U1 boundary/defer?

A. Yes: U1 defines C6 fallback result/payload types and a simple in-app share/copy use-case/service boundary sufficient for mandatory share-sheet/copy-text fallback now; U3 Functional Design owns the deferred extraction seam with minimum required elements: `ExportShoppingListUseCase` (or equivalent), `ShoppingExportPayload`, `ShoppingExportAdapter`/platform share port, dependencies inward, share-sheet/copy-text default path, official-compatible S-ostoslista adapter behind same port.  
B. Yes, but make the defer owned by U1 NFR Design instead of U3 Functional Design (specify why).  
C. No, U1 should define the extraction seam interfaces now (move toward Q3=A) to avoid U3/U4 rework.  
X. Other (please specify)

[Answer]: A

### Q8. Q5=B minimum weekday-time exclusion fields
Do you accept these minimum fields for U1 baseline exclusion/explanation data in plan/recipe candidate results (U2 generalizes later)?

A. Yes: stable `reasonCode`, user-safe message/text key or localized-safe text source marker, affected slot ref, affected recipe/candidate ref when available, and source marker (`rule|data|fallback` as applicable). Keep shape extensible.  
B. Yes, but include full reusable `ExclusionOrWarningReason` now (closer to Q5=A) to avoid rework.  
C. No, plain strings are sufficient for U1 (specify risk acceptance).  
X. Other (please specify)

[Answer]: A

**Batch 2 recorded:** `Q6=A, Q7=A, Q8=A`. No unresolved ambiguity identified; ready for consolidated summary confirmation before artifact generation.


## Consolidated Summary Confirmation

Decision: Confirm consolidated Functional Design answers for `u1-mvp-foundation-skeleton` before generating `entities.md`, `rules.md`, `functional-spec.md`, and `traceability.json`.

Options:
- Looks correct
- Request changes

[Answer]: Looks correct 
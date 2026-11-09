# Reliability Design — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U1 — MVP foundation + integration slice (Android app)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-08

## Upstream
- [reliability-requirements.md](../nfr-requirements/reliability-requirements.md) (local `NFR1.1`–`NFR1.2`, `NFR2.1`–`NFR2.3`; supports inception NFR2)
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md) (BR1.3, BR1.5–BR1.13)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (ADR-006, C1–C6, A1–A4)
- [nfr-design-questions.md](nfr-design-questions.md) (Q2=A, Q3=A)

## Design goals
- Core planning and shopping-list baseline usable with previously available/local data where possible.
- All integration outcomes explicit; graceful degradation/manual fallback; no fabricated data.
- ADR-006 stale semantics preserved exactly.

## Patterns and decisions

| Area | Design | Addresses | Notes |
|---|---|---|---|
| Local continuity | Persist minimal continuity set via Room + DataStore split: weekly plan state, lock snapshot context, shopping list + checked/edited items, selected store, season override, source/fallback/status metadata. Load local state first on startup/resume. | NFR1.1, NFR2.2 | Exact schema deferred to Code Generation. |
| Typed result states | Use case/adapter results are typed: `ok/available`, `cached`, `fallback`, `unknown`, `unavailable`, `failed_mapped` (or equivalent domain enums) plus reason and next action where applicable. | NFR2.1 | Maps to A1–A3/C1–C6 contracts. |
| Adapter fallback | Bounded timeout + typed fallback result (Q3=A). Sydänmerkki -> curated/imported/local fallback or explicit unavailable; PRISMA -> Unknown with reason + manual store/checklist; S-ostoslista official attempt -> mandatory share sheet/copy text with instructions. | NFR1.2, FR4, FR8, FR10, FR11 | No raw exceptions across UI boundaries. |
| No fabrication | Domain rules prohibit invented ingredients, quantities, units, nutrition, season fit, or availability. Missing/changed data becomes explicit unresolved/unknown state. | NFR2.1, NFR2.3, BR1.8, BR1.13 | Critical for C4 re-resolution and C5 presence. |
| ADR-006 stale state | Plan changes affecting locked context set stale status and preserve exact warning: `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."` No auto-regeneration. Regeneration with existing edits requires explicit confirmation before replace. | NFR2.2, BR1.6–BR1.9 | C1/C4 own state transitions; U4 presents. |
| Recovery/partial data | Show available data plus explicit unavailable/fallback state and next action. Export fallback preserves actionable path (copy/share/instructions). | NFR2.3 | First-run seed-data gaps documented as known risk. |
| Idempotent reads / safe retries | Optional limited retries only for safe idempotent read/lookups; no automatic retries that change user state. | NFR1.2 | Supports adapter resilience without side effects. |

## Failure-mode table

| Failure | Handling | User-visible outcome |
|---|---|---|
| Sydänmerkki live unavailable | Use curated/imported fallback or explicit unavailable | Source/fallback status; planning remains usable where data exists |
| Location denied/unavailable | Manual store selection; season/device fallback/unknown | Clear status and next action |
| PRISMA lookup timeout/failure/inconclusive | `UNKNOWN`/`NOT_FOUND` with reason | Manual checklist fallback available |
| S-ostoslista official method unavailable | C6 share sheet/copy text | Mandatory fallback with instructions |
| Missing/changed recipe/ingredient at generation | Re-resolve from C2; mark unresolved/changed explicitly | No fabricated items |
| Plan changed after lock | Mark stale; no auto-regenerate | Exact ADR-006 warning and explicit Regenerate action |

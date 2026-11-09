## Review

**Verdict:** NOT-READY
**Reviewer:** aidlc-architecture-reviewer-agent
**Date:** [not specified in dispatch brief]
**Iteration:** 1
**Verdict Dictated:** no

### Findings

| ID | Severity | Location | Finding | Required action | Status |
|---|---|---|---|---|---|
| R-01 | Major | `aidlc/spaces/default/intents/261107-weekly-meal-planner/construction/u1-mvp-foundation-skeleton/functional-design/entities.md` > Entities — source of truth / C2 and C3 baseline shapes | R-01/Q1=A requires minimum required fields/status values for all U1-touched baseline types, including C2 recipe/ingredient query DTOs used by C1/C4 and C3 season/override DTOs. The defer register claims C2/C3 are “Defined minimum”, but the YAML entity catalog defines no concrete C2 DTOs (e.g., recipe summary/query/batch result, ingredient line/ref minimum shape) and no C3 DTOs (`SeasonContext`, `SeasonOverride`, effective season resolution result). `ExclusionReason` is defined, but the candidate/query result shapes that must carry weekday-time exclusion hooks are not, so dependent units cannot implement against U1’s baseline from this artifact alone. | Add minimum C2 and C3 entity shapes (or explicitly named contract DTOs) to `entities.md` with required fields and status values sufficient for C1/C4 weekday eligibility/exclusion hooks and season/override resolution, or explicitly defer each missing shape with owner + minimum required fields/status values consistent with R-01. Keep ADR-006 re-resolution, explicit missing/changed data, PRISMA name/presence only, and S-ostoslista fallback semantics unchanged. | New |
| R-02 | Minor | `aidlc/spaces/default/intents/261107-weekly-meal-planner/construction/u1-mvp-foundation-skeleton/functional-design/entities.md` > ENT-U1-015 `DataSourceStatus.source` / contract alignment | `ENT-U1-015` includes `SYDANMERKKI_LIVE`, `SYDANMERKKI_FALLBACK`, `PRISMA`, and `MANUAL` source values, while the passed contract summary `C2` model uses `sydanmerkki_live`, `sydanmerkki_curated_fallback`, `imported_fallback`, `unknown`. The functional design does not state a mapping, so implementers may produce incompatible status values across U1/C2 baselines and U2 completion. | Define an explicit mapping or canonical enum/value set for recipe/source status values shared by `DataSourceStatus` and C2 recipe results, or document that `ENT-U1-015` is a cross-cutting status distinct from `RecipeSummary.sourceType/sourceStatus` and show how they relate. | New |
| R-03 | Minor | `aidlc/spaces/default/intents/261107-weekly-meal-planner/construction/u1-mvp-foundation-skeleton/functional-design/functional-spec.md` > 3.4 Batch presence projection/remap and `aidlc/spaces/default/intents/261107-weekly-meal-planner/construction/u1-mvp-foundation-skeleton/functional-design/rules.md` > BR1.12 | R-02 ownership is clear (C4 `ApplyBatchPresenceToList`, C5 `MapIngredientsToStorePresenceBatch`, U3 orchestrates, C4 owns unknown-after-edit/merge-regenerate), but the minimum operation contracts are described only in prose. There is no baseline request/result shape for C5 batch input or C4 apply/remap input (e.g., projection mapping key, identity/remap context after edits/merge), which can lead to incompatible C4/C5 implementations. | Add minimum request/response shapes or field lists for both operations in `entities.md`/`functional-spec.md`, including how a `StorePresenceResult` maps to a `ShoppingListItem` (ingredient ref/query/item identity) and what C4 requires to keep items `UNKNOWN` with reason when mapping is ambiguous. | New |

### Validation Tool Results

| Tool | Result | Interpretation |
|---|---|---|
| (none) | Not run | No validation tools were listed in the stage definition provided to this review. Findings are based on evidence from the reviewed artifacts and passed upstream contracts. |

### Summary

The design correctly captures ADR-006/R-01–R-03 semantics, the R-02 C4/C5 split, the R-03 C6 fallback boundary with U3-owned extraction seam, and the AC2.2.3 weekday-time exclusion data intent. It is NOT-READY because R-01/Q1=A is only partially satisfied: C2/C3 baseline DTO shapes are claimed as defined but not concretely specified, leaving dependent units without implementable minimum contracts.

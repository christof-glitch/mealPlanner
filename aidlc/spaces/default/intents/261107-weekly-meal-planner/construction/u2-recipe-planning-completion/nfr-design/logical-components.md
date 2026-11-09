# Logical Components — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`, C2/C3 + A1)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [tech-stack-decisions.md](../nfr-requirements/tech-stack-decisions.md) (incl. NFR4.1–NFR4.3)
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (C2/C3/A1)
- [performance/security/scalability/reliability/observability-design.md](performance-design.md)

## Component inventory (logical)

| Component | Responsibility | Failure domain / isolation | NFR patterns |
|---|---|---|---|
| C2 Recipe Catalog/Query | Expose recipe metadata, source status/provenance, season fit evidence, nutrition profile availability, and weekday eligibility inputs for U4/U3. | Read/query failures map to typed empty/unavailable/fallback outcomes; no fabrication. | Deterministic normalization, bounded scans, typed states. |
| Time Normalization Evaluator | Parse/normalize prep+cook totals into `KNOWN|MISSING|AMBIGUOUS`; determine Mon–Fri Breakfast/Dinner eligibility and exclusion reasons (BR2.3). | Isolated pure logic; malformed input becomes `MISSING/AMBIGUOUS` typed result, never guessed. | Pure/deterministic, testable, no PII. |
| Season Fit Evaluator | Compute `IN_SEASON|OUT_OF_SEASON|SEASON_FIT_UNKNOWN` from metadata/tags/curated mapping and resolved season context/overrides (BR2.4). | Insufficient evidence yields `SEASON_FIT_UNKNOWN`; override precedence explicit. | Pure/deterministic, missing-data short-circuit. |
| Nutrition & Cue Calculator | Build `NutritionProfile` availability labels and `WeeklyNutritionBalanceCue` from `WeeklyNutritionBalanceCueRequest` (BR2.5); non-medical, no missing-value fabrication. | Missing profiles excluded/counted; cue reasons explicit. | Missing-data short-circuit, typed labels. |
| Alternatives Selector | Return eligible alternatives within current context; when none, typed result with fallback reason (BR2.6). | Returns empty+reason; never blocks slot state. | Bounded filtering, explicit fallback. |
| C3 Planning State Manager | Apply per-slot `WeeklyPlanState` transitions (set/replace/clear/skip), preserve R-06 single source model, emit stale signaling semantics per BR2.7/BR2.8/ADR-006. | Scoped to target slot; no auto-regeneration; no ingredient freeze. | Deterministic state transitions, typed results. |
| A1 Live Access Gate | Enforce default-off live execution (`allowLiveSource=false`) and require explicit approved OQ1 feasibility/terms/allowed-access decision/config path before live use (FQ1=A). | Blocks unintended live access; routes to curated fallback. | Security default-off, auditable/testable gate. |
| A1 Recipe Source Adapter Port | `RecipeSourceAdapter` interface for live/curated implementations returning typed `RecipeSourceAdapterResult`. | Adapter failures isolated behind port; U2 consumers see typed outcomes only. | Port/adapter, DI for tests/fakes. |
| A1 Resilience Chain | Gate → bounded timeout → limited idempotent read retries (0–2, allowed only) → defensive parse/map → curated fallback resolver. | Timeout/restriction/parse/unavailable/not_supported map to typed status/fallback; no raw errors. | Resilience, graceful degradation, non-blocking async. |
| Curated/Imported Fallback Source | Mandatory local/app-content Sydänmerkki-aligned fallback with provenance (`RecipeSourceStatus`/reason). | Available when live path disabled/fails; explicit status. | Mandatory fallback, no secrets, app content. |
| Input Validator/Mapper | Validate/normalize `PlanSlotRef`, IDs, filters, requests, and A1 query/response shapes at boundaries; produce typed non-fabricated failures. | Malformed input contained; no unsafe propagation. | Validation/sanitization, mapped boundaries. |
| U2 Diagnostics Hook | Injectable hook for structured safe events (op/status/reason/count/timing bucket); default no-op. | Disabled by default in release; never logs PII/secrets/payloads. | Observability + test capture. |

## Dependency flow (logical)
`U4/C1 inputs` → `Input Validator/Mapper` → `C2 evaluators/query` / `C3 Planning State Manager` → typed results/states.  
`C2 catalog request` → `A1 Gate` → (`A1 Adapter Port` via `Resilience Chain` when allowed) or `Curated Fallback Source` → mapped typed result + diagnostics.  
`C3 plan change affecting lock/list context` → stale signaling per ADR-006 exact warning (U3/U4 own list snapshot/regeneration flow).

## Notes
- U2 is a `library`/domain+adapter unit: Android UI, permissions UI, persistence implementation details, and U3/U4 orchestration remain in owning units.
- Logical component names are design targets; Code Generation may refine package/class names while preserving responsibilities and NFR behaviors.
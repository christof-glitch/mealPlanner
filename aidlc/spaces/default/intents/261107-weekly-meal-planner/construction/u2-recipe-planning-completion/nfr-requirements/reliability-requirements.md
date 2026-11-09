# Reliability Requirements — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`, C2/C3 + A1)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Sources
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (FR11, NFR2, NFR5)
- [rules.md](../functional-design/rules.md) (BR2.1, BR2.6, BR2.7, BR2.8, BR2.9)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (A1 fallback, C3 stale semantics)

## Requirements

| ID | Requirement | Target / measure | Inherited from | Notes |
|---|---|---|---|---|
| NFR2.1 | Mandatory curated fallback for recipe source path | A1/Sydänmerkki path has mandatory curated/imported Sydänmerkki-aligned fallback. Live execution is gated by OQ1 feasibility/terms/allowed access (FQ1=A); restriction/failure/timeout/parse/unavailability/not-supported yields typed status and fallback where available, else explicit unavailable. No scraping/terms assumptions. | NFR2, FR4, FR11 | BR2.1. |
| NFR2.2 | Typed explicit states, never silent failure/fabrication | C2/C3/A1 results expose explicit typed statuses, fallback reasons, investigation outcomes, `Season fit unknown`, `Nutrition information unavailable`, weekday exclusion reasons, empty/unknown states. Missing/ambiguous data is never guessed or fabricated. | NFR2, FR3, FR5, FR6, FR11 | BR2.2–BR2.6, BR2.9. |
| NFR2.3 | Plan state integrity and no auto-regeneration | `WeeklyPlanState` remains single source of truth for Today/Plan (R-06/BR2.7). Slot operations affect only target slot. Plan changes affecting active lock/list context mark stale per ADR-006/BR1.9 with exact warning `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."`; U2 never triggers auto-regeneration. | NFR2, FR9 | BR2.7, BR2.8. |
| NFR2.4 | Graceful degradation for local planning | Core recipe viewing/selection/planning contracts remain usable with curated/local data when live sources fail; adapter failures do not crash or block unrelated slot/plan operations. Errors map to user-safe/status outcomes for U4. | NFR2, FR11, NFR5 | BR2.9. |
| NFR2.5 | Deterministic normalization/eligibility behavior | Weekday time normalization is deterministic and no-guess (`KNOWN|MISSING|AMBIGUOUS`; eligible only when KNOWN and <= 30 minutes for Mon–Fri Breakfast/Dinner). Season fit returns `SEASON_FIT_UNKNOWN` when evidence insufficient. | NFR2, FR3, FR5 | Supports testability and user trust. |

## Availability / recovery (MVP scope)
- No server SLA/SLO. Reliability is achieved through local-first behavior, typed fallbacks, and explicit states. Persistence/durability of plan state is shared with U1/U4 and refined in NFR Design.
# NFR Security & Privacy Requirements — U4 `u4-android-ui-experience`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Scope
UI handling of permissions, user-visible diagnostics, export/share handoff, and privacy messaging for RM-01–RM-09. U4 is on-device presentation; no MealPlanner account/backend introduced.

## Upstream
- [functional-spec.md](../functional-design/functional-spec.md) (RM-01/RM-06–RM-09, fallback/privacy UX)
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (NFR2, NFR3, NFR5, FR7/FR8/FR10/FR12)
- Domain rules context from U1–U3/U4 functional design (R-02/R-03, BR3.5/BR3.9, ADR-005/ADR-006)

## Requirements

| ID | Requirement | Target / measure | Verification | Source |
|---|---|---|---|---|
| NFR3.1-U4 | No MealPlanner account, authentication, or unnecessary identity collection in UI flows. | Zero sign-in/account screens or identifiers required for MVP flows. | Code review/tests assert no auth/account state. | NFR3.1, FR12 |
| NFR3.2-U4 | Location is optional and purpose-limited (PRISMA suggestion). UI requests permission with purpose text and always offers manual store selection. | RM-01/RM-07/RM-09 show purpose + manual path; denied permission never blocks store selection. | UI state tests for permission denied/granted/not-asked; manual review. | NFR3.2, FR7, FR12, US5.1, US5.2 |
| NFR3.3-U4 | UI and diagnostics never expose raw GPS coordinates, secrets, API keys, or private endpoints. | Store/permission states use typed/coarse labels only; logs/telemetry hooks privacy-safe. | Code review; fake-based tests; lint/review for accidental secret strings. | NFR3.3, NFR3.4 |
| NFR3.4-U4 | Export/share uses current edited shopping list payload only and explicit user action. Official S-ostoslista path remains investigational; mandatory share/copy fallback with instructions always available. | No background export; RM-06/RM-08 actions user-initiated; fallback shown on unavailability/failure/cancel preserves list. | UI/facade tests with fake export results. | FR10, NFR3.5, US4.1, US4.2, OQ4 |
| NFR3.5-U4 | Availability UI reflects name/presence only (`PRESENT`/`NOT_FOUND`/`UNKNOWN` + reason); never stock quantity or guaranteed availability. | Labels/badges copy and state models enforce presence semantics; manual checklist fallback available. | UI/state tests; copy review. | FR8, NFR5.3, US3.3 |
| NFR3.6-U4 | Accessibility and privacy labels are clear and non-deceptive (no medical claims, no fabricated certainty). | Nutrition/season/presence unavailable states explicitly labeled; WCAG 2.2 AA practical baseline. | Accessibility/manual review + UI tests for missing-data labels. | NFR2.3, NFR2.4, FR5, FR6, AC2.4.4 |

## Constraints / notes
- GDPR-aware MVP posture: on-device/local data where possible, minimal collection, transparent purpose text (NFR3). Any future analytics/remote integration requires separate review.
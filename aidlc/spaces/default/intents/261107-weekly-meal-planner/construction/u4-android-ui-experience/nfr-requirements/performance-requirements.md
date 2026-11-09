# NFR Performance Requirements — U4 `u4-android-ui-experience`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Scope
UI responsiveness, rendering/state update behavior, and interaction latency for RM-01–RM-09 on supported Android devices. External adapter latency (Sydänmerkki/PRISMA/S-ostoslista) remains investigational; UI must remain responsive via loading/fallback states.

## Upstream
- [functional-spec.md](../functional-design/functional-spec.md) (RM flows, R-05/R-06, thin orchestration)
- [frontend-components.md](../functional-design/frontend-components.md)
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (NFR1, NFR2)
- Domain rules context from U1–U3/U4 functional design (BR/ADR invariants; UI does not duplicate rule ownership)

## Requirements

| ID | Requirement | Target / measure | Verification | Source |
|---|---|---|---|---|
| NFR1.1-U4 | Primary local UI interactions (tab navigation, slot selection/replace confirmation, checklist toggle/edit local state, dialog open/cancel) remain responsive. | User-perceivable response without blocking main thread; state updates render within normal Compose frame budget on MVP test device/emulator profile (no ANR). | Manual/UI smoke on debug build + unit tests for state reducers/facades; Android vitals/ANR-free expectation in later QA. | NFR1.1, NFR1.2 |
| NFR1.2-U4 | Screen transitions and full-screen flows (RM-04/RM-05/RM-07) preserve context and restore focus without jank from avoidable recomposition/data reload. | Back-stack and focus behavior per R-06; avoid unnecessary full-screen recomposition by stable keys/state hoisting. | Navigation/state tests; manual TalkBack/focus checks. | R-06, NFR1.2 |
| NFR1.3-U4 | Data-dependent screens show explicit loading/skeleton/empty/error/fallback states while domain/adapter work is in progress. | No blank blocking state for primary screens; R-05 next actions visible when applicable. | UI state tests with fake facades returning loading/error/empty/fallback. | NFR1.3, NFR2.1, R-05 |
| NFR1.4-U4 | Grocery list rendering supports typical MVP list sizes (selected 7-day plan ingredients, merged/editable items) without interaction loss. | Checklist scroll/edit/check remains usable; long operations (generate/regenerate/export/share handoff) run off main thread with progress/status. | Unit/fake-backed performance sanity; manual test with representative fixture. | FR9, FR10, NFR1.2 |

## Constraints / notes
- Performance targets are MVP/practical (no premature optimization). Investigational external integrations may be slow/unavailable; UI must degrade gracefully (NFR2, OQ1–OQ4).
- Domain rules remain in U1–U3; UI performance work must not move business logic into composables.
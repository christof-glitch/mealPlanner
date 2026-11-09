# NFR Performance Design — U4 `u4-android-ui-experience`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [performance-requirements.md](../nfr-requirements/performance-requirements.md) (NFR1.1-U4–NFR1.4-U4)
- [functional-spec.md](../functional-design/functional-spec.md) (R-05/R-06, RM-01–RM-09)
- [tech-stack-decisions.md](../nfr-requirements/tech-stack-decisions.md)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (C2–C6 boundaries)

## Design decisions

| Requirement | Design pattern/decision | Verification hook |
|---|---|---|
| NFR1.1-U4 | Compose unidirectional state: ViewModel/state-holder emits immutable `UiState`; user intents dispatched as events. Keep domain calls off main thread via coroutines; local checklist edits update state locally and persist through domain/facade contracts as applicable. | Fake-facade unit tests for state transitions; manual debug smoke/ANR observation. |
| NFR1.2-U4 | Navigation preserves `slotContext`/origin in route args or saved state handle; shared plan state observed by Today/Plan from same source/facade scope. Use stable lazy-list keys for days/slots/items; avoid reloading full aggregates on tab switch where cached state is valid. Focus request after selection/return per interaction spec. | Navigation tests; manual focus/TalkBack check. |
| NFR1.3-U4 | Every primary screen models `loading/empty/error/fallback/partial` plus R-05 next actions. Long operations expose progress/status; cancellations/timeouts surfaced as typed fallback states, never blank screens. | UI state tests with fake results; screenshot/manual review. |
| NFR1.4-U4 | Grocery checklist uses `LazyColumn`/efficient item keys; edits/checks are local state mutations with debounced/bounded persistence as needed (implementation detail). Generate/regenerate/export/share run via suspending use cases; UI shows status and confirmation dialog when `hasUserEdits`. | Representative fixture sanity test; unit tests for confirmation gate. |

## Trade-offs
- MVP practicality over micro-optimization. If future profiling shows recomposition/jank, add derived state/memoization/selectors without moving business rules into UI.
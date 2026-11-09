# Logical Components — U4 `u4-android-ui-experience`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [functional-spec.md](../functional-design/functional-spec.md)
- [frontend-components.md](../functional-design/frontend-components.md)
- [performance-design.md](performance-design.md)
- [security-design.md](security-design.md)
- [tech-stack-decisions.md](../nfr-requirements/tech-stack-decisions.md)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md)

## Component inventory (UI logical boundaries)

| Logical component | Responsibility | Isolation / blast radius | NFR patterns applied |
|---|---|---|---|
| `NavigationShell` | Bottom nav, route graph, back-stack, full-screen/pushed flows | UI-only; route arg validation prevents invalid slot context crashes | R-06 context preservation, focus management |
| `PlanExperienceFacade` | Shared Today/Plan state and intents over C3/lock services | Failure isolated to plan screens; fallback/error state rendered | Single source of truth, no rule duplication (ADR-005) |
| `RecipeExperienceFacade` | Detail/alternatives presentation over C2/U2 | Isolated per detail/alternatives flow; domain provides eligibility | Typed fallback/source labels, R-05 states |
| `GroceryExperienceFacade` | Lock/readiness/generate/regenerate/edit/export orchestration over C4/C6 | Confirmation gate for edited regenerate; export failures preserve list | R-03, ADR-006, explicit user actions |
| `StoreAvailabilityFacade` | Store suggestion/manual selection and presence display over C5/C4 projection | Manual fallback always available; lookup failures isolated | FR8 name/presence only, NFR3.2/NFR2 |
| `UserSettingsFacade` | Season override and permission/privacy status over preferences/platform boundaries | Invalid override falls back to default; no sensitive data stored | NFR3, AC2.3.4 |
| `PermissionStatusProvider` (platform boundary) | Runtime permission status/requests | Platform interaction isolated from domain; denied path supported | Purpose-limited location, manual fallback |
| `ShareExportPort` / clipboard/share platform boundary | Android share sheet/copy | Platform-specific; domain receives typed outcomes only (BR3.9) | Mandatory fallback, explicit action |
| `DiagnosticsUiMapper` | Typed outcomes -> user-safe labels/actions | Prevents leakage of raw diagnostics to UI copy | Privacy-safe messaging, no secrets/raw GPS |

## Shared resources
- Compose theme/typography (Material 3) and accessibility helpers are shared UI resources; changes require visual/accessibility regression checks.
- Domain use cases/repositories are shared dependencies injected into facades; UI does not own their lifecycle rules.
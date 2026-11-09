# Security Design — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`, C4/C5/C6)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [security-requirements.md](../nfr-requirements/security-requirements.md) (NFR3.1–NFR3.7)
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md) (BR3.5–BR3.10)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (C4–C6, A2–A3)
- [nfr-design-questions.md](nfr-design-questions.md) (Q1=A, Q3=A, Q4=A)

## Design goals
- Enforce GDPR-aware minimal data handling: no MealPlanner account, no unnecessary personal data, no raw GPS coordinates in U3 domain state.
- Treat PRISMA/S-ostoslista/share/export targets as untrusted boundaries with typed mapped outcomes and mandatory fallbacks.
- Preserve FR8 invariant: name/presence only (`PRESENT|NOT_FOUND|UNKNOWN`), never stock quantity or guaranteed availability.
- Prevent fabrication of ingredients, merge results, presence, or export success.

## Patterns and decisions

| Area | Design | Addresses | Notes |
|---|---|---|---|
| No account/secrets | U3 contracts and implementations contain no auth, user identity, API keys, tokens, or hardcoded private endpoints. Adapter config, if ever needed, is injected outside committed source. | NFR3.1, NFR3.4 | MVP share/copy fallback requires no secrets. |
| Minimal local data | Domain state limited to shopping list items/edits/checks/merge metadata, selected store ref/label metadata, availability status/reason, stale status, and export payload derived from current edited list. | NFR3.2 | Exact persistence schema deferred to U4/NFR Design. |
| Location boundary | C5 accepts typed suggestion/selection states (`NONE|MANUAL|NEAREST_SUGGESTION`, `DENIED|UNAVAILABLE|UNKNOWN|NOT_FOUND`/equivalent). U3 never requests Android permissions and never stores raw coordinates. Manual store selection always valid input. | NFR3.3, FR7, FR12 | Permission prompt/purpose text owned by U4/platform. |
| Untrusted adapter mapping | A2/A3 adapters return raw/transport-specific data only behind ports; U3 maps to typed domain statuses/reasons. Unknown/inconclusive presence becomes `UNKNOWN` with reason, never `PRESENT`/stock/guarantee. | NFR3.6, FR8, BR3.7 | No remote content executed as instructions. |
| Privacy-safe diagnostics | Status reasons/messages and investigation outcomes use coarse typed/safe text; exclude raw coords, secrets, contact data, and unnecessary identifiers. `ExportResult.investigationOutcome` uses coarse values (e.g., `USED|PARTIALLY_USED|NOT_FEASIBLE_CONFIRMED|UNKNOWN`) mapped to C6/A3 semantics where needed. | NFR3.5, NFR3.7, NFR2.7 | Resolves NFR Requirements review minor by requiring explicit mapping in implementation/contracts. |
| Current-state export only | Export use case builds `ShoppingListExportDto` from current `ShoppingListAggregate`; adapters receive DTO/request only, never stale pre-edit copies or hidden list snapshots. | NFR3.6, FR10, BR3.3, BR3.8 | Share/copy fallback mandatory. |
| Inward dependency seam | Domain/application owns export use-case interface and DTO; platform share/copy/S-ostoslista adapters implement ports. No Android intent/clipboard types in domain package. | BR3.9 | Supports safe future extraction. |

## Illustrative seam (non-normative)
```kotlin
interface ExportShoppingListUseCase { fun export(req: ExportRequest): ExportResult }
interface ShareExportPort { fun share(payload: ShoppingListExportDto): ExportResult }
```

## Threat/compliance considerations
- **Data leakage:** no coords/secrets/PII in U3 logs or DTOs beyond necessary store label/ref and list content user chose to export.
- **Fabricated availability/export:** typed mapping + tests prevent false `PRESENT`/`SUCCESS`.
- **Integration failure:** fallback-first ports avoid dead ends (NFR2.5/NFR2.6).
- GDPR-aware/minimal data posture aligns with MVP; formal legal review outside this artifact. OQ2–OQ4 feasibility/terms remain open.
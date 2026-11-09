# Logical Components — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md)
- [security-design.md](security-design.md), [reliability-design.md](reliability-design.md)
- [tech-stack-decisions.md](../nfr-requirements/tech-stack-decisions.md)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (C4–C6, A2–A3)

## Component inventory

| Component | Responsibility | Owns | Depends on (ports/inward) | Failure domain / notes |
|---|---|---|---|---|
| `ShoppingListUseCase` / C4 application service | Generate/regenerate from locked snapshot, apply edits, compute stale/status, build export DTO from current state | `ShoppingListAggregate`, `ShoppingItem` state transitions, confirmation gate, ADR-006 stale warning | C2 recipe-resolution port, lock/snapshot input, A2 batch-apply input via C4 operation | Local domain failures return typed `GenerationResult`; never fabricate. |
| `IngredientMergeService` | Normalize candidates and produce deterministic merge/separate decisions | `MergeDecision` logic, merge keys/provenance | Pure input (ingredient candidates) | Uncertain compatibility => separate with reason (`NFR2.1/NFR2.6` safety via no fabrication). |
| `AvailabilityProjectionService` (C4-owned) | Explicit `applyPresenceCheckBatch` and remap after identity changes | `availabilityStatus/availabilityReason` projection | `PresenceCheckBatchResult` DTO, current list state | Ambiguity => `UNKNOWN`/`NOT_CHECKED`; C5/A2 cannot mutate items (R-02). |
| `StoreAvailabilityPort` / A2 adapter boundary | Nearest suggestion lookup (best effort) and name/presence checks | Raw external lookup/check mapping behind port | Platform/location provider (outside U3 core), PRISMA adapter implementation (investigational OQ2/OQ3) | Returns typed unavailable/denied/unknown/partial; name/presence only. |
| `ExportShoppingListUseCase` | Orchestrate export from current edited list | `ExportRequest` handling, fallback decision based on adapter result | `ShoppingListAggregate` current state, `ShoppingListExportDto` builder, export port(s) | Must produce `FALLBACK_USED` with instructions when official path not feasible/failed. |
| `ShoppingListExportDtoBuilder` | Build stable extraction-seam payload | `ShoppingListExportDto`, `ExportLineItem` | Current aggregate only | Prevents stale pre-edit export. |
| `ShareExportPort` / A3 platform adapter boundary | Share sheet/copy text and optional S-ostoslista official attempt | Platform intent/clipboard/S-ostoslista integration | Android/platform implementation (U4/infra), optional official method (OQ4) | Mandatory share/copy fallback always available. |

## Boundaries and blast radius
- **Domain core (C4 merge/projection/export DTO):** pure/testable; failures localized to list operations.
- **External adapters (A2/A3, platform share):** isolated behind ports; mapped typed failures cannot corrupt list state or fabricate availability/export success.
- **U4/UI:** presents statuses/reasons/instructions and handles permissions/intents; U3 supplies contracts only.
- **Extraction seam (BR3.9):** export use-case interface + DTO + share port allow future module/service extraction without changing domain semantics.

## Traceability anchors
- NFR1.x: on-device library, MVP bounds, bounded batches.
- NFR2.x: confirmation/stale/exact warning, safe projection/remap, fallback-first export/presence, typed statuses.
- NFR3.x: no secrets/coords/PII logging, minimal data, untrusted mapping.
- NFR4.x: fakeable ports, deterministic core, CI-preserved quality.
- NFR5.x: O(n) local operations, bounded adapter consumption.
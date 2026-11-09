# U3 Functional Design — Entities (`u3-grocery-store-export-completion`)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Stage:** Functional Design (construction, per-unit)  
**Date:** 2026-11-09

## 1. Purpose
Logical entity model for ShoppingList completion (C4), StoreAvailability completion (C5), and Export/fallback completion (C6/A2/A3). U1 baselines and C2/C3 contracts remain authoritative (`LockedRecipeSnapshot`, `RecipeDetail`/ingredient lines, `WeeklyPlanState` stale signals). ShoppingList owns availability projection (ADR-006/R-02).

## Upstream references
- [unit-of-work.md](../../../inception/units-generation/unit-of-work.md) (U3 scope, dependencies, responsibilities)
- [components.md](../../../inception/domain-design/components.md) (ShoppingListComponent, StoreAvailabilityComponent ownership)

## 2. Entities (source of truth)

```yaml
entities:
  - name: ShoppingListGenerationRequest
    description: Explicit user request to generate or regenerate a shopping list from a locked snapshot (R-03).
    attributes:
      - { name: snapshotId, type: String, required: true }
      - { name: operation, type: Enum, required: true, values: [GENERATE, REGENERATE] }
      - { name: confirmRegenerateIfEdited, type: Boolean, required: false, default: false }
      - { name: requestedAt, type: DateTime, required: false }
    relationships:
      - { to: LockedRecipeSnapshot, cardinality: "N:1", via: snapshotId }

  - name: ShoppingListAggregate
    description: Current editable shopping-list state for the active lock/generation context.
    attributes:
      - { name: shoppingListId, type: String, required: true, unique: true }
      - { name: snapshotId, type: String, required: false }
      - { name: sourceRecipeIds, type: "List<String>", required: false }
      - { name: items, type: "List<ShoppingItem>", required: true }
      - { name: listStatus, type: Enum, required: true, values: [EMPTY, GENERATED, PARTIAL, STALE, ERROR_MAPPED] }
      - { name: staleStatus, type: ShoppingListStaleStatus, required: false }
      - { name: hasUserEdits, type: Boolean, required: true, default: false }
      - { name: generationNotes, type: "List<String>", required: false }
      - { name: lastGeneratedAt, type: DateTime, required: false }
      - { name: lastEditedAt, type: DateTime, required: false }
    invariants:
      - "Exports always use current edited items, never pre-edit copy (FR10/AC4.1.3)."
      - "No fabricated ingredients/amounts; missing/changed source data surfaced explicitly (FR9/AC3.1.4)."

  - name: ShoppingItem
    description: Editable shopping-list line item with merge/provenance and projected availability (C4).
    attributes:
      - { name: shoppingItemId, type: String, required: true, unique: true }
      - { name: displayText, type: String, required: true }
      - { name: normalizedName, type: String, required: false }
      - { name: quantity, type: Decimal, required: false }
      - { name: unit, type: String, required: false }
      - { name: quantityText, type: String, required: false }
      - { name: checked, type: Boolean, required: true, default: false }
      - { name: userEdited, type: Boolean, required: true, default: false }
      - { name: sourceRecipeIds, type: "List<String>", required: false }
      - { name: sourceIngredientRefs, type: "List<String>", required: false }
      - { name: mergeKey, type: String, required: false }
      - { name: ingredientDataStatus, type: Enum, required: true, values: [COMPLETE, INCOMPLETE, MISSING_SOURCE, MANUAL] }
      - { name: availabilityStatus, type: Enum, required: true, values: [PRESENT, NOT_FOUND, UNKNOWN, NOT_CHECKED] }
      - { name: availabilityReason, type: String, required: false }
      - { name: availabilityCheckedAt, type: DateTime, required: false }
      - { name: selectedStoreId, type: String, required: false }
      - { name: presenceQueryKey, type: String, required: false }

  - name: ShoppingItemPatch
    description: User edit to a shopping item before export.
    attributes:
      - { name: itemId, type: String, required: true }
      - { name: displayText, type: String, required: false }
      - { name: quantity, type: Decimal, required: false }
      - { name: unit, type: String, required: false }
      - { name: quantityText, type: String, required: false }
      - { name: checked, type: Boolean, required: false }
      - { name: removed, type: Boolean, required: false }

  - name: MergeDecision
    description: Deterministic merge outcome for candidate ingredient lines.
    attributes:
      - { name: decision, type: Enum, required: true, values: [MERGED, SEPARATE, MANUAL_REVIEW] }
      - { name: mergeKey, type: String, required: false }
      - { name: reasonCode, type: Enum, required: true, values: [NAME_UNIT_COMPATIBLE, UNIT_INCOMPATIBLE, AMOUNT_INCOMPATIBLE, MISSING_DATA, UNSAFE_TO_MERGE] }
      - { name: explanation, type: String, required: false }

  - name: GenerationResult
    description: Result of generate/regenerate operation.
    attributes:
      - { name: status, type: Enum, required: true, values: [GENERATED, CONFIRMATION_REQUIRED, INVALID_SNAPSHOT, PARTIAL, FAILED_MAPPED] }
      - { name: shoppingList, type: ShoppingListAggregate, required: false }
      - { name: missingOrChangedRecipeIds, type: "List<String>", required: false }
      - { name: message, type: String, required: false }
      - { name: staleStatus, type: ShoppingListStaleStatus, required: false }

  - name: ShoppingListStaleStatus
    description: ADR-006/R-03 stale state for generated list vs current plan/lock context.
    attributes:
      - { name: snapshotId, type: String, required: false }
      - { name: isStale, type: Boolean, required: true }
      - { name: staleWarning, type: String, required: false, default: "Meal plan modified. Grocery list not up-to-date. Press the Regenerate button." }
      - { name: staleReason, type: String, required: false }

  - name: StoreSelectionState
    description: Selected PRISMA/manual store context (C5).
    attributes:
      - { name: selectedStore, type: PrismaStoreRef, required: false }
      - { name: selectionMode, type: Enum, required: true, values: [NONE, MANUAL, NEAREST_SUGGESTION] }
      - { name: suggestionStatus, type: Enum, required: false, values: [OK, UNAVAILABLE, DENIED, NOT_FOUND, UNKNOWN] }
      - { name: message, type: String, required: false }

  - name: PrismaStoreRef
    description: Store identity/label used for selection and presence checks.
    attributes:
      - { name: storeId, type: String, required: true }
      - { name: displayName, type: String, required: false }
      - { name: addressSummary, type: String, required: false }
      - { name: sourceStatus, type: String, required: false }

  - name: PresenceCheckQuery
    description: Item-scoped best-effort ingredient name/presence query (FR8).
    attributes:
      - { name: itemRef, type: String, required: true }
      - { name: queryName, type: String, required: true }
      - { name: normalizedQueryName, type: String, required: false }
      - { name: mergeKey, type: String, required: false }

  - name: PresenceCheckResult
    description: Raw store-availability check result keyed to item/query context (C5/A2).
    attributes:
      - { name: itemRef, type: String, required: true }
      - { name: queryName, type: String, required: true }
      - { name: resultStatus, type: Enum, required: true, values: [PRESENT, NOT_FOUND, UNKNOWN] }
      - { name: reason, type: String, required: false }
      - { name: checkedAt, type: DateTime, required: false }
      - { name: storeId, type: String, required: false }
      - { name: checkScope, type: String, required: false }

  - name: PresenceCheckBatchResult
    description: Batch of presence results for application by ShoppingList projection.
    attributes:
      - { name: storeId, type: String, required: false }
      - { name: results, type: "List<PresenceCheckResult>", required: true }
      - { name: batchStatus, type: Enum, required: true, values: [OK, PARTIAL, UNAVAILABLE, DENIED, FAILED_MAPPED] }
      - { name: message, type: String, required: false }

  - name: ApplyPresenceResultsRequest
    description: Explicit C4 operation to apply/remap batch presence results (project rule).
    attributes:
      - { name: shoppingListId, type: String, required: true }
      - { name: storeId, type: String, required: false }
      - { name: batch, type: PresenceCheckBatchResult, required: true }
      - { name: allowIdentityRemap, type: Boolean, required: false, default: true }

  - name: ShoppingListExportDto
    description: Stable extraction-seam DTO containing current edited list data for export adapters (inward deps).
    attributes:
      - { name: shoppingListId, type: String, required: true }
      - { name: snapshotId, type: String, required: false }
      - { name: exportedAt, type: DateTime, required: false }
      - { name: items, type: "List<ExportLineItem>", required: true }
      - { name: selectedStore, type: PrismaStoreRef, required: false }
      - { name: staleWarning, type: String, required: false }
      - { name: notes, type: "List<String>", required: false }

  - name: ExportLineItem
    description: Flattened current-state export line (checked/text/quantity as edited).
    attributes:
      - { name: displayText, type: String, required: true }
      - { name: checked, type: Boolean, required: true }
      - { name: quantityText, type: String, required: false }
      - { name: availabilityStatus, type: String, required: false }
      - { name: availabilityReason, type: String, required: false }

  - name: ExportRequest
    description: Request to export current edited list via preferred method.
    attributes:
      - { name: payload, type: ShoppingListExportDto, required: true }
      - { name: preferredMethod, type: Enum, required: true, values: [S_OSTOSLISTA_OFFICIAL_IF_AVAILABLE, SHARE_SHEET, COPY_TEXT] }

  - name: ExportResult
    description: Mapped export outcome with mandatory fallback behavior.
    attributes:
      - { name: status, type: Enum, required: true, values: [SUCCESS, FALLBACK_USED, FAILED_MAPPED] }
      - { name: methodUsed, type: Enum, required: true, values: [S_OSTOSLISTA_OFFICIAL_IF_AVAILABLE, SHARE_SHEET, COPY_TEXT] }
      - { name: instructions, type: String, required: false }
      - { name: message, type: String, required: false }
      - { name: investigationOutcome, type: Enum, required: false, values: [USED, PARTIALLY_USED, NOT_FEASIBLE_CONFIRMED, UNKNOWN] }
```

## 3. Entity summary
- **C4:** `ShoppingListGenerationRequest`, `ShoppingListAggregate`, `ShoppingItem`, `ShoppingItemPatch`, `MergeDecision`, `GenerationResult`, `ShoppingListStaleStatus`, `ApplyPresenceResultsRequest` implement locked-snapshot generation (ADR-006 re-resolve), deterministic merge/no fabrication, edits, regeneration confirmation, stale warning, and ShoppingList-owned availability projection.
- **C5/A2:** `StoreSelectionState`, `PrismaStoreRef`, `PresenceCheckQuery`, `PresenceCheckResult`, `PresenceCheckBatchResult` model best-effort PRISMA name/presence checks (never stock/guaranteed availability), manual store always available, `UNKNOWN` with reason.
- **C6/A3 + seam:** `ShoppingListExportDto`, `ExportLineItem`, `ExportRequest`, `ExportResult` provide stable grocery/export extraction seam (export use-case interface input/output, payload DTO, platform/share adapter port consumes request/payload) with mandatory share/copy fallback and current edited list state.

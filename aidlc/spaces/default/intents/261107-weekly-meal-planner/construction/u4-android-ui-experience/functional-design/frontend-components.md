# U4 Frontend Components — Android UI (`u4-android-ui-experience`)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** Functional Design (construction, per-unit)  
**Date:** 2026-11-09  
**Design system:** Material 3 / Jetpack Compose (brownfield app already uses Compose Material 3)

## 1. Navigation/components map
```yaml
navigation:
  root:
    bottom_nav: [TodayRoute, PlanRoute, GroceryRoute, UserRoute]
  pushed:
    - RecipeDetailRoute(slotContext?, recipeId)
  full_screen:
    - ReplaceAlternativesRoute(slotContext)
    - SelectPrismaStoreRoute(currentStoreRef?, suggestionState)
  dialogs:
    - RegenerateEditedListConfirmationDialog
    - SystemPermissionRequests (platform)
    - AndroidShareSheet (platform, via share port)
```

## 2. Shared UI primitives
| Component | Kind | Responsibility | Accessibility/behavior |
|---|---|---|---|
| `MealPlannerScaffold` | layout | Bottom nav, top bars, snackbar/status host, safe areas | Focus to title/main content on navigation; WCAG 2.2 AA contrast |
| `StatusBanner` | feedback | Loading/empty/error/fallback/partial/investigational status + next action | Text not color-only; announced via live region semantics as implemented |
| `NutritionBalanceCueCard` | display | View-only today/week cue from reliable attributes; partial/unavailable labels | Non-medical; never blocks selection; announces scope/state |
| `SeasonContextIndicator` | display | `Using device date/location` or `Using override...` | Reflects RM-09 state (AC2.3.4) |
| `MealSlotCard` | display/action | Slot label + filled/unfilled state, badges, Replace/View/Choose actions with slot context | Labels include slot context; unfilled remains empty (R-06) |
| `RecipeMetaBadges` | display | `≤30 min`, season fit/unknown, nutrition cue/unavailable, source/fallback labels | Explicit text labels |
| `StorePresenceStatusRow` | display | `PRESENT`/`NOT_FOUND`/`UNKNOWN` + reason; name/presence only | Never stock/guaranteed availability |
| `RegenerateConfirmationDialog` | dialog | Confirms regenerate when edits/check-offs exist (R-03/AC3.2.3) | Clear consequence text; Cancel/Regenerate and replace; focus trap |
| `ErrorRetryPanel` | feedback/action | Retry/bundled fallback/manual planning/manual selection actions | No dead-end core flows |

## 3. Screen component breakdown

### RM-01 `OnboardingScreen`
- Inputs: first-run flag, permission state, privacy-ack state, data bootstrap state.
- Outputs/intents: allow location, choose manually later, view privacy, continue, skip-after-notice.
- Composed: value cards, location purpose card, privacy card, CTA row.
- States: first run; granted/denied/not asked; R-05 no-local-data+remote-unavailable.

### RM-02 `TodayScreen`
- Inputs: `TodayScreenState` (date, slots for today, nutrition cue, season context, sync/error/fallback state).
- Outputs: view recipe, replace/choose slot, retry/bootstrap actions.
- Composed: `SeasonContextIndicator`, `NutritionBalanceCueCard`, list of `MealSlotCard`.
- States: populated/partial/empty/loading/error/fallback/R-05.

### RM-03 `PlanScreen`
- Inputs: `PlanScreenState` (week days/slots, lock status, nutrition cue, season context, sync/error/fallback state).
- Outputs: select/replace/view slot, lock recipes for shopping, navigate grocery, retry/bootstrap.
- Composed: week sections/day groups, `MealSlotCard`, lock status card/control.
- States: filled/partial/none/loading/error/fallback/locked-unlocked/R-05.

### RM-04 `RecipeDetailScreen` (pushed)
- Inputs: `RecipeDetailState` + `slotContext?`.
- Outputs: use for slot, choose different (same slot), back.
- Composed: summary, season/time/weekday badges, nutrition section, ingredients, prep notes, actions.
- States: loaded/loading/error/source-unavailable-fallback/missing data labels/default/overridden season context.

### RM-05 `ReplaceAlternativesScreen` (full screen)
- Inputs: `AlternativesState` with slot context/origin/eligibility explanations/alternatives.
- Outputs: view/select/cancel.
- Composed: rules summary, alternative cards, footer actions.
- States: loading/populated/empty/error/selected/cancelled. Selection returns to origin and focuses updated slot.

### RM-06 `GroceryScreen`
- Inputs: `GroceryScreenState` (lock/readiness, list items, hasEdits, stale status, store/availability, export capability/result).
- Outputs: lock link, generate, regenerate, edit/check/add/remove, check presence, choose store, export/share/copy.
- Composed: readiness card, store card, availability section, checklist editor, export actions, `RegenerateConfirmationDialog`.
- States: recipes-not-locked/locked-not-generated/loading/default/edited/stale/regenerate-confirm/empty/partial/availability-updated/export states.

### RM-07 `SelectPrismaStoreScreen` (full screen)
- Inputs: `StoreSelectionState` (permission, suggestion, current store, lookup state).
- Outputs: use suggestion, select manual, confirm, manage permission, back.
- Composed: suggestion card, manual selector/search UI (MVP-appropriate), confirm actions.
- States: default/permission-needed/suggesting/suggested/lookup-failed/selected.

### RM-08 Export/share UI (section/dialog + system flows)
- Inputs: export capability/result, current edited payload/instructions.
- Outputs: official export attempt (if exposed), share sheet, copy text.
- Composed: export action row, fallback instructions, status messages.
- States: default/official/fallback/success/cancelled/error.

### RM-09 `UserScreen`
- Inputs: `UserSettingsState` (season override, permission/privacy status).
- Outputs: set/reset season context, open settings, view privacy.
- Composed: season context controls, permissions card, privacy/data card.
- States: default active, override on/invalid, reset, permission states.

## 4. UI state contracts (presentation)
```yaml
ui_state_contracts:
  - name: MealSlotUiModel
    fields: [slotId, dayOfWeek, mealType, recipeSummary?, isEmpty, badges, sourceLabel?, statusLabels]
  - name: PlanScreenState
    fields: [weekSlots, lockState, lockSummary, nutritionCue, seasonContextLabel, loading, errorState?, fallbackState?, r05State?]
  - name: TodayScreenState
    fields: [dateLabel, todaySlots, nutritionCue, seasonContextLabel, loading, errorState?, fallbackState?, r05State?]
  - name: GroceryScreenState
    fields: [recipesLocked, lockSummary, canGenerate, hasList, hasUserEdits, isStale, staleWarningText?, items, storeSelection, availabilityState, exportState, loading, errorState?]
  - name: StoreSelectionState
    fields: [currentStore, locationPermission, suggestion?, lookupStatus, manualSelectionEnabled(always true), loading/error?]
  - name: RecipeDetailState
    fields: [recipeId, sourceLabel, seasonFitLabel, timeLabel, weekdayEligibilityLabel?, nutritionLabel/cues, ingredients, prepNotes, missingDataLabels, slotContext?]
  - name: AlternativesState
    fields: [slotContext, origin, rulesSummary, alternatives, loading, emptyReason?, errorState?]
  - name: UserSettingsState
    fields: [seasonOverrideEnabled, seasonSelection?, areaLabel?, validationMessage?, permissionState, privacySummary]
```

## 5. Domain integration points (no rule duplication)
- C2/U2 recipe/catalog: recipe details, alternatives, normalized metadata/labels (FR3–FR6 semantics from domain).
- C3/U2 planning: slot selection/replacement, lock state, Today/Plan shared state (R-06), stale triggers.
- C4/U3 shopping: `generate/regenerate` (explicit, confirmation gate from domain result/request), edit/check/add/remove, current edited export, `applyPresenceCheckBatch` projection display, exact ADR-006 stale warning constant.
- C5/A2 store/presence: suggestion/manual selection, batch presence name/presence only.
- C6/A3 export: official attempt outcome/fallback, share/copy ports; UI uses platform share/copy boundaries outside domain.

## 6. Key Compose implementation targets (planned for code generation)
- `app/src/main/java/com/mealplanner/ui/navigation/*` (routes, nav host, bottom bar)
- `app/src/main/java/com/mealplanner/ui/state/*` (UiState models, facades/mappers)
- `app/src/main/java/com/mealplanner/ui/screens/*` (onboarding/today/plan/grocery/user)
- `app/src/main/java/com/mealplanner/ui/screens/recipe/*` (detail, alternatives)
- `app/src/main/java/com/mealplanner/ui/screens/store/*` (PRISMA selector)
- `app/src/main/java/com/mealplanner/ui/components/*` (shared cards/banners/dialog/editor rows)
- `app/src/main/java/com/mealplanner/ui/accessibility/*` or localized helpers (labels/announcements as needed)
- `app/src/test/java/com/mealplanner/ui/*` (fake-backed state/facade/navigation rule tests where practical)

## 7. Non-functional UI notes
- Keep interactions responsive with coroutine/ViewModel state; no blocking network required for core local flows.
- Handle process/config changes through state holders/persistence-backed domain state.
- All fallback states provide next actions; no dead ends.
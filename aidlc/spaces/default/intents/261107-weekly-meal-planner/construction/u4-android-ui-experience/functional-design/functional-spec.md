# U4 Functional Design — Functional Specification (`u4-android-ui-experience`)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** Functional Design (construction, per-unit)  
**Date:** 2026-11-09  
**Conversation language:** English

## 1. Purpose and scope
U4 completes the Android presentation experience (RM-01–RM-09) and integrates U1–U3 domain/adapters through thin UI-facing contracts. UI orchestrates only; business rules remain in domain components (ADR-005). U4 must not duplicate R-03/FR3/FR9/ADR-006 rule logic; it invokes C2–C6/A1–A3/use-case contracts and renders typed states.

### In scope
- Material 3 Android UI (Jetpack Compose, already present in brownfield app) with bottom navigation: **Today**, **Plan**, **Grocery**, **User**.
- Full-screen flows: Replace meal / Alternatives (RM-05) and PRISMA store selection (RM-07), per Q3=B. Recipe detail (RM-04) is a pushed screen.
- Loading/empty/error/fallback/partial states for all primary screens, including RM-02/RM-03 first-launch/no-local-data handling (R-05).
- Synchronized Today/Plan (R-06): single shared slot-selection source of truth; unfilled slots remain empty.
- Grocery UX: lock/readiness, explicit generate/regenerate (R-03), regeneration confirmation when edits/check-offs exist, stale status on locked recipe changes (no auto-regeneration), edit/check/add/remove, availability projection display, export/share/copy fallback.
- Permissions/privacy UX (FR12/NFR3): location purpose, manual alternative, on-device MVP/no MealPlanner account messaging.
- Accessibility baseline WCAG 2.2 AA practical Android/mobile: text labels for status (not color-only), focus/announce behavior, touch targets, content descriptions.
- Responsive: portrait primary, landscape supported (scrollable, actions reachable, safe areas).

### Out of scope / deferred
- Backend service, cloud sync, MealPlanner account/auth.
- Medical nutrition advice or fabricated nutrition/season/presence data.
- Live Sydänmerkki/PRISMA/S-ostoslista production integrations beyond existing investigational adapter boundaries (OQ1–OQ4 remain open; UI shows fallback/investigational states).
- Tablet-specific two-pane layouts.

## 2. Upstream references
- [unit-of-work.md](../../../inception/units-generation/unit-of-work.md) (U4 responsibilities/notes)
- [unit-of-work-story-map.md](../../../inception/units-generation/unit-of-work-story-map.md) (U4 story/AC tracing)
- [stories.md](../../../inception/user-stories/stories.md) (US1.1–US5.6)
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (FR1–FR12, NFR1–NFR5)
- [components.md](../../../inception/domain-design/components.md) (PresentationUiComponent, ADR-005)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (C2–C6, A1–A3, ADR-006/R-01–R-03)
- [mockups.md](../../../inception/refined-mockups/mockups.md) (RM-01–RM-09)
- [interaction-spec.md](../../../inception/refined-mockups/interaction-spec.md)
- U1–U3 construction artifacts and implemented domain contracts in `app/src/main/java/com/mealplanner/`.

## 3. UI architecture (thin orchestration)
```yaml
ui_architecture:
  pattern: Compose + MVVM-style state holders orchestrating domain use cases/ports
  rule_ownership: domain (U1-U3) owns business rules; UI owns presentation state, navigation, user intents, accessibility/focus announcements
  state_source_of_truth:
    plan_slots: shared Planning state (U1/U2 C3 contracts) exposed to Today and Plan; no duplicated slot state
    shopping_list: ShoppingList aggregate/current edited state from C4/U3; UI never mutates merge/stale/presence rules
    store_selection: Preferences/C5 state; manual selection always available
  navigation:
    top_level: [Today, Plan, Grocery, User]
    pushed: [RecipeDetail]
    full_screen: [ReplaceAlternatives, SelectPrismaStore]
    dialogs_system: [Android share sheet/system settings as applicable, regenerate confirmation dialog]
  dependency_direction: UI -> use-case interfaces/facades -> domain/adapters; no Android framework types in domain (preserve BR3.9 seam)
```

## 4. UI-facing contracts / facades (new, thin)
U4 introduces presentation contracts in `app/src/main/java/com/mealplanner/ui/**` (or `ui/contracts`) that map domain results to renderable state. Implementations compose existing domain services; they must not reimplement domain rules.

| Contract | Purpose | Consumes | Produces |
|---|---|---|---|
| `PlanExperienceFacade` | Shared Today/Plan slot state, select/replace/lock intents | C3/U2 planning services, plan lock service, season context | `PlanScreenState`, `TodayScreenState`, slot context, lock status |
| `RecipeExperienceFacade` | Recipe detail, alternatives eligibility presentation, source/fallback labels | C2/U2 recipe/catalog services, alternative selector | `RecipeDetailState`, `AlternativesState` (domain-provided eligibility/explanations) |
| `GroceryExperienceFacade` | Lock/readiness, generate/regenerate/edit/check/add/remove/export intents | C4/U3 shopping service, export use case/seam | `GroceryScreenState`, confirmation requirement flag, stale status, export result |
| `StoreAvailabilityFacade` | Store suggestion/manual selection, presence check display | C5/A2 store selection/presence use cases, C4 projection | `StoreSelectionState`, availability display items (from ShoppingList projection) |
| `UserSettingsFacade` | Season override, permission/privacy status, system settings intent | preferences/season context, permission status provider | `UserSettingsState` |
| `DiagnosticsUiMapper` | Maps typed fallback/diagnostic outcomes to user-safe labels/actions | domain result/status types | status banners/snackbar/toast content; never raw secrets/PII/GPS |

## 5. Screen behaviors (RM mapping)

### RM-01 Onboarding / Permissions
- First run: value/privacy summary, location purpose card, **Allow location** / **Choose store manually later**, **Continue**.
- Manual-later always available (FR7/FR12/US5.1). Permission request uses Android system flow via platform permission boundary; UI shows granted/denied/not-asked states.
- Skip allowed only after privacy/data notice shown (per flow). Continue routes to Today/Plan.
- R-05/AC5.5.3: if initial remote data unavailable and no local data, show explicit status + **Retry** / **Use bundled fallback catalog** (if available) / **Continue with manual planning** where feasible; do not block feasible local actions.

### RM-02 Today’s meals
- Header shows current device day/date and season context indicator (device default or RM-09 override).
- Shows today’s slots by FR1 day type (weekday 2, weekend 3). Filled slots show title/time/nutrition cue/source and **Replace**/**View recipe**. Unfilled slots show `No recipe selected` + **Choose recipe** and remain empty (R-06).
- Nutrition planning aid (view-only): shows reliable cues or `Some nutrition details unavailable` / `Nutrition information unavailable`; never blocks; non-medical.
- Actions carry slot context and update shared state; selection reflected in Plan.
- States: populated/partial/empty/loading/error/fallback; R-05 no-local-data+remote-unavailable with next actions.

### RM-03 Plan (week view)
- 7-day sections with FR1 slot rules. Season context indicator reflects default/override (AC2.3.4).
- Weekly nutrition balance cue follows same availability rules as RM-02.
- Filled/unfilled slot rows/cards follow RM-02 semantics and R-06. **Replace**/**View recipe**/**Choose recipe** carry slot context.
- **Lock recipes for shopping list** control shows lock status (`Recipes locked`/not locked) and invokes domain lock action; does not auto-generate list (R-03).
- States include locked/unlocked status, missing labels (`Nutrition information unavailable`, `Season fit unknown`, time-unverifiable explanation), R-05 handling.

### RM-04 Recipe detail (pushed)
- Shows source/fallback label, season fit/`Season fit unknown` using current season context, total time and weekday `≤30 min` indicator where relevant, nutrition cues or `Nutrition information unavailable`, ingredients/prep notes with missing data labeled (no fabrication).
- Opened with slot context when reached from slot/alternatives: **Use for this meal slot** updates shared slot and returns to origin focused on updated slot/confirmation. **Choose different** opens RM-05 for same slot. No-slot-context case (not required MVP) hides selection action.
- Back-stack per R-06: RM-05→RM-04 back returns to RM-05 preserving slot context.

### RM-05 Replace meal / Alternatives (full screen)
- Slot context always present (`day`, `mealType`, `slotId`, current recipe or `null`, origin). Title distinguishes choose vs replace.
- Rules summary and alternatives are rendered from domain-provided eligibility/explanations (FR2–FR6, weekday ≤30 min, missing/ambiguous excluded/explained, season/nutrition/source cues). UI does not re-evaluate FR3 eligibility.
- **View** opens RM-04 with same slot context. **Select** updates shared slot only and returns to origin with focus/confirmation. **Cancel/Keep current/Leave empty** returns without change.
- Empty/error states provide explanation and next action.

### RM-06 Grocery
- Plan readiness card: `Recipes locked: [n]` or not locked + link to RM-03. **Generate shopping list** enabled only when domain reports ready context; **Regenerate** shown after list exists.
- R-03/ADR-006: generation only from locked snapshot by explicit action. If `hasUserEdits` and regenerate requested, show confirmation dialog: “Regenerate shopping list from locked recipes? Your current list edits/check-offs may be replaced or need review.” Actions **Cancel** (keep current) and **Regenerate and replace** (explicit). No silent loss/auto-regeneration.
- Stale status: if locked recipes changed after generation, show `Locked recipes changed — regenerate to update the shopping list.` and exact ADR-006 stale warning text when applicable: `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."` Do not auto-regenerate; keep current list.
- PRISMA store card: selected store, suggestion status, **Use suggested**, **Choose manually/Change**, location status/manage. Manual always available.
- Availability section: present/not found/`Unknown` + reason from C4 projection; name/presence only, never stock/guaranteed availability. Manual checklist fallback.
- Combined list: check/edit/add/remove; merge behavior is domain-owned (FR9). Edits apply to current generated list. Export uses current edited list (FR10).
- Export actions: S-ostoslista official attempt state/fallback per C6; **Copy text/share fallback** with instructions always available.

### RM-07 Select PRISMA store (full screen)
- Manual selection always available regardless of permission/suggestion outcome.
- Suggestion is best-effort; lookup failures show clear status and keep manual selection.
- Confirmed selection updates store context used by C5/C4 flows. Back preserves no-change behavior.

### RM-08 Share / Export
- Shows official S-ostoslista capability/investigation state from C6 result and mandatory fallback (Android share sheet / copy text) with instructions.
- Messages: official unavailable/failed uses fallback messaging; no blocking. Cancel preserves list.
- Uses current edited shopping list payload only.

### RM-09 User
- Season context default `Using device date and location`; override control (off by default) sets season and optional area/region label; invalid/incomplete handled with clear message and fallback to default until valid. **Reset to device default** restores default. Changes saved locally and affect season fit display contexts.
- Location permission status/purpose, manage in system settings. Privacy details: on-device MVP, minimal collection, no MealPlanner account.

## 6. Accessibility and responsiveness
- WCAG 2.2 AA practical baseline: sufficient contrast (Material 3 theme), visible labels, status never color-only, focus order, TalkBack/content descriptions, dialog focus management, announcements for important state changes.
- Touch targets and controls remain reachable in portrait and landscape.
- All primary actions have slot/list/store context labels where applicable (e.g., “Replace Monday dinner”, “Remove lentils”).

## 7. Error/fallback handling rules (UI)
- Map domain typed outcomes to user-safe messages/actions (retry, manual fallback, bundled fallback/manual planning, share/copy fallback). No dead-end states for core MVP flows (NFR2).
- Never display secrets, raw GPS coordinates, or fabricated data. Presence is name/presence only.
- Integration unavailable/denied/failed states are explicit and preserve local core actions.

## 8. Assumptions / open questions carried
- OQ1–OQ8 remain open; UI surfaces investigational/fallback states as specified.
- C5 live PRISMA and C6 official S-ostoslista may be unavailable; manual/checklist/share-copy always work.
- Android permission/system flows are platform-handled; UI provides purpose and fallback paths.

## 9. Completion evidence (stage outputs)
- `functional-spec.md` (this file)
- `frontend-components.md` (component/navigation/state catalog)
- `traceability.json` (U4 AC/FR/NFR/RM/R-06/R-05/ADR-005/ADR-006 mapping with test/implementation targets planned for U4 code generation)
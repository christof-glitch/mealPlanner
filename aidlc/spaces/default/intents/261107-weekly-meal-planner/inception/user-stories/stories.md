# User Stories

**Intent:** `261107-weekly-meal-planner` (Android MVP)  
**Primary persona:** [Household Meal Planner](personas.md)  
**Sources:** [requirements.md](../requirements-analysis/requirements.md) (FR1–FR12, NFR1–NFR5), [user-stories-questions.md](user-stories-questions.md) (Q1=A, Q2=B, Q3=B clarified by Q5=A, Q4=A)  
**Format:** `As a [persona], I want [goal], so that [benefit].`  
**IDs:** Stories `US{group}.{seq}`; acceptance criteria `AC{group}.{seq}.{criterion}`.

## Priority Legend
- **Must Have** — required for MVP acceptance/alignment with confirmed requirements.
- **Should Have** — valuable; can be reduced if needed without breaking confirmed requirement intent (where explicitly clarified).
- **Could Have / Won’t Have** — not assigned in this MVP set unless noted.

## Epic Overview (Workflow, Q4=A)

| Epic | Focus | Story IDs |
|---|---|---|
| US1 | Plan week | US1.1–US1.2 |
| US2 | Choose recipes | US2.1–US2.5 |
| US3 | Build grocery list | US3.1–US3.3 |
| US4 | Share/export | US4.1–US4.2 |
| US5 | Cross-cutting permissions/privacy/fallbacks | US5.1–US5.6 |

## Story Dependencies (high level)
- US1.1 supports US2.x and US3.1 (plan/slots provide context).
- US2.1/US2.2 feed selected recipes into US3.1.
- US3.1 feeds US3.2/US3.3 and US4.1/US4.2.
- US3.3 and US5.3 relate to store/availability; US5.2/US5.4/US5.5/US5.6 provide reliability/privacy behavior across flows.

---

## US1 — Plan Week

### US1.1 — View 7-day meal plan structure
**Priority:** Must Have  
**Trace:** FR1, NFR1, NFR2

As a Household Meal Planner, I want to view a 7-day meal plan with the correct meal slots for weekdays and weekends, so that I can plan my week clearly.

**Acceptance criteria**
- **AC1.1.1:** Given I open the plan view, when the 7-day plan is shown, then I see all 7 days with Saturday and Sunday containing exactly `Breakfast`, `Lunch`, and `Dinner` slots.
- **AC1.1.2:** Given I open the plan view, when I view Monday–Friday, then each day contains exactly `Breakfast` and `Dinner` slots and no `Lunch` slot.
- **AC1.1.3:** Given any meal slot, when I view it, then I can see whether a recipe is selected or the slot is empty/awaiting selection.
- **AC1.1.4:** Given the app is an Android MVP, when I use core plan viewing, then the experience is usable on supported Android devices without requiring other platforms.

**INVEST:** Independent plan-structure value; negotiable presentation; valuable for weekly planning; estimable; small; testable by slot counts/labels.

### US1.2 — Replace a selected recipe in a plan slot
**Priority:** Must Have  
**Trace:** FR2, NFR2

As a Household Meal Planner, I want to change or replace a recipe in a specific meal slot, so that the plan reflects recipes I would actually use.

**Acceptance criteria**
- **AC1.2.1:** Given a slot has a selected recipe, when I choose to change it, then I can view at least one alternative/replacement option or a clear empty/fallback state.
- **AC1.2.2:** Given I select a replacement recipe, when I confirm the selection, then the slot shows the newly selected recipe.
- **AC1.2.3:** Given I replace a recipe, when I later view the plan and shopping list, then downstream views use the current selected recipes (not stale selections).

**INVEST:** Independent of export method; valuable; estimable; small; testable.

---

## US2 — Choose Recipes

### US2.1 — Browse and view recipe details
**Priority:** Must Have  
**Trace:** FR2, FR3, FR5, FR6, NFR2

As a Household Meal Planner, I want to view recipe details before selecting a recipe, so that I can decide whether it fits my week.

**Acceptance criteria**
- **AC2.1.1:** Given I open a recipe, when details are available, then I see at minimum recipe name/title, ingredients, and preparation/cooking guidance.
- **AC2.1.2:** Given recipe data is partially missing, when I view details, then missing fields are labeled clearly (not fabricated) and the recipe remains viewable/selectable unless another rule explicitly excludes it.
- **AC2.1.3:** Given weekday eligibility, season, or nutrition information is used, when I view details, then I can see the relevant available data or a clear unavailable/unknown/explanation label.

**INVEST:** Vertical slice for recipe decision; testable with fixture data.

### US2.2 — Enforce weekday 30-minute total-time rule
**Priority:** Must Have  
**Trace:** FR3, NFR2

As a Household Meal Planner, I want weekday breakfast/dinner suggestions to respect a 30-minute total preparation/cooking time rule, so that weekday meals are realistic.

**Acceptance criteria**
- **AC2.2.1:** Given a recipe source provides total preparation/cooking time for a Monday–Friday `Breakfast` or `Dinner` slot, when that total time is ≤ 30 minutes, then the recipe is eligible for weekday suggestion.
- **AC2.2.2:** Given total preparation/cooking time is > 30 minutes for a Monday–Friday `Breakfast` or `Dinner` slot, when suggestions are shown, then the recipe is not suggested for that weekday slot.
- **AC2.2.3:** Given total preparation/cooking time is missing or ambiguous for a weekday slot, when suggestions/details are shown, then the recipe is excluded from weekday suggestions and the app shows an explanation (e.g., “Weekday prep time unavailable/unverifiable; excluded from weekday suggestions”).

**INVEST:** Clear pass/fail; independent rule; testable.

### US2.3 — See seasonal suggestions based on device date and location
**Priority:** Must Have  
**Trace:** FR5, FR12, NFR2, NFR3

As a Household Meal Planner, I want recipe suggestions to reflect season based on my device date and location, so that meal ideas feel timely.

**Acceptance criteria**
- **AC2.3.1:** Given device date and location signals are available/permitted as needed, when seasonal suggestions are determined, then the default season context is derived from device date and location.
- **AC2.3.2:** Given a recipe has season metadata/tags or curated mapping, when I view suggestions/details, then season fit is shown using that available data.
- **AC2.3.3:** Given season fit cannot be determined, when I view a recipe, then the app shows `Season fit unknown` (or equivalent clear label) and does not incorrectly claim a match.
- **AC2.3.4:** Given I need to adjust seasonal context (intent IN-05), when I use season controls/settings if provided, then I can override season/region context; if override UI is deferred, the limitation is explicit and default device-date/location behavior still works. *(Traceability note: addresses IN-05 and advisory review; implementation detail confirmed in design.)*

**INVEST:** Valuable; testable with mocked date/location/metadata.

### US2.4 — Use nutrition cues and a minimal planning aid
**Priority:** Must Have (minimal aid/cues/label per FR6, Q5=A); richer aids Should Have  
**Trace:** FR6, NFR2

As a Household Meal Planner, I want nutrition cues and a simple planning aid when choosing recipes, so that I can consider nutrition without being blocked by missing data.

**Acceptance criteria**
- **AC2.4.1:** Given nutrition data/cues are available for a recipe, when I view or select recipes, then nutrition-related information/cues are shown (e.g., calories/macros/balance tags or reliably available attributes).
- **AC2.4.2:** Given nutrition data is missing, when I view or select recipes, then the app clearly labels `Nutrition information unavailable` and allows me to continue.
- **AC2.4.3:** Given I am selecting/planning recipes, when I use nutrition-aware planning, then I have access to at least one minimal planning aid (e.g., a simple weekly balance cue or filter by a reliably available nutrition attribute).
- **AC2.4.4:** Given nutrition attributes are partial or unavailable, when the planning aid is shown, then it avoids false precision and labels/handles unavailable data safely.

**INVEST:** End-to-end user value; testable with partial fixture data.

### US2.5 — See Sydänmerkki-based recipes with clear source status
**Priority:** Must Have  
**Trace:** FR4, NFR2

As a Household Meal Planner, I want to browse/select Sydänmerkki-based recipes (or a clear fallback), so that I can use heart-symbol recipe suggestions in my plan.

**Acceptance criteria**
- **AC2.5.1:** Given the app attempts Sydänmerkki support, when recipe data is loaded, then the result is based on feasible/allowed Sydänmerkki data or a curated/imported Sydänmerkki-aligned fallback.
- **AC2.5.2:** Given live Sydänmerkki data is available, limited, unavailable, restricted, or fails, when I browse recipes, then I see a clear source/fallback status when relevant.
- **AC2.5.3:** Given I view/select a recipe, when Sydänmerkki-related fields are shown, then available heart-symbol/nutrition cues are presented clearly and missing data is labeled.

**INVEST:** Valuable; testable via adapter states.

---

## US3 — Build Grocery List

### US3.1 — Generate a combined shopping list from selected recipes
**Priority:** Must Have  
**Trace:** FR9, FR2, NFR2

As a Household Meal Planner, I want a combined shopping list from my selected recipes, so that I can shop for the week efficiently.

**Acceptance criteria**
- **AC3.1.1:** Given I have selected recipes in my plan, when I open the grocery/shopping list, then ingredients from all selected recipes are combined into one list.
- **AC3.1.2:** Given ingredient names and units/amounts are compatible, when the list is generated, then obvious duplicates are merged (MVP rule: merge when normalized ingredient name matches and units/amounts can be safely combined; otherwise keep separate).
- **AC3.1.3:** Given units/amounts cannot be safely merged, when the list is generated, then items remain as separate lines.
- **AC3.1.4:** Given some recipes have incomplete ingredient data, when the list is generated, then missing items are not silently fabricated and the UI indicates incomplete data where practical.

**INVEST:** Core value; testable merge/no-merge cases.

### US3.2 — Edit and check off shopping-list items before export/share
**Priority:** Must Have  
**Trace:** FR9, FR10, NFR2

As a Household Meal Planner, I want to edit and check off shopping-list items, so that the exported/shared list matches what I actually need.

**Acceptance criteria**
- **AC3.2.1:** Given a generated shopping list, when I review it, then I can check off items, edit item text/quantity, add items, and remove items.
- **AC3.2.2:** Given I have edited the list, when I export/share it, then the export uses the current edited list state.
- **AC3.2.3:** Given I change recipe selections after editing, when I regenerate or refresh the list behavior defined by the app, then the app handles changes predictably and avoids silently losing user edits without notice (behavior detail finalized in design).

**INVEST:** Valuable; testable.

### US3.3 — Select a PRISMA store and check ingredient name presence
**Priority:** Must Have  
**Trace:** FR7, FR8, FR12, NFR2, NFR3

As a Household Meal Planner, I want to select a PRISMA store and see best-effort ingredient name presence checks, so that I can prepare for shopping with realistic expectations.

**Acceptance criteria**
- **AC3.3.1:** Given I use grocery/store features, when I choose a store, then I can manually select a PRISMA store regardless of location permission.
- **AC3.3.2:** Given PRISMA ingredient data is available for the selected store, when presence is checked, then the app performs a best-effort ingredient **name/presence** check (not stock quantity or guaranteed availability).
- **AC3.3.3:** Given a name/presence result can be shown confidently by the implemented check, when I view an item, then I see a clear present/not-found status as supported; otherwise I see `Unknown` with a reason.
- **AC3.3.4:** Given automated checks are unavailable, inconclusive, or fail, when I view the list, then I can use a manual checklist fallback.

**INVEST:** Testable with mock store data/states.

---

## US4 — Share/Export

### US4.1 — Export/share shopping list with mandatory fallback
**Priority:** Must Have  
**Trace:** FR10, NFR2

As a Household Meal Planner, I want to export or share my shopping list, so that I can use it in S-ostoslista or another app.

**Acceptance criteria**
- **AC4.1.1:** Given I have a current shopping list (including edits), when I choose export/share, then I can use a working share sheet and/or copyable text fallback.
- **AC4.1.2:** Given the fallback export is shown, when I read instructions, then I receive clear guidance for using the export with S-ostoslista (and I am expected to have an S-ostoslista account).
- **AC4.1.3:** Given the official S-ostoslista-compatible method is unavailable or not confirmed feasible, when I export/share, then the fallback still works and does not block shopping-list use.

**INVEST:** Critical MVP path; testable.

### US4.2 — Use official S-ostoslista method when feasible
**Priority:** Must Have (attempt/investigate and use if feasible); fallback remains mandatory  
**Trace:** FR10

As a Household Meal Planner, I want the app to use an official S-ostoslista-compatible import/share method when feasible, so that moving my list is as smooth as possible.

**Acceptance criteria**
- **AC4.2.1:** Given an official S-ostoslista-compatible method is confirmed feasible during implementation, when I export/share, then the app offers or uses that method per implementation/design.
- **AC4.2.2:** Given the official method is partially feasible, unavailable, or fails, when I export/share, then the app falls back to the working share sheet/copyable text export with instructions.
- **AC4.2.3:** Given any export outcome, when I complete or cancel export/share, then I return to the shopping list without losing the current list state.

**INVEST:** Valuable; testable by capability flags/mock handlers.

---

## US5 — Cross-Cutting Permissions, Privacy, and Fallbacks (Q2=B)

### US5.1 — Control location permission with clear purpose
**Priority:** Must Have  
**Trace:** FR7, FR12, NFR2, NFR3

As a Household Meal Planner, I want to understand and control location permission, so that I can choose privacy while still using the app.

**Acceptance criteria**
- **AC5.1.1:** Given nearest-PRISMA suggestion needs location, when the app requests permission, then it includes clear purpose text.
- **AC5.1.2:** Given I deny or revoke location permission, when I use store features, then I can manually select a PRISMA store and continue.
- **AC5.1.3:** Given MVP scope, when I use the app, then no MealPlanner account is required and only necessary data is collected.

**INVEST:** Testable permission flows.

### US5.2 — Get nearest PRISMA suggestion when permission granted (best effort)
**Priority:** Must Have (per FR7/Q5=A clarification)  
**Trace:** FR7, FR12, NFR2

As a Household Meal Planner, I want the nearest PRISMA store suggested when I allow location, so that I can choose a store faster.

**Acceptance criteria**
- **AC5.2.1:** Given location permission is granted and store-location data is available, when I open store selection, then the app suggests a nearest PRISMA store (best effort).
- **AC5.2.2:** Given nearest-store lookup fails or data is unavailable, when I open store selection, then I see a clear status and can manually select a store.
- **AC5.2.3:** Given a store is selected (suggested or manual), when I view grocery/availability context, then the selected store is visible.

**INVEST:** Convenience with fallback; testable.

### US5.3 — Handle PRISMA availability unknown/fallback states
**Priority:** Must Have  
**Trace:** FR8, FR11, NFR2

As a Household Meal Planner, I want clear availability fallback behavior when PRISMA checks are limited, so that I can still complete my shopping plan.

**Acceptance criteria**
- **AC5.3.1:** Given a presence check cannot produce a definitive result, when I view an ingredient, then I see `Unknown` with a reason (e.g., data unavailable/not checked/name match inconclusive).
- **AC5.3.2:** Given PRISMA data/integration fails, when I view grocery features, then the app shows a clear status and offers a manual checklist fallback without blocking list editing/export.
- **AC5.3.3:** Given fallback/checklist mode is used, when I continue planning, then core shopping-list tasks remain usable.

**INVEST:** Separate reliability story (Q2=B); testable.

### US5.4 — Handle Sydänmerkki unavailable with curated fallback
**Priority:** Must Have  
**Trace:** FR4, FR11, NFR2

As a Household Meal Planner, I want the app to keep recipe browsing usable when Sydänmerkki is unavailable, so that I can still plan my week.

**Acceptance criteria**
- **AC5.4.1:** Given live Sydänmerkki data is unavailable, restricted, or fails, when I browse recipes, then I can use a curated/imported Sydänmerkki-aligned fallback catalog.
- **AC5.4.2:** Given fallback is active, when I view recipe/source context, then I see a clear status indicating fallback use.
- **AC5.4.3:** Given fallback recipes lack some fields, when I view/select them, then missing data is labeled and core planning remains possible.

**INVEST:** Separate reliability story; testable.

### US5.5 — Use local/previously available data and handle first launch with no local data
**Priority:** Must Have  
**Trace:** FR11, NFR2, NFR5

As a Household Meal Planner, I want the app to work with local data when possible and behave clearly on first launch, so that I’m not blocked by temporary connectivity or missing state.

**Acceptance criteria**
- **AC5.5.1:** Given previously available/local data exists, when integrations/network are unavailable, then core planning and shopping-list features remain usable where possible.
- **AC5.5.2:** Given an integration fails, when I use the app, then I see a clear status and the failure does not block unrelated core actions.
- **AC5.5.3:** Given first launch/no previously available local data and required remote data is unavailable, when I open the app, then I see a clear loading/empty/error state and at least one next action (retry, use bundled fallback catalog if present, or continue with manual planning where applicable).

**INVEST:** Addresses advisory review minor; testable.

### US5.6 — Keep data on-device for MVP with sync-ready handling
**Priority:** Must Have  
**Trace:** FR12, NFR3

As a Household Meal Planner, I want my MVP data handled on-device and minimally, so that my privacy is respected.

**Acceptance criteria**
- **AC5.6.1:** Given MVP operation, when data is saved, then plan/list/preferences needed for MVP are stored locally/on-device by default.
- **AC5.6.2:** Given future sync may be added later, when MVP architecture/design decisions are made, then they do not require a MealPlanner account in MVP and do not prevent later sync capability.
- **AC5.6.3:** Given location or integration data is used, when the app processes it, then only necessary data is used for the selected feature and purpose/consent is clear where applicable.

**INVEST:** Privacy-focused; testable through behavior/config/review.

---

## INVEST Compliance Notes
- Stories are user-centered, vertically sliced by workflow, and include observable outcomes.
- Integration/fallback behavior is represented as separate reliability stories (Q2=B) plus concrete acceptance criteria where needed.
- Priorities reflect confirmed requirements (Q5=A), with polish/richer aids as non-blocking enhancements.
- Some implementation details (exact official S-ostoslista method, PRISMA data source, season mapping catalog, nutrition attribute set) remain open and are captured in requirements open questions; acceptance criteria use testable states/fallbacks.
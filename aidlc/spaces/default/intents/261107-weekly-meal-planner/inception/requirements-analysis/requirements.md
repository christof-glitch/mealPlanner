# Requirements — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Stage:** Requirements Analysis (inception)  
**Date:** 2026-11-07  
**Conversation language:** English

## Sources

- [Initial description](../../project-description.json): `Build a mobile application that gives me meal suggestions for a week.` [desc]
- [Workflow-selected scope](../../ideation/scope-definition/scope-document.md): `mvp` [scope]
- [Intent statement](../../ideation/intent-capture/intent-statement.md)
- [Feasibility assessment](../../ideation/feasibility/feasibility-assessment.md)
- [Constraint register](../../ideation/feasibility/constraint-register.md)
- [RAID log](../../ideation/feasibility/raid-log.md)
- [Scope document](../../ideation/scope-definition/scope-document.md)
- [Intent backlog](../../ideation/scope-definition/intent-backlog.md)
- [Rough mockups](../../ideation/rough-mockups/wireframes.md), [user flow](../../ideation/rough-mockups/user-flow.md)
- [Requirements Analysis questions/answers](requirements-analysis-questions.md) [Q1–Q7, summary confirmed `Looks correct`]

## Intent Analysis

### Problem and users
- Household/family members need help planning a week of meals with better variety and inspiration, while considering budget, nutrition, and dietary restrictions. [intent Q1]
- MVP success means a user can view a useful 7-day suggestion plan and select recipes they would actually use. [intent Q3]

### MVP outcome
- Android-only mobile application for a 7-day meal plan with selectable recipes, seasonal suggestions, PRISMA store/ingredient support, and a combined shopping list shareable/importable to S-ostoslista, with investigate-and-fallback behavior for uncertain external integrations. [intent Q2,Q8,Q11–Q13,Q15–Q17; scope IN-01–IN-12]

## Functional Requirements

### FR1 — 7-day meal plan structure
**The system shall** present a 7-day meal plan where each weekend day (Saturday and Sunday) contains exactly 3 meal slots labeled `Breakfast`, `Lunch`, and `Dinner`, and each weekday (Monday–Friday) contains exactly 2 meal slots labeled `Breakfast` and `Dinner`.  
**Acceptance criteria:**
- A user can view all 7 days and the required meal slots for each day.
- No weekday includes a `Lunch` slot in the MVP plan structure.
- The plan view allows the user to see which recipe (if any) is selected for each slot.

### FR2 — Recipe suggestion, viewing, and selection
**The system shall** provide recipe suggestions for plan slots and allow the user to view recipe details and select recipes they would use for the plan.  
**Acceptance criteria:**
- For each plan slot, the user can view at least one suggested recipe or a clear empty/fallback state.
- Recipe details show at minimum: recipe name/title, ingredients (when available), preparation/cooking guidance (when available), and the data used for weekday time/season/nutrition decisions when available.
- The user can select a recipe for a slot and replace/change that selection.
- The user can view individual slot alternatives/replacement options where the UI supports it (per rough mockups R-01).

### FR3 — Weekday preparation time rule
**The system shall** enforce the weekday preparation-time rule using the **total preparation/cooking time as provided by the recipe source** for Monday–Friday `Breakfast` and `Dinner` suggestions. [Q2=A]  
**Acceptance criteria:**
- A recipe is eligible for weekday suggestion only when its total preparation/cooking time is present, interpretable, and ≤ 30 minutes.
- If weekday prep time is missing or ambiguous, the recipe is excluded from weekday suggestions.
- When a recipe is excluded or cannot be verified for weekday time, the app shows an explanation in recipe details/fallback behavior (e.g., “Weekday prep time unavailable/unverifiable; excluded from weekday suggestions”).

### FR4 — Sydänmerkki recipe integration and fallback
**The system shall** attempt Sydänmerkki-based recipe support for MVP by investigating and, where feasible/allowed, querying `www.sydanmerkki.fi`; if live querying is not feasible/allowed or fails, the system shall use a curated/imported Sydänmerkki-aligned recipe fallback. [intent Q2,Q11,Q14,Q15; scope IN-04]  
**Acceptance criteria:**
- The implementation path includes an explicit Sydänmerkki integration attempt/investigation outcome (feasible+used, feasible+limited, or not feasible for MVP).
- When live Sydänmerkki data is unavailable, restricted, or fails, users can still browse/select recipes from the fallback catalog.
- The UI shows a clear recipe-source/fallback status when relevant (e.g., live Sydänmerkki unavailable; using curated/imported recipes).

### FR5 — Seasonal recipe selection
**The system shall** determine seasonal recipe suggestions based on **device date and location**. [Q3=X]  
**Acceptance criteria:**
- The default season context is derived from device date and location.
- Recipe season fit uses available recipe metadata/tags or a curated mapping.
- If season fit cannot be determined for a recipe, the app shows `Season fit unknown` (or equivalent clear label) rather than incorrectly claiming a match.
- **Assumption (pending confirmation):** user override of season/region remains available as in intent IN-05 unless later specified otherwise.

### FR6 — Nutrition-aware planning
**The system shall** actively reflect nutrition-aware planning in MVP by showing nutrition-related recipe information/cues when available, clearly labeling `Nutrition information unavailable` when missing, and providing at least one nutrition-aware planning aid during recipe selection. [Q1=B; scope IN-06]  
**Acceptance criteria:**
- When nutrition data/cues are available (e.g., calories/macros/balance tags, Sydänmerkki nutrition fields, or other reliably available attributes), they are shown in recipe/selection context.
- When nutrition data is missing, the app clearly labels `Nutrition information unavailable` and does not block recipe viewing or selection.
- During selection/planning, the user has access to at least one nutrition-aware aid (e.g., a simple weekly balance cue or a filter by a reliably available nutrition attribute).
- The aid behaves safely with partial data: missing attributes do not create false precision and are labeled/handled as unavailable.

### FR7 — PRISMA store selection
**The system shall** support selecting a PRISMA store for shopping/availability context, using device location to suggest the nearest PRISMA store when location permission is granted, and always allowing manual store selection. [intent Q10,Q12; scope IN-07]  
**Acceptance criteria:**
- With location permission granted, the app can suggest a nearest PRISMA store (best effort, based on available store-location data).
- Without location permission or when nearest-store lookup fails, the user can manually select a PRISMA store.
- The selected store is visible in the grocery/availability context.

### FR8 — PRISMA ingredient presence check
**The system shall** perform a best-effort PRISMA ingredient check by testing whether the **ingredient’s name exists in the selected PRISMA store** when data is available; otherwise it shall show `Unknown` with a reason and provide a manual checklist fallback. [Q4=X; scope IN-08]  
**Acceptance criteria:**
- For each shopping-list ingredient checked against a selected store, the app shows `Available`/present, `Not available`/not found, or `Unknown` only as supported by the implemented best-effort name/presence match and data confidence; when a definitive result cannot be produced, it shows `Unknown` with reason (e.g., data unavailable, store data not checked, name match inconclusive).
- **Assumption (confirmed intent):** the check is a best-effort ingredient name/presence check, **not** stock quantity or guaranteed in-store availability.
- The user can use a manual checklist fallback regardless of automated check outcome.

### FR9 — Combined shopping list creation and editing
**The system shall** generate a combined shopping list from selected weekly recipes and allow users to review/edit it before export/share. [Q5=A; scope IN-09]  
**Acceptance criteria:**
- Ingredients from all selected recipes are combined into one shopping list.
- Obvious duplicates are merged by ingredient name when units/amounts are compatible.
- Items are kept as separate lines when units/amounts cannot be safely merged.
- Before export/share, the user can check off items, edit item text/quantity, add items, and remove items.
- The list remains usable when some recipes have incomplete ingredient data (missing items are not silently fabricated; the UI indicates incomplete data where practical).

### FR10 — S-ostoslista support and export fallback
**The system shall** investigate and implement an official S-ostoslista-compatible import/share method if confirmed feasible during implementation; regardless of outcome, the MVP shall provide a working fallback export (share sheet and/or copyable text) with clear instructions. [Q6=A; scope IN-10]  
**Acceptance criteria:**
- The implementation records the S-ostoslista investigation outcome (official method used, partially used, or not feasible/confirmed).
- The MVP always provides a working share-sheet and/or copyable text export for the shopping list.
- Fallback export includes clear instructions that the user can use with S-ostoslista (user expected to have an S-ostoslista account).
- Export uses the user’s current edited shopping-list state.

### FR11 — Reliability and fallback behavior
**The system shall** keep core planning and shopping-list features usable when integrations, location, or network data are unavailable, using defined fallbacks and clear status messaging without blocking the whole app. [Q7=A]  
**Acceptance criteria:**
- Core planning and shopping-list features work on-device with previously available/local data where possible.
- Location is optional; PRISMA store can be selected manually when location is unavailable/denied.
- Sydänmerkki failure/unavailability uses curated/imported recipe fallback.
- PRISMA availability failure/unavailability uses `Unknown`/manual checklist fallback.
- S-ostoslista official-method failure/unavailability uses share-sheet/copyable text fallback.
- Each integration/fallback state shows a clear user-visible status and does not block unrelated core actions.

### FR12 — Privacy, consent, and data minimization
**The system shall** collect only data necessary for MVP features, request location consent with purpose text, and keep MVP user data on-device by default with a design that is sync-ready later. [intent Q5,Q6,Q9,Q12; scope IN-11, IN-12]  
**Acceptance criteria:**
- Location permission is requested only when needed for nearest-PRISMA suggestion (or equivalent location-based feature) and includes purpose text.
- The app functions with location denied through manual store selection.
- MVP does not require a MealPlanner account.
- Personal data handling is minimized to what is necessary for selected MVP features; no unnecessary personal data is collected.
- Data persistence is local/on-device for MVP; architecture/design choices do not prevent later sync capability.

## Non-Functional Requirements

### NFR1 — Platform scope
**The system shall** target **Android only** for the first version.  
**Measure:** Release/build artifacts and supported install targets are Android; iOS/web are not required for MVP acceptance.

### NFR2 — Usability and clarity of fallback states
**The system shall** present integration, permission, missing-data, and fallback states in clear user-facing language.  
**Measure:** For each defined fallback case (missing nutrition, missing/ambiguous weekday time, unknown season fit, PRISMA `Unknown`, Sydänmerkki fallback, S-ostoslista fallback, location denied), the UI provides a visible status/label and a next action that does not dead-end the user.

### NFR3 — Privacy and GDPR-aware handling
**The system shall** apply GDPR-aware minimal data handling for MVP data flows.  
**Measure:** Location consent includes purpose text; no account is required; only necessary data is collected/stored; local data can be explained/inspected at a high level in implementation artifacts; no secrets/personal data are hardcoded.

### NFR4 — Testability and quality gates
**The system shall** be delivered with testable behavior for key MVP flows and quality checks in CI as the codebase/pipeline is established. [team testing posture]  
**Measure:** Key behaviors (plan structure/time rule, recipe selection, shopping-list aggregation/editing, integration adapters/fallbacks, privacy-sensitive permission handling) have automated tests where practical; critical tests/CI checks run; existing tests remain green; quality targets are not weakened to pass.

### NFR5 — Performance and responsiveness (MVP baseline)
**The system shall** remain responsive for normal MVP planning tasks on supported Android devices.  
**Measure:** Plan viewing, recipe selection, shopping-list edits, and export/share actions complete without requiring the user to force-close the app; long-running integration checks show progress/status and do not block unrelated local interactions.

## Constraints

- Android-only MVP. [intent Q8]
- Sydänmerkki integration must be attempted; fallback required. [scope Q2, IN-04]
- PRISMA availability automation is best-effort and may be `Unknown`/manual checklist. [intent Q15,Q16; Q4=X]
- Official S-ostoslista method is investigational; working export fallback is mandatory. [intent Q17; Q6=A]
- MVP data on-device, sync-ready later; no MealPlanner account in MVP. [intent Q5,Q6; scope IN-11,IN-12]
- Location permission is optional and purpose-limited. [intent Q9,Q12; NFR3]
- Stack undecided; recommendation requested with backend/service expected (technical design decision, not a product requirement to expose).

## Assumptions

- A user can override season/region context (intent IN-05) even though Q3 specified device date and location as the basis; if not desired, this can be removed in a follow-up change request.
- “Device date and location” for season may use available platform signals (date/time, locale, approximate/precise location depending on permission and implementation choice) with clear consent/fallback behavior.
- PRISMA ingredient name/presence matching may require normalization (case/spacing/basic aliases) but does not guarantee stock quantity or real-time shelf availability.
- Curated/imported recipe fallback data can provide enough fields (title, ingredients, prep time, optional season/nutrition cues) to exercise MVP flows.
- Finland is the initial regional context unless device/location indicates otherwise; detailed produce calendars/monthly mappings are out of MVP unless later specified.

## Out of Scope (MVP)

- iOS/web versions.
- Grocery delivery integration.
- Store-specific pricing.
- Full meal-prep scheduling workflows.
- In-app cooking mode/timers.
- MealPlanner account/sync in MVP (design sync-ready only).
- Exact macronutrient targets/medical nutrition plans unless later specified.
- Stock quantity guarantees or real-time shelf availability guarantees for PRISMA.
- Full-week regeneration UX (deferred per rough mockups R-02).
- Budget and dietary-restriction filtering as committed MVP scope unless later specified.

## Open Questions

- **OQ1 (Sydänmerkki):** What access method, terms, and data fields are feasible/allowed for MVP (live query vs curated/import)? [RAID I-01,D-01]
- **OQ2 (PRISMA location):** What reliable PRISMA store-location data/source and nearest-store method will be used? [RAID I-02,D-02]
- **OQ3 (PRISMA availability):** What PRISMA ingredient/presence data source is available, and what name-matching quality is acceptable? [RAID I-03,D-03]
- **OQ4 (S-ostoslista):** Is there an official, feasible import/share/deep-link/API method and required format? [RAID I-04,D-04]
- **OQ5 (Season mapping):** What curated mapping/tags define season fit in Finland (and how location granularity affects it)?
- **OQ6 (Nutrition data):** Which nutrition attributes are reliably available across Sydänmerkki/fallback data for the required planning aid?
- **OQ7 (Prep-time data quality):** How will inconsistent/missing recipe time formats be normalized before applying the ≤ 30 minute weekday rule?
- **OQ8 (Stakeholder cadence):** Who are secondary stakeholders and what review/decision cadence applies? [intent open question]

## Corner-Case Coverage Notes

- **Missing/ambiguous data:** weekday prep time, nutrition, season fit, ingredient availability, and incomplete ingredients have explicit labels/fallbacks and do not silently produce incorrect eligibility/availability claims.
- **Permission denied/unavailable:** location denial keeps PRISMA selection usable via manual selection.
- **Integration failure/unavailable:** each external integration has a defined fallback and clear status; core local planning/list editing remains usable.
- **Partial local data:** on-device previously available/local data is used where possible; first-load behavior depends on implementation but must not leave users without a clear state/action.
- **Unsafe merges:** shopping-list duplicates are merged only when units/amounts are compatible; otherwise kept separate.
- **Export state:** S-ostoslista/share export uses the current edited list, not a stale pre-edit copy.
- **Privacy:** no account required; location purpose-limited; minimal data collection. Multi-user/tenant, medical, and payment-data corners are out of MVP scope.

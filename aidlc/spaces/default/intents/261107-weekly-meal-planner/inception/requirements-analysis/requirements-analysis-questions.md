# Requirements Analysis Questions

**Intent:** `261107-weekly-meal-planner` (Android MVP, `mvp` scope, space `default`)  
**Sources reviewed:** [intent-statement](../../ideation/intent-capture/intent-statement.md), [scope-document](../../ideation/scope-definition/scope-document.md), [team-practices](../practices-discovery/team-practices.md), feasibility/RAID context carried in scope.  
**Purpose:** Resolve the remaining requirement gaps before generating `requirements.md` and `traceability.json`. Each question includes context and does not re-ask decisions already confirmed in prior stages.

Please answer by filling in each `[Answer]:` tag (or answer in chat and I’ll write the answers back).

---

## Q1. [Product] How should “nutrition-aware planning” be reflected in the MVP?

**Context:** The MVP must actively reflect planning around nutrition (scope IN-06), but the exact behavior is not yet testable. Recipe sources/fallbacks may not always provide structured nutrition fields, so the requirement must define what the app shows and how it behaves when nutrition data is missing.

**Question:** Which MVP behavior should be required for nutrition-aware planning?

A. Show nutrition-related recipe information/cues when available (for example calories/macros/balance tags or Sydänmerkki nutrition fields) and clearly label “Nutrition information unavailable” when missing; do not block recipe selection.
B. Same as A, plus require at least one nutrition-aware planning aid during selection (for example a simple weekly balance cue or filter by a nutrition attribute the data can reliably provide).
C. Keep nutrition awareness lightweight for MVP: show only high-level nutrition cues when available and allow missing data with no extra planning aid.
X. Other (please specify)

[Answer]: B — Show nutrition-related recipe information/cues when available and clearly label “Nutrition information unavailable” when missing; do not block recipe selection, plus require at least one nutrition-aware planning aid during selection (for example a simple weekly balance cue or filter by a nutrition attribute the data can reliably provide). 

---

## Q2. [Product] What is the rule for weekday recipe preparation time (30 minutes or less)?

**Context:** Weekday recipes (Monday–Friday Breakfast and Dinner) must take no more than 30 minutes to prepare (intent/scope IN-03), but the time basis and missing/ambiguous data handling are open. This affects which recipes are eligible and how the app explains exclusions.

**Question:** How should the app define and enforce the 30-minute weekday prep-time rule?

A. Use total preparation/cooking time as provided by the recipe source; include a recipe only when that value is 30 minutes or less. If time is missing/ambiguous, exclude it from weekday suggestions and show an explanation in recipe details/fallback behavior.
B. Use active prep time only (not passive cooking/waiting) when the source distinguishes it; otherwise treat total time as the value. Missing/ambiguous time follows the same exclude-and-explain rule.
C. Allow recipes with missing/ambiguous time into weekday suggestions only if the user explicitly confirms them, and always show that the time could not be verified.
X. Other (please specify)

[Answer]: A — Use total preparation/cooking time as provided by the recipe source; include a recipe only when that value is 30 minutes or less. If time is missing/ambiguous, exclude it from weekday suggestions and show an explanation in recipe details/fallback behavior. 

---

## Q3. [Product] How specific should seasonal recipe selection be for the MVP?

**Context:** Recipes should be seasonal based on current date/season in the user’s region, with a default from date/region and user override (IN-05). The detailed regional/season mapping rule is open.

**Question:** What seasonal behavior should the MVP require?

A. Default to Finland’s current calendar season (based on device date/locale or Finland default) and let the user override the season; recipe season fit uses available recipe metadata/tags or a curated mapping, with “season fit unknown” allowed when data is missing.
B. Ask the user to confirm region/season during onboarding or settings (default preselected from device date/locale) and use that selection for suggestions; same unknown-data handling as A.
C. Use only broad season categories (winter/spring/summer/autumn) with manual override; do not attempt finer regional/monthly produce rules in MVP.
X. Other (please specify)

[Answer]: X — Define season based on device date and location. (Assumption to confirm at summary: user override remains available unless specified otherwise; season fit uses available recipe metadata/tags or curated mapping, with “season fit unknown” allowed when data is missing.) 

---

## Q4. [Operations/UX] How should PRISMA ingredient availability be presented when automation is limited?

**Context:** The app should verify ingredient availability against the selected PRISMA store using available PRISMA data (IN-08), but access/feasibility is uncertain and fallbacks are allowed (best-effort/unknown indicators or checklist). The requirement must define user-visible states.

**Question:** Which availability presentation should the MVP require?

A. For each shopping-list ingredient, show a clear status per selected store: `Available`, `Not available`, or `Unknown` (with reason such as data unavailable/not checked). Provide a manual checklist fallback when automated checks are unavailable.
B. Show availability only as best-effort hints (available/unavailable when confidently known) and otherwise default to `Unknown` without blocking planning; include a manual checklist.
C. Do not promise per-ingredient automated verification in MVP; generate the shopping list and provide a PRISMA-oriented checklist/manual verification flow, with store selection still supported.
X. Other (please specify)

[Answer]: X — Check that the ingredient’s name exists in the selected PRISMA store (name/presence match) when data is available; otherwise show `Unknown` with reason and provide a manual checklist fallback. (Assumption to confirm: this is a best-effort presence check, not stock quantity/guaranteed availability.) 

---

## Q5. [Product] How should the combined shopping list be built and edited?

**Context:** The MVP must include a combined shopping list from selected weekly recipes/plan (IN-09). The current scope does not specify aggregation rules (duplicate ingredients, quantities/units, user edits, pantry items), which affects correctness and usability.

**Question:** What shopping-list behavior should be required for MVP?

A. Combine ingredients from all selected recipes into one list, merge obvious duplicates by ingredient name when units are compatible, keep separate lines when units/amounts cannot be safely merged, and allow users to check off, edit text/quantity, add items, and remove items before export/share.
B. Same core editing/checklist capabilities as A, but keep recipe ingredients as separate grouped entries (no automatic merging) to avoid incorrect unit conversions in MVP.
C. Generate a simple read-only combined list from selected recipes plus a manual checklist; defer editing/merging to a later version.
X. Other (please specify)

[Answer]: A — Combine ingredients from all selected recipes into one list, merge obvious duplicates by ingredient name when units are compatible, keep separate lines when units/amounts cannot be safely merged, and allow users to check off, edit text/quantity, add items, and remove items before export/share.

---

## Q6. [Integration/Product] What is the MVP acceptance target for S-ostoslista support?

**Context:** The app must support sharing/importing the shopping list to S-ostoslista using an official method if one exists (format to be confirmed), with a share-sheet/text export fallback (IN-10). The user is expected to have an S-ostoslista account. The exact method is still an investigation item.

**Question:** What should “S-ostoslista support” mean for MVP acceptance?

A. Investigate and implement an official S-ostoslista-compatible import/share method if confirmed feasible during implementation; regardless of outcome, MVP must provide a working fallback export (share sheet and/or copyable text) with clear instructions.
B. Prioritize a reliable share-sheet/copyable text export that users can use with S-ostoslista; treat official direct integration as best-effort only if it is trivial and confirmed.
C. Require an official S-ostoslista import/share method for MVP acceptance; if no official method is confirmed, block the S-ostoslista feature and ship only a generic shopping list.
X. Other (please specify)

[Answer]: A — Investigate and implement an official S-ostoslista-compatible import/share method if confirmed feasible during implementation; regardless of outcome, MVP must provide a working fallback export (share sheet and/or copyable text) with clear instructions. User is expected to have an S-ostoslista account.

---

## Q7. [Reliability/Privacy] How should the app behave when integrations, location, or network data are unavailable?

**Context:** External integrations (Sydänmerkki, PRISMA availability/store data, S-ostoslista method) and location permission are uncertain or optional (intent assumptions, RAID R-01–R-05). MVP data is on-device and GDPR/minimal data handling is required (IN-11, IN-12). Requirements need explicit fallback/error behavior.

**Question:** Which reliability/privacy behavior should the MVP require?

A. Core planning and shopping-list features must work on-device with previously available/local data where possible; location is optional with manual PRISMA store selection; each integration failure shows a clear status and uses its defined fallback (curated/imported recipes, unknown/checklist availability, export/share fallback) without blocking the whole app. Collect only necessary data and request location consent with purpose text.
B. Same as A, but allow online access to be required for first-time recipe catalog loading/Sydänmerkki results; after data is available, planning/shopping list should remain usable.
C. Require live connectivity for recipe, store, and availability features in MVP; show errors when unavailable (fallbacks limited to manual text export).
X. Other (please specify)

[Answer]: A — Core planning and shopping-list features must work on-device with previously available/local data where possible; location is optional with manual PRISMA store selection; each integration failure shows a clear status and uses its defined fallback (curated/imported recipes, unknown/checklist availability, export/share fallback) without blocking the whole app. Collect only necessary data and request location consent with purpose text.

---

## Consolidated Summary Confirmation

- **Q1 [Product] Nutrition-aware planning:** B — Show nutrition-related recipe information/cues when available and clearly label “Nutrition information unavailable” when missing; do not block recipe selection, plus require at least one nutrition-aware planning aid during selection (e.g., a simple weekly balance cue or filter by a nutrition attribute the data can reliably provide).
- **Q2 [Product] Weekday prep-time (≤ 30 minutes, Mon–Fri Breakfast/Dinner):** A — Use total preparation/cooking time as provided by the recipe source; include a recipe only when that value is ≤ 30 minutes. If time is missing/ambiguous, exclude it from weekday suggestions and show an explanation in recipe details/fallback behavior.
- **Q3 [Product] Seasonal selection:** X — Define season based on device date and location. **Assumption to confirm:** user override remains available (as in intent IN-05) unless you specify otherwise; season fit uses available recipe metadata/tags or a curated mapping, with “season fit unknown” allowed when data is missing.
- **Q4 [Operations/UX] PRISMA availability presentation:** X — Check that the ingredient’s name exists in the selected PRISMA store (name/presence match) when data is available; otherwise show `Unknown` with reason and provide a manual checklist fallback. **Assumption to confirm:** this is a best-effort presence/name check, not stock quantity or guaranteed in-store availability.
- **Q5 [Product] Shopping list build/edit:** A — Combine ingredients from all selected recipes into one list, merge obvious duplicates by ingredient name when units are compatible, keep separate lines when units/amounts cannot be safely merged, and allow users to check off, edit text/quantity, add items, and remove items before export/share.
- **Q6 [Integration/Product] S-ostoslista MVP acceptance:** A — Investigate and implement an official S-ostoslista-compatible import/share method if confirmed feasible during implementation; regardless of outcome, MVP must provide a working fallback export (share sheet and/or copyable text) with clear instructions. User is expected to have an S-ostoslista account.
- **Q7 [Reliability/Privacy] Unavailable integrations/location/network:** A — Core planning and shopping-list features must work on-device with previously available/local data where possible; location is optional with manual PRISMA store selection; each integration failure shows a clear status and uses its defined fallback (curated/imported recipes, unknown/checklist availability, export/share fallback) without blocking the whole app. Collect only necessary data and request location consent with purpose text.

Does this all look correct before I generate the requirements artifact?
- Looks correct
- Request changes

[Answer]: Looks correct 

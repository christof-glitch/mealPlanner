# Scope Document — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Sources:** [intent-statement](../intent-capture/intent-statement.md), [feasibility-assessment](../feasibility/feasibility-assessment.md), [constraint-register](../feasibility/constraint-register.md), [raid-log](../feasibility/raid-log.md), [scope-definition-questions](scope-definition-questions.md) [Q1–Q5]

## Scope Summary
The MVP delivers an **Android-only** weekly meal planning experience that helps a household/family plan a 7-day week with selectable recipes, preparation notes, seasonal suggestions, PRISMA store/availability support, and a combined shopping list that can be shared/imported to S-ostoslista. [intent-statement Q1–Q3, Q8, Q11–Q13] The MVP uses the full intent scope with **investigate-and-fallback** approaches for external integrations. [scope Q1], [feasibility-assessment]

**Must-have integration attempt:** Sydänmerkki integration (`sydanmerkki.fi` recipe query) is must-have to attempt for MVP. PRISMA availability automation and the official S-ostoslista method may use agreed fallbacks when needed. [scope Q2], [intent-statement Q14, Q15, Q17]

**Nutrition:** The MVP must actively reflect planning around nutrition (behavior/details to be specified in requirements/design; budget and dietary-restriction filtering are not explicitly committed as MVP behavior beyond this nutrition focus). [scope Q5], [intent-statement Q1], [R-01 context]

## In Scope (MVP)

| ID | Capability | Notes / Acceptance Boundary | Source |
|---|---|---|---|
| IN-01 | 7-day meal plan view | Weekends (Sat/Sun): 3 meals — Breakfast, Lunch, Dinner. Weekdays (Mon–Fri): 2 meals — Breakfast, Dinner. User can view the plan. | [intent-statement Q3] |
| IN-02 | Recipe suggestion and selection | Weekly recipe suggestions with preparation notes. User can view and select recipes they would actually use for plan slots. | [intent-statement Q2, Q3, Q11] |
| IN-03 | Weekday prep-time constraint | Weekday recipes (Mon–Fri Breakfast/Dinner) take no more than 30 minutes to prepare. Time-basis/missing-data handling to be defined later. | [intent-statement Q3], [TC-07] |
| IN-04 | Sydänmerkki recipe source (must attempt) | App should query recipes from `www.sydanmerkki.fi`. Investigate feasibility/terms/access; if live querying not feasible/allowed for MVP, use curated/imported Sydänmerkki recipe set fallback. | [intent-statement Q11, Q14], [scope Q2], [R-01, I-01, D-01] |
| IN-05 | Seasonal recipe selection | Recipes seasonal based on current date/season in user’s region, default from date/region with user override. Detailed mapping defined during implementation. | [intent-statement Q2, Q9, Q18], [TC-06, I-05] |
| IN-06 | Nutrition-aware planning | MVP actively reflects planning around nutrition in app behavior (e.g., nutrition-relevant recipe information, guidance, or selection cues as defined in requirements). Exact mechanism TBD in requirements/design. | [scope Q5], [intent-statement Q1] |
| IN-07 | PRISMA store selection & nearest suggestion | Suggest nearest PRISMA store by phone location (permission acceptable) with manual selection. Investigate/use reliable PRISMA store-location data source. | [intent-statement Q10, Q16], [TC-04, R-03, I-03, D-03] |
| IN-08 | PRISMA ingredient availability support | Verify ingredient availability against selected PRISMA store using available PRISMA data. Investigate feasibility/access; if automated availability not accessible for MVP, use best-effort/unknown indicators or checklist fallback. | [intent-statement Q12, Q15], [TC-03, R-02, I-02, D-02] |
| IN-09 | Combined shopping list | Generate/maintain a combined shopping list from selected weekly recipes/plan. | [intent-statement Q2, Q13] |
| IN-10 | S-ostoslista share/import support | Share/import shopping list to S-ostoslista using official method if one exists (format to be confirmed). User expected to have `s-ostoslista.fi` account. Investigate method/format; fallback to share-sheet/text export that fits S-ostoslista. | [intent-statement Q13, Q17], [TC-05, R-04, I-04, D-04] |
| IN-11 | Privacy/GDPR baseline for MVP | GDPR compliance required; minimal data handling; location permission with clear purpose/consent; avoid unnecessary third-party sharing; document data flows for any backend/service. | [feasibility Q5], [RC-01, R-05] |
| IN-12 | On-device MVP data, sync-ready design | MVP stores planning data on-device; design to allow cloud sync/account later (no MealPlanner account in MVP). | [feasibility Q3], [TC-08, A-02] |

## Out of Scope (MVP)

| ID | Item | Rationale | Source |
|---|---|---|---|
| OUT-01 | Grocery delivery integration | Explicitly excluded from first version. | [intent-statement Q7] |
| OUT-02 | Store-specific pricing | Explicitly excluded from first version. | [intent-statement Q7] |
| OUT-03 | MealPlanner account / cloud sync in MVP | On-device MVP; design sync-ready only. S-ostoslista account is separate/external. | [feasibility Q3] |
| OUT-04 | iOS / cross-platform release | Android-only first version. | [intent-statement Q8], [TC-01] |
| OUT-05 | Guaranteed live automation for all external integrations | Sydänmerkki integration is must-attempt; PRISMA availability automation and official S-ostoslista method may use fallbacks if not feasible/accessible/confirmed. | [scope Q2], [intent-statement Q14, Q15, Q17] |
| OUT-06 | Explicit budget or dietary-restriction filtering/guarantees (unless covered by nutrition behavior definition) | MVP actively reflects nutrition planning; budget/dietary-restriction behavior not explicitly specified beyond problem context. Capture as follow-up if needed. | [scope Q5], [intent-statement Q1] |
| OUT-07 | Formal legal review | GDPR principles applied to actual data flows; formal legal review outside this workflow artifact. | [feasibility Q5], [A-06] |

## Sequencing & Value Stream (guidance)
**Preference:** Value-first + dependency-first. [scope Q3]

1. **Foundations (dependency-first):** Android app shell, local data model (plan, recipes, shopping list, store preference), 7-day plan UI with correct meal slots, recipe selection/prep notes, weekday ≤30-min constraint handling, shopping list core.
2. **Early user value (value-first):** Usable planning flow with nutrition-aware cues and seasonal defaults/override; shareable shopping list baseline.
3. **Integration spikes + delivery:** Sydänmerkki query investigation/integration (must attempt) with curated/imported fallback; PRISMA store-location/availability investigation with manual/best-effort/checklist fallbacks; S-ostoslista method investigation with share-sheet/text fallback.
4. **Hardening:** GDPR/privacy UX (consent, data minimization), error/unknown states, and on-device persistence readiness for future sync.

## Constraints & Risks (summary)
- **Technical:** Android-only; third-party access/terms/data uncertainty (Sydänmerkki, PRISMA, S-ostoslista); weekday prep ≤30 min; backend/service possible but undecided. [TC-01–TC-08], [R-01–R-04, R-07]
- **Delivery:** Soft target ASAP (today great; next week fine), learning project, no known blockers. [OC-01–OC-03], [scope Q4], [R-06]
- **Compliance:** GDPR required; minimize data and handle location consent carefully. [RC-01, R-05]
- **Key open issues:** I-01 Sydänmerkki access; I-02 PRISMA availability data; I-03 PRISMA store-location source; I-04 S-ostoslista method/format; I-05 season mapping rule. [raid-log]

## Assumptions & Open Questions
- [assumption] “Actively reflect planning around nutrition” will be translated into concrete, testable behaviors during requirements/design (e.g., visible nutrition attributes, guidance, or selection support based on available recipe data). [scope Q5]
- [assumption] A small backend/service may be used where helpful for integrations; final stack/architecture decided later. [feasibility Q2], [A-01]
- [assumption] External integration fallbacks remain acceptable for MVP as defined in intent/feasibility. [intent-statement Q14, Q15, Q17], [A-03–A-05]
- Open: how nutrition is represented when Sydänmerkki/recipe data lacks structured nutrition fields (define in requirements). [scope Q5], [I-01]
- Open: prep-time basis and missing/ambiguous time handling (intent-capture R-03 context). [TC-07]
- Open: PRISMA/S-ostoslista data/method specifics to be investigated. [I-02–I-04]

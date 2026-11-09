# Intent Backlog — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner`  
**Sources:** [scope-document](scope-document.md), [intent-statement](../intent-capture/intent-statement.md), [feasibility-assessment](../feasibility/feasibility-assessment.md), [constraint-register](../feasibility/constraint-register.md), [raid-log](../feasibility/raid-log.md), [scope-definition-questions](scope-definition-questions.md) [Q1–Q5]

Prioritization uses **MoSCoW** plus sequencing aligned to value-first + dependency-first delivery. [scope Q2, Q3]

## Proto-Units / Backlog Items

| ID | Proto-Unit | MoSCoW | Rationale (value/risk/dependency) | Dependencies | Integration Fallback | Source |
|---|---|---|---|---|---|---|
| BU-01 | App foundation & local data model | Must | Foundation for plan, recipes, selections, shopping list, store preference; enables sync-ready design and early value. | None | N/A | [scope Q3], [IN-12], [A-02] |
| BU-02 | 7-day plan UI & meal-slot rules | Must | Core user-visible value: weekends 3 meals, weekdays Mon–Fri 2 meals. | BU-01 | N/A | [IN-01], [intent-statement Q3] |
| BU-03 | Recipe catalog, detail, and selection flow | Must | Users must view/select recipes they would actually use; includes prep notes. | BU-01, BU-02 | N/A (data source via BU-04/BU-05) | [IN-02], [intent-statement Q2, Q3] |
| BU-04 | Sydänmerkki recipe integration (must attempt) | Must | Required integration attempt; primary recipe-source direction. High uncertainty — investigate early in implementation flow but deliver with foundation/value. | BU-01, BU-03 | Curated/imported Sydänmerkki recipe set if live query not feasible/allowed. | [IN-04], [scope Q2], [R-01, I-01, D-01] |
| BU-05 | Weekday ≤30-min prep & seasonal selection | Must | Meets confirmed constraints and seasonal behavior (default date/region + override). | BU-03 (and BU-04 data where applicable) | Manual override / rule defaults; handle missing prep-time data explicitly. | [IN-03, IN-05], [TC-06, TC-07, I-05] |
| BU-06 | Nutrition-aware planning behavior | Must | MVP must actively reflect planning around nutrition. | BU-03, BU-05 | If structured nutrition missing, show available cues/notes and make limitation explicit (define in requirements). | [IN-06], [scope Q5] |
| BU-07 | Combined shopping list generation | Must | Converts selected plan into actionable output; high value. | BU-01, BU-03 | N/A | [IN-09], [intent-statement Q13] |
| BU-08 | PRISMA store selection & nearest-store suggestion | Should (capability in MVP scope) | Supports ingredient availability and local shopping; manual selection remains. | BU-01 | Manual store selection; degrade nearest suggestion if location/source unavailable. | [IN-07], [R-03, I-03, D-03] |
| BU-09 | PRISMA ingredient availability support | Should (capability in MVP scope) | Valuable but external data uncertain; fallback required if automation inaccessible. | BU-07, BU-08 | Best-effort/unknown indicators or checklist fallback. | [IN-08], [scope Q2], [R-02, I-02, D-02] |
| BU-10 | S-ostoslista share/import support | Should (capability in MVP scope) | Important export path; official method uncertain. | BU-07 | Share-sheet/text export that fits S-ostoslista. | [IN-10], [scope Q2], [R-04, I-04, D-04] |
| BU-11 | Privacy/GDPR UX & data-flow hardening | Must | Required compliance/privacy baseline: consent, minimization, transparency, safe data handling. | BU-01 (and any backend/service used by BU-04/08/09/10) | N/A; must be satisfied for MVP behavior. | [IN-11], [RC-01, R-05] |
| BU-12 | Integration investigation spikes & operational fallbacks | Must | Reduces delivery risk under ASAP + unknown integrations; ensures Sydänmerkki attempt and other fallbacks are actionable. | Parallel/cross-cutting with BU-04, BU-08–BU-10 | Use documented fallbacks per integration. | [scope Q2, Q3], [feasibility-assessment], [I-01–I-04] |

## Suggested Delivery Sequence (ASAP, soft target)
*Soft target: today would be great; next week is fine. No hard capability deadlines.* [scope Q4]

1. **Slice 1 — Foundations + usable core:** BU-01, BU-02, BU-03, BU-07 (minimal plan/select/list flow).
2. **Slice 2 — Constraints + nutrition:** BU-05, BU-06, BU-11 (baseline privacy/consent for data used).
3. **Slice 3 — Sydänmerkki must-attempt:** BU-12 (Sydänmerkki spike) + BU-04 integration with fallback ready.
4. **Slice 4 — Store/sharing integrations:** BU-08, BU-09, BU-10 with fallbacks; complete BU-11 for final data flows.

## Follow-ups (post-MVP / later refinement)
| ID | Item | Reason |
|---|---|---|
| FU-01 | Budget-aware planning behavior | Mentioned in problem context; not explicitly committed as MVP behavior. [intent-statement Q1], [OUT-06] |
| FU-02 | Dietary-restriction filtering/guarantees | Mentioned in problem context; not explicitly committed as MVP behavior. [intent-statement Q1], [OUT-06] |
| FU-03 | Cloud sync / MealPlanner account | Explicitly deferred; design kept sync-ready. [feasibility Q3], [OUT-03] |
| FU-04 | Grocery delivery / store-specific pricing | Explicitly out of MVP. [OUT-01, OUT-02] |

## Risks/Issues to Carry Forward
- Sydänmerkki access/terms/technical path unknown (R-01, I-01, D-01). Must attempt integration; fallback required.
- PRISMA availability and store-location data sources unknown (R-02, R-03, I-02, I-03, D-02, D-03).
- S-ostoslista official method/format unknown (R-04, I-04, D-04).
- Nutrition behavior must be made concrete in requirements despite possible missing structured nutrition data (scope Q5, FU-01/FU-02 boundary).
- GDPR/privacy must cover actual app/backend data flows (R-05, RC-01).

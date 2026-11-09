# Constraint Register — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner`  
**Sources:** [intent-statement](../intent-capture/intent-statement.md) [intent-statement], [feasibility-questions](feasibility-questions.md) [feasibility Q1–Q6]

## Technical Constraints

| ID | Constraint | Impact | Mitigation / Note | Source |
|---|---|---|---|---|
| TC-01 | Android-only first version. | Limits initial platform to Android; iOS/cross-platform out of MVP scope. | Plan UI/data layers so future expansion is possible if desired; not required now. | [intent-statement Q8] |
| TC-02 | App must query recipes from `www.sydanmerkki.fi`. | Depends on site terms, technical constraints, and available data/integration approach. | Investigate feasibility/terms; if live querying not feasible/allowed for MVP, use curated/imported Sydänmerkki recipe set fallback. | [intent-statement Q11, Q14], [feasibility Q1] |
| TC-03 | Ingredient availability must be verified against selected PRISMA store using available PRISMA data. | Depends on accessible PRISMA product/availability data, accuracy, permissions, and method. | Investigate feasibility/access; if not accessible for MVP, use best-effort/unknown indicators or checklist fallback. | [intent-statement Q12, Q15], [feasibility Q1] |
| TC-04 | Nearest PRISMA store suggested by phone location (permission acceptable) with manual selection. | Requires location permission handling and a reliable PRISMA store-location data source. | Use explicit consent; investigate/use reliable store-location data source; keep manual selection. | [intent-statement Q10, Q16] |
| TC-05 | Shopping list import/share to S-ostoslista via official method if exists (format to be confirmed); user has S-ostoslista account. | Depends on official/share method availability and list format. | Investigate method/format; fallback to share-sheet/text export that fits S-ostoslista if not confirmed for MVP. | [intent-statement Q13, Q17] |
| TC-06 | Seasonal recipes based on current date/season in user region, default with user override. | Needs regional/season mapping rule. | Define mapping during implementation; provide user override. | [intent-statement Q9, Q18] |
| TC-07 | Weekday recipes (Mon–Fri Breakfast/Dinner) must take no more than 30 minutes to prepare. | Recipe data/filtering must respect prep-time constraint where data exists. | Define time basis and handling of missing/ambiguous times in later stages (also noted in intent-capture review R-03). | [intent-statement Q3] |
| TC-08 | Stack not decided; backend/service expected; MVP data on-device with design for later sync/account. | Architecture must support integrations and future evolution without requiring MealPlanner account in MVP. | Recommend native Android Kotlin (+ modern UI toolkit) and small backend/service where helpful for integrations; keep local persistence sync-ready. | [feasibility Q2, Q3] |

## Organizational / Delivery Constraints

| ID | Constraint | Impact | Mitigation / Note | Source |
|---|---|---|---|---|
| OC-01 | Personal need + learning project; decision-maker is the user. | Priorities and pace set by one person; learning curve affects delivery. | Keep MVP scope tight; use fallbacks to reduce integration risk. | [intent-statement Q4, Q6] |
| OC-02 | Timeline: as early as possible (ASAP). | Pressure to deliver quickly conflicts with integration unknowns and learning. | Prioritize core 7-day plan + selection + shopping list; implement investigate-and-fallback integrations incrementally. | [feasibility Q4], [intent-statement Q7] |
| OC-03 | No known blockers (device/emulator and needed accounts available). | Low current delivery blocker risk. | Confirm account/access/setup details during implementation/integration spikes. | [feasibility Q6], [intent-statement Q17] |
| OC-04 | Out of first version: no grocery delivery integration or store-specific pricing. | Prevents scope expansion into delivery/pricing domains. | Enforce boundary in later requirements/design. | [intent-statement Q7] |

## Regulatory / Compliance Constraints

| ID | Constraint | Impact | Mitigation / Note | Source |
|---|---|---|---|---|
| RC-01 | App must comply with GDPR; user personal privacy is very important. | Requires privacy-by-design: data minimization, lawful basis/consent where applicable (especially location), transparency, and careful handling of any data processed by app/backend/third parties. | Keep data handling minimal; request location permission with clear purpose/consent; avoid unnecessary third-party sharing; define retention/deletion and data-flow documentation in later stages. | [feasibility Q5], [intent-statement Q10, Q16] |
| RC-02 | Use of third-party sources/services (Sydänmerkki, PRISMA, S-ostoslista) must respect terms and permissions. | Legal/technical access constraints may limit live integration. | Investigate terms/access; use accepted fallbacks when not feasible/allowed for MVP. | [intent-statement], [feasibility Q1, Q14, Q15, Q17] |

## Assumptions & Open Questions
- [assumption] GDPR compliance for MVP will be addressed through appropriate technical/UX measures for actual data flows; formal legal review is outside this stage. [feasibility Q5]
- [assumption] “Available PRISMA data” source/method and S-ostoslista official/share format remain to be investigated. [feasibility Q15, Q17]
- [assumption] Detailed Finland/region-to-season mapping rules will be defined during implementation. [intent-statement Q18]

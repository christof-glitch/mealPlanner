# RAID Log — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner`  
**Sources:** [intent-statement](../intent-capture/intent-statement.md) [intent-statement], [feasibility-questions](feasibility-questions.md) [feasibility Q1–Q6]

RAID = **R**isks, **A**ssumptions, **I**ssues, **D**ependencies.

## Risks

| ID | Risk | Likelihood | Impact | Mitigation | Owner (TBD) | Source |
|---|---|---|---|---|---|---|
| R-01 | Sydänmerkki recipe querying is not feasible/allowed (terms, technical constraints, no accessible data path) for MVP. | Medium | High | Investigate feasibility/terms early; use curated/imported Sydänmerkki recipe set fallback. [intent-statement Q14] | Decision-maker / builder | [intent-statement Q11, Q14], [feasibility Q1] |
| R-02 | PRISMA ingredient availability data is not accessible/accurate/usable for automated checks in MVP. | Medium | High | Investigate available PRISMA data source/method; use best-effort/unknown indicators or checklist fallback. [intent-statement Q15] | Decision-maker / builder | [intent-statement Q12, Q15], [feasibility Q1] |
| R-03 | PRISMA store-location data source is unreliable/unavailable, affecting nearest-store suggestion. | Medium | Medium | Investigate reliable store-location source; keep manual store selection; handle location permission gracefully. [intent-statement Q10, Q16] | Decision-maker / builder | [feasibility Q1, Q6] |
| R-04 | S-ostoslista official import/share method/format cannot be confirmed or integrated smoothly in MVP. | Medium | Medium | Investigate method/format (user has S-ostoslista account; sharing possible); fallback to share-sheet/text export that fits S-ostoslista. [intent-statement Q17] | Decision-maker / builder | [intent-statement Q13, Q17] |
| R-05 | GDPR compliance gaps (location/personal data handling, backend data flows, transparency/consent) create privacy risk. | Medium | High | Apply privacy-by-design: minimal data, clear consent for location, avoid unnecessary third-party sharing, document data flows/retention; validate during design/build. [feasibility Q5] | Decision-maker / builder | [feasibility Q5] |
| R-06 | ASAP timeline conflicts with learning curve and integration investigations. | Medium | Medium | Protect MVP scope (no delivery/pricing), use fallbacks, prioritize core 7-day plan + selection + shopping list. [intent-statement Q7], [feasibility Q4] | Decision-maker / builder | [feasibility Q4] |
| R-07 | Backend/service (expected) adds complexity/operational burden for a learning MVP. | Medium | Medium | Keep backend minimal and purpose-specific (integration support/caching/proxy) if used; prefer simplest viable approach; on-device core remains functional where possible. [feasibility Q2, Q3] | Decision-maker / builder | [feasibility Q2] |

## Assumptions

| ID | Assumption | Validation Trigger | Source |
|---|---|---|---|
| A-01 | Native Android with Kotlin is an appropriate recommended stack for this learning MVP; a small backend/service may be used for integrations. | Confirm during design/implementation planning. | [feasibility Q2] |
| A-02 | MVP planning data can be stored on-device, designed for later sync/account without requiring MealPlanner account now. | Confirm data model/storage approach in design. | [feasibility Q3] |
| A-03 | Curated/imported Sydänmerkki fallback is acceptable if live querying is not feasible/allowed. | Trigger if Sydänmerkki investigation fails. | [intent-statement Q14] |
| A-04 | Best-effort/unknown indicators or checklist fallback is acceptable if PRISMA availability automation is not accessible. | Trigger if PRISMA data investigation fails. | [intent-statement Q15] |
| A-05 | Share-sheet/text export fallback is acceptable if official S-ostoslista method/format cannot be confirmed for MVP. | Trigger if S-ostoslista method investigation fails. | [intent-statement Q17] |
| A-06 | GDPR compliance for MVP can be achieved through appropriate minimal-data/consent/data-flow practices for actual implementation (formal legal review out of scope here). | Validate when data flows/backend are defined. | [feasibility Q5] |
| A-07 | Stakeholder/communication process remains informal for first version (kept as open assumption). | Revisit if project involves instructor/client/team. | [intent-statement Q19] |

## Issues

| ID | Issue | Status | Action | Source |
|---|---|---|---|---|
| I-01 | No confirmed technical access path for Sydänmerkki recipe querying. | Open | Investigate terms/data approach; prepare curated/imported fallback. | [intent-statement Q11, Q14] |
| I-02 | No confirmed “available PRISMA data” source/method for ingredient availability. | Open | Investigate PRISMA data access/permissions/accuracy. | [intent-statement Q12, Q15] |
| I-03 | No confirmed reliable PRISMA store-location data source. | Open | Investigate store-location source compatible with location-based suggestion. | [intent-statement Q10, Q16] |
| I-04 | No confirmed S-ostoslista official import/share method or list format. | Open | Investigate S-ostoslista sharing/import behavior and acceptable formats. | [intent-statement Q13, Q17] |
| I-05 | Detailed season-to-region/date mapping rule not defined. | Open | Define in implementation/design (default date/region + user override already decided). | [intent-statement Q9, Q18] |

## Dependencies

| ID | Dependency | Type | Status | Impact if Unresolved | Source |
|---|---|---|---|---|---|
| D-01 | `www.sydanmerkki.fi` recipe data/terms/technical access. | External | Unconfirmed | Use curated/imported Sydänmerkki fallback; live query may be deferred. | [intent-statement Q11, Q14] |
| D-02 | PRISMA product/availability data (“available PRISMA data”) and access permissions. | External | Unconfirmed | Use best-effort/unknown/checklist fallback for availability. | [intent-statement Q12, Q15] |
| D-03 | PRISMA store-location data source and Android location permission. | External / Platform | Partially confirmed (permission acceptable) | Use manual store selection; nearest suggestion may be limited. | [intent-statement Q10, Q16] |
| D-04 | S-ostoslista (`s-ostoslista.fi`) share/import behavior and user account. | External | Partially confirmed (user account/sharing expected) | Use share-sheet/text export fallback. | [intent-statement Q13, Q17] |
| D-05 | Android device/emulator and developer accounts/tooling. | Platform / Personal | Available (reported) | Low; testing/development blocked if unavailable. | [feasibility Q6] |

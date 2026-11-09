# Feasibility Assessment — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Sources:** [intent-statement](../intent-capture/intent-statement.md) [intent-statement], [feasibility-questions](feasibility-questions.md) [feasibility Q1–Q6]

## Summary Verdict
The MVP is **feasible with managed risk**, provided the team treats external integrations (Sydänmerkki recipes, PRISMA store/location/availability, S-ostoslista sharing) as investigation-and-fallback work and keeps the first release within the confirmed Android-only scope. [intent-statement], [feasibility Q1, Q14–Q17] The main feasibility risks are data access/terms for third-party services, GDPR-compliant handling of user/location data, and delivery pressure (ASAP) on a learning project. [feasibility Q4, Q5]

## Technical Feasibility

| Area | Assessment | Rationale | Source |
|---|---|---|---|
| Android MVP app (7-day plan, recipe selection, prep notes, shopping list UI) | Feasible | Core app behavior is a standard Android product surface: weekly plan view, meal slots (weekend 3, weekday 2 Mon–Fri), recipe detail/prep notes, and shopping-list aggregation/selection. [intent-statement Q3, Q8] | [intent-statement] |
| Sydänmerkki recipe query (`sydanmerkki.fi`) | Feasible to attempt; fallback required | User requires the app to query recipes from Sydänmerkki. Feasibility depends on site terms, technical constraints, and available data/integration approach. An accepted fallback is a curated/imported Sydänmerkki recipe set if live querying is not feasible/allowed for MVP. [intent-statement Q11, Q14] | [intent-statement], [feasibility Q1] |
| PRISMA nearest store + availability | Feasible with investigation; fallback required | Nearest-store suggestion uses phone location with permission and manual selection. Location permission is acceptable; a reliable PRISMA store-location data source must be investigated/used. Ingredient availability is to be verified against selected PRISMA store using available PRISMA data; if automated availability is not accessible for MVP, best-effort/unknown indicators or checklist fallback are accepted. [intent-statement Q10, Q12, Q15, Q16] | [intent-statement], [feasibility Q1, Q6] |
| S-ostoslista import/share | Feasible with investigation; fallback required | User expects an official import/share method if one exists (format to be confirmed). User will have an S-ostoslista (`s-ostoslista.fi`) account and sharing is possible there; list format/method must be investigated. Accepted fallback: share-sheet/text export that fits what S-ostoslista accepts. [intent-statement Q13, Q17] | [intent-statement] |
| Seasonal selection (date/region + override) | Feasible | Default season from current date/region with user override is straightforward; detailed regional/season mapping rule will be defined during implementation. [intent-statement Q9, Q18] | [intent-statement] |
| Data storage / future sync | Feasible | MVP stores planning data on-device; design should allow cloud sync/account later. No MealPlanner account required in MVP. [feasibility Q3] | [feasibility Q3] |

## Stack Recommendation (feasibility-level)
The stack is not decided; the user requested a recommendation and expects a backend/service may be used. [feasibility Q2]

- **Recommended client:** Native Android with **Kotlin** and a modern Android UI toolkit (e.g., Jetpack Compose) in Android Studio. Rationale: aligns with Android-only MVP, strong learning value, and good fit for on-device data, permissions, and share flows. [feasibility Q2], [intent-statement Q8]
- **Recommended integration approach:** Use a **small backend/service** where helpful for third-party integration concerns (investigating/normalizing recipe queries, PRISMA data access patterns, caching, and reducing direct dependency on fragile web scraping from the mobile client). This is a feasibility recommendation, not a final architecture decision; details belong to later design stages. [feasibility Q2]
- **Local data:** On-device persistence for meal plans, selections, shopping list state, and preferred store; structure data so future sync/account can be added later. [feasibility Q3]

## Delivery Feasibility
- **Timeline:** ASAP, on a learning project. Feasibility improves if MVP scope is protected and integration fallbacks are used when investigations block progress. [feasibility Q4], [intent-statement Q7]
- **Blockers:** No known blockers reported; Android device/emulator and needed accounts are available. [feasibility Q6]

## Compliance / Privacy Feasibility
- **GDPR is required.** The app must comply with GDPR and treat user personal privacy as important. [feasibility Q5]
- **Practical MVP implications (to be validated in later stages):** minimize personal data; request location permission with clear purpose/consent; avoid unnecessary sharing of personal data with third parties; define retention/deletion behavior for on-device data; document data flows for any backend/service used. [feasibility Q5], [intent-statement Q10, Q16], [feasibility Q3]

## Key Risks to Manage
- Third-party data access/terms uncertainty (Sydänmerkki, PRISMA, S-ostoslista) — mitigated by explicit investigate-and-fallback decisions. [intent-statement], [feasibility Q1]
- GDPR scope creep if backend/sync or analytics are added later — mitigated by minimal data handling and privacy-by-design in MVP. [feasibility Q3, Q5]
- ASAP timeline vs. learning + integration unknowns — mitigated by MVP boundaries and fallback paths. [feasibility Q4], [intent-statement Q7]

## Assumptions & Open Questions
- [assumption] A small backend/service is acceptable if needed for integrations; exact hosting/stack and data-processing responsibilities will be decided in design stages. [feasibility Q2]
- [assumption] “GDPR compliance” for this MVP means applying core principles (data minimization, purpose limitation, consent/transparency for location, user control/retention considerations) appropriate to the app’s actual data flows; formal legal review is outside this feasibility artifact. [feasibility Q5]
- [assumption] Sydänmerkki/PRISMA/S-ostoslista access methods and terms will be investigated; fallbacks remain acceptable for MVP when not feasible/allowed. [intent-statement], [feasibility Q1]

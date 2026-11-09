# Scope Definition Questions — Weekly Meal Planner (MVP)

Sources: [intent-statement](../intent-capture/intent-statement.md), [feasibility-assessment](../feasibility/feasibility-assessment.md), [constraint-register](../feasibility/constraint-register.md)

## Q1. [Product] What is the minimum viable scope that delivers value for this first Android release?

The intent already defines a strong MVP candidate: a 7-day meal plan (weekends 3 meals: Breakfast/Lunch/Dinner; weekdays Mon–Fri 2 meals: Breakfast/Dinner), with selectable recipes (weekday prep ≤ 30 minutes), Sydänmerkki recipe suggestions, seasonal selection, PRISMA store/availability support, and a combined shopping list shareable to S-ostoslista — with investigate-and-fallback approaches for external integrations. [intent-statement], [feasibility-assessment]

A. Use the intent as the MVP scope (all confirmed capabilities), implementing integrations with the agreed fallbacks when needed
B. Trim MVP further for ASAP delivery — I can specify what to defer (e.g., PRISMA availability automation, S-ostoslista official method, live Sydänmerkki query)
C. Keep the core 7-day plan + recipe selection + shopping list as MVP; treat PRISMA/Sydänmerkki/S-ostoslista integrations as best-effort enhancements with fallbacks
D. Not sure — I’d like a recommendation
X. Other (please specify)

[Answer]: A — Use the intent as the MVP scope (all confirmed capabilities), implementing integrations with the agreed fallbacks when needed.

## Q2. [Product/Technical] Which capabilities are must-have vs. nice-to-have for the MVP, given the investigate-and-fallback decisions?

This helps prioritize when time is tight (ASAP). External integrations already have accepted fallbacks: curated/imported Sydänmerkki recipes, PRISMA availability best-effort/unknown/checklist, S-ostoslista share-sheet/text export. [intent-statement Q14, Q15, Q17], [feasibility-assessment]

A. Must-have: 7-day plan with correct meal slots, recipe selection, prep notes, ≤30-min weekday filter, seasonal default+override, shopping list; Nice-to-have: live Sydänmerkki query, automated PRISMA availability, official S-ostoslista method (fallbacks acceptable)
B. Must-have includes at least one “live” integration (specify which: Sydänmerkki / PRISMA availability / S-ostoslista official) — others can use fallbacks
C. All confirmed capabilities are must-have; use fallbacks only when an integration is proven infeasible/blocked
D. Not sure — I’d like a recommendation
X. Other (please specify)

[Answer]: B — Must-have includes at least one “live” integration: Sydänmerkki integration (the app should query recipes from `sydanmerkki.fi`) is must-have to attempt; PRISMA availability automation and official S-ostoslista method may use agreed fallbacks when needed.

## Q3. [Delivery] What sequencing preference should guide the work (considering integration unknowns and ASAP timeline)?

This affects how we order proto-Units/capabilities. Integration dependencies (Sydänmerkki, PRISMA, S-ostoslista) carry uncertainty; fallbacks exist. [constraint-register], [raid-log]

A. Value-first: deliver the usable 7-day planning + shopping list experience early, then improve integrations
B. Risk-first: investigate/validate Sydänmerkki, PRISMA, and S-ostoslista constraints early, then build the full experience
C. Dependency-first: build foundations (data model, plan UI, recipe/shopping list core) before external integrations
D. Balanced: foundations + quick integration spikes, then complete MVP with fallbacks as needed
X. Other (please specify)

[Answer]: A and C — Value-first and dependency-first: deliver the usable 7-day planning + shopping list experience early, built on foundations (data model, plan UI, recipe/shopping list core), then integrate/improve external services (with Sydänmerkki integration treated as must-have to attempt and agreed fallbacks available).

## Q4. [Delivery] Are there any hard deadlines tied to specific capabilities in this MVP?

You indicated ASAP delivery as a learning project with no fixed deadline in the usual sense. [feasibility Q4]

A. No hard deadlines; ASAP is a preference
B. Yes — I can specify a date and which capabilities must be ready
C. A soft target exists (learning milestone/demo) — I can specify
X. Other (please specify)

[Answer]: C — Soft target: today would be great; next week is fine. No hard deadline tied to specific capabilities.

## Q5. [Product] Should the MVP actively reflect planning around budget, nutrition, or dietary restrictions in app behavior, or treat those as context-only for now?

The problem/target mention planning around budget, nutrition, or dietary restrictions, but current confirmed capabilities don’t explicitly define filtering/labels/constraints behavior (intent-capture advisory finding R-01). [intent-statement Q1], [intent-capture review context]

A. Context-only for MVP — no specific budget/nutrition/dietary-restriction filtering or guarantees; keep the core planning experience
B. Include lightweight behavior in MVP — I can specify (e.g., user notes/tags, simple dietary preference filter if recipe data supports it)
C. Defer to a later version; capture as a follow-up capability
X. Other (please specify)

[Answer]: X (Other) — The MVP should actively reflect planning around nutrition.

## Consolidated Summary Confirmation

- Looks correct
- Request changes

[Answer]: Looks correct


# User Stories Assessment

**Intent:** `261107-weekly-meal-planner` (Android MVP, `mvp`)  
**Stage:** User Stories (inception)  
**Date:** 2026-11-07

## Decision
**Execute** — user stories add clear value for this project.

## Rationale
The MVP is user-facing and centers on a household/family planning workflow: reviewing a 7-day plan, viewing/selecting recipes, handling nutrition/season/time constraints, choosing a PRISMA store, checking ingredient presence with fallbacks, editing a combined shopping list, and exporting/sharing to S-ostoslista. Stories will make acceptance criteria testable, expose integration/fallback behavior, and provide traceability from requirements (FR1–FR12, NFR1–NFR5) to downstream design and implementation.

## Factors Considered
- **Project type:** Android-only consumer/mobile MVP with external integrations and local-first fallback behavior.
- **User-facing scope:** Multiple screens/flows (onboarding/today, week plan, recipe details/alternatives, grocery/store/availability, export/share, preferences/permissions).
- **Complexity signals:** Conditional integrations (Sydänmerkki, PRISMA, S-ostoslista), permission handling, missing/ambiguous data states, merge-safe shopping-list editing, and GDPR/minimal-data constraints.
- **Personas/team coordination:** Household/family planner is primary; supporting contexts (recipe chooser, shopper) can be represented as personas or goals.

## Key Areas Where Stories Will Add the Most Value
- 7-day plan structure and recipe selection/replacement behavior.
- Weekday ≤30 minute total-time eligibility and missing/ambiguous time explanations.
- Seasonal suggestions based on device date/location with unknown-season handling.
- Nutrition cues/unavailable labeling plus one nutrition-aware planning aid.
- PRISMA store selection and best-effort ingredient name/presence checks with `Unknown`/manual checklist fallback.
- Combined shopping-list merge/edit/checkoff and export of the current edited list.
- S-ostoslista official-method attempt (if feasible) plus mandatory share/copy fallback with instructions.
- Location consent/purpose text, on-device/local-first behavior, and integration failure states that do not block core planning.

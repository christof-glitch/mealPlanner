<!-- INVARIANT: examples are single-line HTML comments so a fresh template parses to total=0 (MEMORY_EMPTY). Do NOT un-comment or split across lines. t100 guards this. -->
> This file is kept up to date automatically while the stage runs. Add observations at the review step, not by editing here directly.

## Interpretations
<!-- example: 2026-05-29T10:14:32Z — chose REST over GraphQL; the consuming team only needs CRUD, revisit if subscriptions land -->
- 2026-11-09T12:40Z — Reopened U1 `u1-mvp-foundation-skeleton` functional-design after skeleton checkpoint Request Changes (user requested rebuild of U1 code generation from originally approved plan/test instructions). Existing Q1–Q8 answers remained confirmed (`Looks correct`); entities/rules/functional-spec retained and validated; traceability updated with `reverse` explanations for cross-cutting BR1.1/BR1.3/BR1.4/BR1.13/BR1.14 to satisfy orphan check under new run floor.
- 2026-11-09T14:00Z — Started U2 `u2-recipe-planning-completion` functional design (full mode). U2 completes C2/C3 baselines from U1: Sydänmerkki adapter boundary/fallback status (OQ1 investigational), season fit mapping/unknown/override integration (OQ5), nutrition cues/minimal planning aid from reliably available attributes (OQ6), weekday time normalization/eligibility explanations (OQ7, BR1.2/AC2.2.3), alternatives/replacement and Today/Plan sync contracts (R-06). Will generate focused questions for open design choices and preserve U1 invariants/contracts.
- 2026-11-09T20:45Z — U4 `u4-android-ui-experience` functional design (UI unit): thin Compose/Material 3 orchestration over U1–U3 contracts (ADR-005), RM-01–RM-09 coverage, R-05/R-06/ADR-006 invariants preserved in UI flows, C5 name/presence-only and C6 mandatory fallback surfaced; produced `functional-spec.md`, `frontend-components.md`, `traceability.json` (US1.1–US5.6 + cross-cutting). No questions asked (autonomous, sufficient upstream).

## Deviations
<!-- example: 2026-05-29T10:14:32Z — skipped the optional caching layer the stage prose suggested; the dataset is small enough that it adds risk -->
- 2026-11-09T14:20Z — U2 Q1 answered **B** (live-first). Resolved via FQ1=A: live Sydänmerkki adapter path required in U2 boundary, but live execution gated by confirmed feasibility/terms/allowed access (OQ1); mandatory curated/imported Sydänmerkki-aligned fallback always available; typed status/investigation outcome recorded. No scraping/terms assumptions.

## Tradeoffs
<!-- example: 2026-05-29T10:14:32Z — picked TDD over BDD this run; the team is unit-first and the domain is well-understood -->
- 2026-11-09T14:25Z — U2 Sydänmerkki path (Q1=B, FQ1=A): required live adapter boundary in design but gated live execution by confirmed feasibility/terms/allowed access (OQ1). Preserves FR4 investigation/adapter intent while avoiding scraping/terms assumptions; curated/imported fallback remains mandatory and runnable.
- 2026-11-09T15:05Z — U2 review iteration 1 NOT-READY addressed: canonical `PlanSlotRef` everywhere; `WeeklyNutritionBalanceCueRequest` added so C2 aggregates from C3/U4-supplied nutrition inputs (C2 must not read `WeeklyPlanState`); `RecipeFilters`/shared type sources documented; `parse_error_mapped`/partial safe live data clarified with curated fallback behavior.

## Open questions
<!-- example: 2026-05-29T10:14:32Z — confirm the retention window with compliance before the next stage hardens the schema -->

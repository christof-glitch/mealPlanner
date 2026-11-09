<!-- INVARIANT: examples are single-line HTML comments so a fresh template parses to total=0 (MEMORY_EMPTY). Do NOT un-comment or split across lines. t100 guards this. -->
> This file is kept up to date automatically while the stage runs. Add observations at the review step, not by editing here directly.

## Interpretations
<!-- example: 2026-05-29T10:14:32Z — chose REST over GraphQL; the consuming team only needs CRUD, revisit if subscriptions land -->
- 2026-11-09T14:40Z — U2 NFR Requirements (unit `u2-recipe-planning-completion`, library C2/C3 + A1): produced plan + questions (Q1–Q5, Recommended defaults) covering A1 bounded/fallback behavior, on-device MVP performance baseline, typed observability hooks, no server scalability, security/privacy/minimal data. Awaiting plan approval (human stop).

## Deviations
<!-- example: 2026-05-29T10:14:32Z — skipped the optional caching layer the stage prose suggested; the dataset is small enough that it adds risk -->

## Tradeoffs
<!-- example: 2026-05-29T10:14:32Z — picked TDD over BDD this run; the team is unit-first and the domain is well-understood -->

## Open questions
<!-- example: 2026-05-29T10:14:32Z — confirm the retention window with compliance before the next stage hardens the schema -->
- 2026-11-09T14:45Z — U2 NFR Requirements: OQ1/OQ5–OQ7 remain open (carried forward). A1 live path remains gated (FQ1=A); curated fallback mandatory. Exact ms/perf thresholds and logging hook implementation deferred to NFR Design/testing.

- 2026-11-09T19:45Z — U3 NFR Requirements (`u3-grocery-store-export-completion`, library C4/C5/C6): created plan + questions (Q1–Q5=A, resolved from U1/U2/upstream), produced performance/security/scalability/reliability/observability requirements, tech-stack-decisions, and traceability. Key constraints: PRISMA name/presence only (`PRESENT|NOT_FOUND|UNKNOWN`, never stock/guaranteed availability), ADR-006 exact stale warning/no auto-regeneration/explicit regenerate confirmation, ShoppingList owns `applyPresenceCheckBatch` projection/remap (Unknown after ambiguous edit/merge/regenerate), export uses current edited list with mandatory share/copy fallback and stable inward extraction seam (BR3.9). OQ2–OQ4 investigational; adapter boundaries typed/fallback-first.

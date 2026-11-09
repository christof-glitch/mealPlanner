<!-- INVARIANT: examples are single-line HTML comments so a fresh template parses to total=0 (MEMORY_EMPTY). Do NOT un-comment or split across lines. t100 guards this. -->
> This file is kept up to date automatically while the stage runs. Add observations at the review step, not by editing here directly.

## Interpretations
<!-- example: 2026-05-29T10:14:32Z — chose REST over GraphQL; the consuming team only needs CRUD, revisit if subscriptions land -->
- 2026-11-08T13:57:42Z — Contract Design for single Android app (no public/external API exposed by app). Focus on in-app Kotlin unit contracts (U1–U4) and outbound adapter contracts to Sydänmerkki/PRISMA/S-ostoslista/Android platform. Incorporating Units Generation direction R-01–R-04 into questions/artifacts.

## Deviations
<!-- example: 2026-05-29T10:14:32Z — skipped the optional caching layer the stage prose suggested; the dataset is small enough that it adds risk -->
- 2026-11-08T14:02:00Z — Q1–Q3 answers captured in questions file from structured batch; `log answer` refused (no new human reply recorded for question). Proceeding with file as source of truth; will reconcile if engine requires receipt later.

## Tradeoffs
<!-- example: 2026-05-29T10:14:32Z — picked TDD over BDD this run; the team is unit-first and the domain is well-understood -->

## Open questions
<!-- example: 2026-05-29T10:14:32Z — confirm the retention window with compliance before the next stage hardens the schema -->
- 2026-11-08T14:03:00Z — Summary confirmation receipt (`log answer --checkpoint summary-confirmation`) was refused by engine guard in this environment despite explicit user confirmation via structured question; questions file records `[Answer]: Looks correct`. Review/gate may require reconciliation.

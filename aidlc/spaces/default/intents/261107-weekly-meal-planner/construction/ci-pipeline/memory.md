<!-- INVARIANT: examples are single-line HTML comments so a fresh template parses to total=0 (MEMORY_EMPTY). Do NOT un-comment or split across lines. t100 guards this. -->
> This file is kept up to date automatically while the stage runs. Add observations at the review step, not by editing here directly.

## Interpretations
<!-- example: 2026-05-29T10:14:32Z — chose REST over GraphQL; the consuming team only needs CRUD, revisit if subscriptions land -->
- 2026-11-10T00:00:00Z — Selected GitHub Actions (remote is GitHub, no prior CI) with trunk-based `main` triggers (push/PR) to enforce MVP CI-before-merge obligation deferred from Build and Test.

## Deviations
<!-- example: 2026-05-29T10:14:32Z — skipped the optional caching layer the stage prose suggested; the dataset is small enough that it adds risk -->
- 2026-11-10T00:00:00Z — Enforced MVP 80% JaCoCo line-coverage floor via CI XML check step because `app/build.gradle.kts` generates report but does not define `jacocoTestCoverageVerification`; gate preserved without weakening thresholds.

## Tradeoffs
<!-- example: 2026-05-29T10:14:32Z — picked TDD over BDD this run; the team is unit-first and the domain is well-understood -->
- 2026-11-10T00:00:00Z — Stored reports as GitHub Actions artifacts (not external artifact repo/release distribution) for MVP traceability; release signing/distribution left for deployment/operation concerns.

## Open questions
<!-- example: 2026-05-29T10:14:32Z — confirm the retention window with compliance before the next stage hardens the schema -->
- 2026-11-10T00:00:00Z — Android SDK provisioning in GitHub-hosted runners may vary; workflow uses `android-actions/setup-android@v3` but may need adjustment if runner images change. No live Sydänmerkki/PRISMA/S-ostoslista integrations in CI (OQ1–OQ4).

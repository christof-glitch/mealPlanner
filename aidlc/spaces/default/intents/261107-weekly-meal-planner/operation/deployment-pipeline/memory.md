<!-- INVARIANT: examples are single-line HTML comments so a fresh template parses to total=0 (MEMORY_EMPTY). Do NOT un-comment or split across lines. t100 guards this. -->
> This file is kept up to date automatically while the stage runs. Add observations at the review step, not by editing here directly.

## Interpretations
- 2026-11-14T00:00:00Z — [deployment-pipeline] Treated "deployment pipeline" for this on-device Android MVP as Play Store release/CD configuration (tracks, signed AAB delivery, promotion gates, rollout/rollback) rather than AWS server deployment; grounded in existing GitHub Actions CI, Build and Test evidence, and U2–U4 infrastructure-design artifacts that state no cloud infra is provisioned.

## Deviations
- 2026-11-14T00:00:00Z — [deployment-pipeline] Stage topic examples mention AWS strategies/services (blue/green, CloudWatch Evidently, AppConfig); documented them as not applicable for the no-backend Android client and selected Android-appropriate equivalents (staged Play Store rollout, build-config/in-app default-off flags) per confirmed answers and constraints (OQ1–OQ4, FR8/FR10).

## Tradeoffs
- 2026-11-14T00:00:00Z — [deployment-pipeline] Chose CI-assisted signed release build with manual Play Console upload (Q4) over fully automated Play Developer API publishing to reduce secret/store-setup risk now while keeping a repeatable build path; documented automation as a future prerequisite-gated enhancement.

## Open questions
- 2026-11-14T00:00:00Z — [deployment-pipeline] Release signing/keystore, Play App Signing, and any GitHub protected-environment secrets must be configured outside the repo before CI-assisted signed AAB builds can run; Play Console track setup and tester access are operational prerequisites.

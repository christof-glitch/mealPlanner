# CI Pipeline Questions

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Stage:** CI Pipeline (3.7, Construction)  
**Date:** 2026-11-10

Questions gathered per stage requirements (branch strategy, CI tool, quality gates, artifact repository) with answers recorded below.

## Q1 — Branch strategy
**Question:** Which branching strategy should CI enforce for this repository?  
**Options:** A. Trunk-based (single `main`, short-lived branches/PRs), B. GitFlow, C. GitHub Flow, D. Other.  
**[Answer]: A. Trunk-based development**  
**Rationale:** Aligns with team/org affirmed practice (`aidlc/spaces/default/memory/team.md`, `org.md`) and current repo usage (`main` is the primary line; commits/PRs merge after checks). PRs to `main` and pushes to `main` trigger CI.

## Q2 — CI tool
**Question:** Which CI tool/platform should be used?  
**Options:** A. GitHub Actions, B. GitLab CI, C. AWS CodeBuild/CodePipeline, D. Other.  
**[Answer]: A. GitHub Actions**  
**Rationale:** Repository remote is hosted on GitHub (`https://github.com/christof-glitch/mealPlanner.git`) and no existing CI config was found. Workflow implemented at `.github/workflows/ci.yml`.

## Q3 — Quality gates
**Question:** Which quality gates must pass before merge/build promotion?  
**Options:** A. Build + unit tests only, B. Build + unit tests + coverage floor, C. Build + tests + coverage + lint/static analysis, D. Other.  
**[Answer]: B. Build + unit tests + JaCoCo coverage floor (MVP 80% line), with recorded commands enforced**  
**Rationale:** Build and Test recorded verification command and evidence require `./gradlew :app:testDebugUnitTest --console=plain` and JaCoCo LINE >= 80% on full assembled suite (measured 84.83%, 2729/3427, 2026-11-10; floor not weakened). CI runs `./gradlew :app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport --console=plain` and fails if JaCoCo XML reports line coverage < 80%. Lint/static-analysis tooling is not separately configured as a blocking gate in this MVP baseline but compile/test execution provides type/build checks; gates can be extended later without weakening current targets.

## Q4 — Artifact repository
**Question:** Where should build artifacts/reports be stored?  
**Options:** A. CI job artifacts only, B. GitHub Packages, C. External artifact repo (Nexus/Artifactory/S3), D. Other.  
**[Answer]: A. CI job artifacts (GitHub Actions artifacts) for MVP**  
**Rationale:** MVP Android app has no deployment artifact repository requirement defined; debug/test reports are sufficient for CI visibility. CI uploads JaCoCo XML/HTML and test results as workflow artifacts for traceability. Release signing/distribution artifact storage can be added in deployment/release stages if needed.

## Notes
- No live Sydänmerkki/PRISMA/S-ostoslista integrations are executed in CI (OQ1–OQ4 investigational/gated; mandatory fallbacks preserved).
- No secrets, credentials, or raw GPS/PII are required by CI config.

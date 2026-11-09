# Infrastructure Design Questions — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Date:** 2026-11-09

Resolved with Recommended defaults (Android MVP library, no cloud infra).

**Q1. Deployment/infrastructure scope**
- A) (Recommended) U3 is on-device library code; no AWS/cloud resources, no server deployment, no standalone packaging. CI/CD covers Gradle build/test/static checks and safe fallback/investigational-adapter boundaries.
- B) Provision cloud services for grocery/export.
[Answer]: A

**Q2. CI verification gates for U3**
- A) (Recommended) Run layout-explicit tests covering U3 module/library tests plus project verification (`./gradlew :app:testDebugUnitTest --console=plain` baseline or equivalent aggregated command that includes U3). Include BR3.* tests with fakes for C2/A2/A3; no live PRISMA/S-ostoslista network in CI; no secrets.
- B) Skip U3 tests in CI.
[Answer]: A

**Q3. Investigational adapters (OQ2–OQ4) in CI/release**
- A) (Recommended) Official PRISMA/S-ostoslista paths remain disabled/fake-backed unless explicitly feasible/confirmed and injected safely; mandatory share/copy and manual store/checklist fallbacks always testable.
- B) Require live PRISMA/S-ostoslista credentials in CI.
[Answer]: A
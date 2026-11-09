# Infrastructure Design Questions — U4 `u4-android-ui-experience`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** Infrastructure Design (construction, per-unit)  
**Date:** 2026-11-09  
**Mode:** Full (autonomous). U4 is an on-device Android UI unit; MVP scope has no MealPlanner backend/cloud infrastructure (NFR3, FR12). Questions below are resolved with explicit MVP assumptions; no AWS/cloud resources are introduced.

## Q1. Deployment strategy for U4 scope?
- A. Android app build artifacts only (debug/release APK/AAB via Gradle/Android Studio), no cloud deployment for U4
- B. Add new cloud backend/services for U4
- C. Hybrid (client + new cloud services)

[Answer]: A (assumed, autonomous). Rationale: MVP is on-device; no backend/account; external integrations remain investigational behind domain/adapters.

## Q2. CI/CD pipeline for U4 changes?
- A. Local/Gradle-verified builds + existing/standard Android CI gates (`:app:testDebugUnitTest`, lint/static checks as configured), layout-explicit; no production cloud promotion
- B. Full multi-environment cloud promotion pipeline with new AWS infrastructure
- C. No CI checks

[Answer]: A (assumed, autonomous). Rationale: matches established project verification command and MVP packaging; no new cloud infra.

## Q3. Monitoring/observability for U4 UI?
- A. On-device/app-level design only: user-safe status/fallback messaging, privacy-safe diagnostics hooks (no raw GPS/secrets/PII), Android vitals/ANR/crash reporting deferred to release process/tooling decision
- B. Provision CloudWatch/AWS monitoring stack for U4
- C. No monitoring design

[Answer]: A (assumed, autonomous). Rationale: NFR3.3 prohibits sensitive leakage; cloud observability stack not in MVP scope.

## Q4. Secrets management for UI/build?
- A. No secrets hardcoded; build/release signing and any future integration credentials managed outside source control via standard Android/CI secret stores if/when needed (OQ1–OQ4 investigational)
- B. Store secrets in repository/UI state
- C. New AWS Secrets Manager/Vault for this unit now

[Answer]: A (assumed, autonomous).

## Ambiguity analysis
No unresolved contradictions for MVP UI unit. Cloud/IaC questions are N/A by scope; assumptions recorded above. Infrastructure artifacts will explicitly state “no AWS/cloud resources provisioned by U4”.

# Scalability Design — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Date:** 2026-11-09

## Upstream
- [scalability-requirements.md](../nfr-requirements/scalability-requirements.md) (NFR1.1–NFR1.3)

## Design

| Concern | Pattern | Addresses | Notes |
|---|---|---|---|
| Scope | On-device library unit; no backend/public API, no horizontal scaling targets. | NFR1.1 | BR3.9 seam supports future extraction without MVP infrastructure. |
| MVP bounds | One active user/device context, one current 7-day locked generation context, weekly ingredient/list scale. | NFR1.2 | History/multi-list/cloud scale out of MVP. |
| Bounded batch work | Presence queries/results operate on current list items or explicit batch; adapter implementations should cap/chunk requests and avoid retry storms if live PRISMA enabled. | NFR1.3 | Exact rate limits depend on OQ2/OQ3 feasibility; contracts already support partial/unknown outcomes. |
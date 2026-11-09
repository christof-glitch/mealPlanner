# Scalability Requirements — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [requirements.md](../../../inception/requirements-analysis/requirements.md) — inception **NFR1 Platform scope**, **NFR5 MVP responsiveness**; no server scalability NFR in MVP.

## Requirements

| ID | Inherits | Requirement | Target / constraint | Notes |
|---|---|---|---|---|
| NFR1.1 | NFR1 | U3 remains Android/on-device scoped. | No backend service, no public API, no cloud scaling target introduced by U3. | Aligns with extraction seam (BR3.9) without adding runtime infrastructure. |
| NFR1.2 | NFR1 | Data/operation scale is MVP-bounded. | Design assumes one active user context on device, one 7-day plan lock/generation context at a time, and ingredient/list sizes typical of weekly meal planning. | Larger catalogs/multi-list/history are post-MVP concerns. |
| NFR1.3 | NFR1 | Presence/export batching is bounded by current list. | `applyPresenceCheckBatch`/A2 query construction operates on current list items or explicit batch inputs; avoid unbounded retry storms. Exact batch chunking/rate controls deferred to adapter implementation if live PRISMA is enabled. | OQ2/OQ3 investigational. |
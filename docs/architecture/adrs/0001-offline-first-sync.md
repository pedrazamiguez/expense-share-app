# ADR-0001: True Offline-First Architecture & Reusable Sync Delegates

* **Status:** Accepted
* **Deciders:** SplitTrip Engineering Team
* **Date:** 2026-08-15
* **Tags:** data, offline-first, room, firestore, sync

---

## Context and Problem Statement

SplitTrip is a shared travel expense tracking app where users frequently operate in environments with intermittent or nonexistent internet connectivity (airports, flights, remote rural areas). Synchronous cloud-first network calls introduce latency, cause operation failures without connectivity, and lead to poor user experience.

How do we guarantee immediate, deterministic local persistence while ensuring reliable, eventual cloud synchronization without duplicate boilerplate across repositories?

## Decision Drivers

* Instant UI responsiveness without waiting for network I/O.
* Deterministic offline operation with zero data loss.
* Elimination of repository boilerplate for cloud synchronization and background job management.
* Strict write-ordering guarantees and clean subcollection deletion handling.

## Considered Options

1. **Cloud-First / Network-First with Local Cache:** UI calls cloud API, updates local database on success.
2. **Ad-Hoc Offline-First:** Each repository writes to Room, launches uncoordinated background coroutines to Firestore.
3. **True Offline-First with Reusable Sync Delegates (Chosen):** UI observes Room only. Repositories write to Room first (generating UUID and timestamp locally), then sync asynchronously to Firestore using centralized sync delegates.

## Decision Outcome

Chosen option: **Option 3 (True Offline-First with Reusable Sync Delegates)**.

### The "True Offline" Write Protocol
1. **Local ID Generation:** Local UUID generation (`UUID.randomUUID().toString()`)—never let Firestore generate document IDs.
2. **Local Metadata Generation:** Generate `createdAt = System.currentTimeMillis()` locally.
3. **Write Order:** Save to Room with `PENDING_SYNC` status first (instant UI update via Room `Flow`), then queue background synchronization via `syncScope.launch { cloudDataSource.upsert(...) }`.
4. **Reusable Sync Delegates (`:data:sync`):**
   - `KeyedSubscriptionTracker`: Tracks and deduplicates cloud snapshot listeners by group key.
   - `subscribeAndReconcile<T>()`: Handles cloud snapshot streams, reconciles Room with `@Transaction` upserts and selective deletes, and executes `confirmPendingSync()`.
   - `syncCreateToCloud()`: Performs background upserts and transitions status from `PENDING_SYNC` to `SYNCED` or `SYNC_FAILED`.
   - `syncDeletionToCloud()`: Executes cloud deletions and cleans up orphaned subcollections.

### Critical Rule: Subcollection Deletion
Firestore does not automatically delete subcollections when a parent document is deleted. If real-time listeners observe a subcollection, subcollection items must be deleted before deleting the parent document.

## Consequences

### Positive
* Zero perceived latency for user operations.
* Full offline capability; pending changes are automatically synced when network returns.
* Repositories remain small, declarative, and maintainable by sharing battle-tested sync delegates.
* Single source of truth for UI is always the local Room database.

### Negative / Trade-offs
* Background coroutines require explicit `CoroutineDispatcher` injection for deterministic unit testing.
* Conflict resolution and reconciliation logic must be carefully maintained in Room transaction mappers.

## References
* [`docs/architecture/patterns/offline-first.md`](../patterns/offline-first.md)
* [`docs/architecture/patterns/core-services-catalog.md`](../patterns/core-services-catalog.md) § G

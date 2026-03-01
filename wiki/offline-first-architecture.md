# Offline-First Architecture & Synchronization

In our application, **Offline Support** is not an afterthought or a "cache"; it is the primary way the app functions. This document outlines the architectural rules ensuring the app remains responsive and consistent, regardless of network connectivity.

## Core Principle: Local Database as the Single Source of Truth

To provide a seamless user experience, the UI **never** observes the network directly. Instead, we follow a strict unidirectional data flow:

1. **Read:** The UI observes the **Local Database** (Room).
2. **Write:** User actions modify the **Local Database** first.
3. **Sync:** A background process synchronizes these changes with the **Cloud** (Firestore).

This ensures that when a user creates a group or adds an expense, the UI updates instantly, even if the device is in Airplane Mode.

## The Creation Flow (Write)

When creating new data (e.g., a Group or Expense), we cannot rely on the server to generate IDs or timestamps, as this would cause items to be missing or unsortable until a sync occurs.

### 1. Generate IDs Locally

We generate unique identifiers (UUIDs) on the client side before saving. This guarantees that the object exists with a known ID immediately.

**Correct Implementation:**

```kotlin
val expenseId = UUID.randomUUID().toString()
val expense = expense.copy(id = expenseId)

```

**Why:** If we let Firestore generate the ID (`.add()`), we would have a temporary local object without an ID, and a future cloud object with a different ID, leading to duplicates during sync.

### 2. Generate Metadata Locally

Timestamps (creation dates) and attribution (User IDs) must be populated locally.

* **Timestamps:** Use `System.currentTimeMillis()` immediately. Do not rely on Firestore `FieldValue.serverTimestamp()` for the local copy, or the item will appear at the wrong position (or not at all) in sorted lists.
* **User ID:** Inject the `AuthenticationService` into your Repository to fetch the current user's ID synchronously.

### 3. Save Local, Then Sync

The repository method should look like this:

1. **Prepare:** Create the object with a local UUID and timestamp.
2. **Local Commit:** Save to Room. The UI updates instantly via the `Flow`.
3. **Cloud Sync:** Launch a background coroutine to upload the data.

```kotlin
// Example Repository Implementation
override suspend fun addExpense(expense: Expense) {
    // 1. Prepare
    val finalExpense = expense.copy(
        id = UUID.randomUUID().toString(),
        createdAt = System.currentTimeMillis()
    )

    // 2. Local Commit (UI updates now)
    localDataSource.saveExpense(finalExpense)

    // 3. Cloud Sync (Background)
    scope.launch {
        cloudDataSource.upsertExpense(finalExpense)
    }
}

```

## The Synchronization Flow (Read)

When fetching data from the cloud, we must ensure we don't accidentally overwrite newer local changes or create duplicates.

### 1. Upsert Strategy (Set vs Add)

Since we generated the ID locally, our Cloud Data Source must use **Upsert** logic (`set` with the specific ID), not `add`.

* **Cloud:** `.document(id).set(data)`
* **Local:** `@Insert(onConflict = OnConflictStrategy.REPLACE)`

### 2. Handling Race Conditions

A common "flicker" bug occurs when the app downloads an old list from the cloud and overwrites a list containing a brand-new local item.

To prevent this:

* Use `OnConflictStrategy.REPLACE` when saving synced data.
* **Do not** delete the entire local table before inserting cloud data. Only insert/update the items returned from the cloud. This leaves your unsynced local items intact.

## Real-Time Multi-Device Sync

While we are Offline-First, this is a **multi-user, multi-device** app. Changes made by one user (or on one device) must eventually propagate to all other users/devices that share the same data. We achieve this using **Firestore Snapshot Listeners** that feed into Room, keeping the local-first flow intact.

### The Pattern: Snapshot Listener → Room Reconciliation

The Repository's `get*Flow()` method subscribes to a real-time Firestore `snapshotListener` via `onStart`. This listener fires whenever **any** user adds, modifies, or deletes data in the watched collection. Each snapshot represents the **complete authoritative state** of the collection at that moment.

```kotlin
// Repository
override fun getGroupExpensesFlow(groupId: String): Flow<List<Expense>> {
    return localDataSource.getExpensesByGroupIdFlow(groupId) // UI observes Room
        .onStart {
            // Cancel any previous listener to prevent duplicates
            cloudSubscriptionJobs[groupId]?.cancel()
            cloudSubscriptionJobs[groupId] = syncScope.launch {
                subscribeToCloudChanges(groupId) // Persistent Firestore listener
            }
        }
}

private suspend fun subscribeToCloudChanges(groupId: String) {
    cloudDataSource.getExpensesByGroupIdFlow(groupId)
        .collect { remoteExpenses ->
            // Atomic replace: delete stale + insert fresh in a @Transaction
            localDataSource.replaceExpensesForGroup(groupId, remoteExpenses)
        }
}
```

**How it works:**
1. The UI subscribes to the **Room Flow** (instant, offline data).
2. `onStart` launches a **persistent Firestore snapshot listener** in a background scope.
3. When the listener fires (new data from any user/device), the Repository atomically replaces the local data for that scope using a Room `@Transaction`.
4. The Room Flow **re-emits automatically**, updating the UI in near real-time.

### ⚠️ Preventing Duplicate Snapshot Listeners

Because `onStart` fires every time the Flow gets a new collector (config changes, `WhileSubscribed` resubscriptions, `flatMapLatest` restarts), launching a new listener in `syncScope` without cancelling the previous one would **leak** Firestore snapshot listeners. Each leaked listener keeps performing `replaceAll` writes to Room, wasting resources and causing duplicate reconciliation loops.

**The Rule:** Track the cloud subscription as a `Job` and cancel it before launching a new one.

```kotlin
// ❌ BAD - Leaks a new listener on every resubscription
syncScope.launch { subscribeToCloudChanges() }

// ✅ GOOD - At most one active listener at any time
cloudSubscriptionJob?.cancel()
cloudSubscriptionJob = syncScope.launch { subscribeToCloudChanges() }
```

For repositories with **keyed** subscriptions (e.g., expenses per group), use a `ConcurrentHashMap<String, Job>` to track one listener per key:

```kotlin
private val cloudSubscriptionJobs = ConcurrentHashMap<String, Job>()

// In onStart:
cloudSubscriptionJobs[groupId]?.cancel()
cloudSubscriptionJobs[groupId] = syncScope.launch {
    subscribeToCloudChanges(groupId)
}
```

### Atomic Reconciliation: `replaceAll` in a `@Transaction`

Because each Firestore snapshot represents the **complete** current state, we use an atomic **delete + insert** strategy in Room:

```kotlin
// Room DAO
@Transaction
open suspend fun replaceExpensesForGroup(groupId: String, expenses: List<ExpenseEntity>) {
    deleteByGroupId(groupId)
    insertAll(expenses)
}
```

This handles all cases in a single operation:
- **Additions** by other users → new items appear locally.
- **Deletions** by other users → stale items are removed locally.
- **Modifications** by other users → items are updated locally.

> ⚠️ **This `replaceAll` strategy is safe here** because the data comes from the authoritative Firestore snapshot, not a partial sync. This is distinct from the one-shot sync (Section "Handling Race Conditions" above), where we use upsert-only to avoid overwriting unsynced local changes.

### 🛑 Critical: Subcollection Cleanup on Deletion

Firestore does **NOT** automatically delete subcollections when a parent document is deleted. If your real-time listener watches a **subcollection** (e.g., `group_members`) to determine data membership, you **MUST** explicitly delete those subcollection documents before deleting the parent.

**Example: Group Deletion**

The group real-time listener watches `group_members` collectionGroup to know which groups the user belongs to. If we only delete the group document, the orphaned member documents remain, and the listener on other devices **never fires** — other users continue to see the deleted group.

```kotlin
// ❌ BAD - Only deletes parent, orphans subcollection
override suspend fun deleteGroup(groupId: String) {
    firestore.collection("groups").document(groupId).delete().await()
    // group_members subcollection still exists → other devices still "see" this group
}

// ✅ GOOD - Deletes subcollection FIRST, then parent
override suspend fun deleteGroup(groupId: String) {
    // 1. Delete all member documents → triggers listener on other devices
    val membersCollection = firestore.collection("groups/$groupId/members")
    val memberDocs = membersCollection.get().await()
    memberDocs.documents.forEach { doc ->
        doc.reference.delete().await()
    }

    // 2. Delete the group document itself
    firestore.collection("groups").document(groupId).delete().await()
}
```

**Rule:** When deleting a document that has subcollections used by real-time listeners, always delete the subcollection documents **first** so the listener fires and propagates the deletion to all devices.

### Firestore Data Source: Cache-First Loading

The Firestore `CloudDataSource` implementations use a **cache-first** strategy inside the snapshot listener callback:

1. **Cache hit:** Load documents from Firestore's local cache (instant, no network).
2. **Cache miss:** Fall back to server fetch for any documents not yet cached.

This minimizes network round-trips while ensuring data completeness.

## Summary Checklist

When implementing a new feature, verify these points:

* [ ] **No Network Block:** Does the repository save to Room *before* making any network call?
* [ ] **Local IDs:** Is the ID generated using `UUID` in the Repository (or UseCase)?
* [ ] **Local Dates:** Is `System.currentTimeMillis()` used for the creation date?
* [ ] **Conflict Resolution:** Does the DAO use `OnConflictStrategy.REPLACE`?
* [ ] **No Blind Deletes:** Ensure the sync process doesn't wipe unsynced local items.
* [ ] **Real-Time Listener:** Does the Repository subscribe to a Firestore snapshot listener via `onStart` for multi-user/multi-device sync?
* [ ] **Single Subscription:** Is the cloud listener tracked as a `Job` and cancelled before re-launching to prevent duplicate listeners?
* [ ] **Atomic Reconciliation:** Does the local data source use `@Transaction` (delete + insert) when reconciling snapshot data?
* [ ] **Subcollection Cleanup:** When deleting a parent document, are subcollection documents deleted first to trigger listeners on other devices?

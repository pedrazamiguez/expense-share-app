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

The Repository's `get*Flow()` method uses `channelFlow` to subscribe to a real-time Firestore `snapshotListener` concurrently with the local Room flow. This listener fires whenever **any** user adds, modifies, or deletes data in the watched collection. Tying the cloud subscription to the `channelFlow` ensures it is cancelled when the consumer stops collecting (e.g., on a group switch), preventing orphan snapshot listeners.

```kotlin
// Repository
override fun getGroupExpensesFlow(groupId: String): Flow<List<Expense>> = channelFlow {
    // Forward local Room data to the consumer (Single Source of Truth)
    launch {
        localDataSource.getExpensesByGroupIdFlow(groupId).collect { send(it) }
    }

    // Cloud subscription — lifecycle tied to this flow's collection
    try {
        cloudDataSource.getExpensesByGroupIdFlow(groupId)
            .collect { remoteExpenses ->
                localDataSource.replaceExpensesForGroup(groupId, remoteExpenses)
            }
    } catch (e: Exception) {
        Timber.w(e, "Cloud subscription failed, using local cache")
    }
}
```

**How it works:**
1. The UI subscribes to the **channelFlow** which forwards **Room Flow** data (instant, offline).
2. Concurrently, a **persistent Firestore snapshot listener** runs in the same scope.
3. When the listener fires (new data from any user/device), the Repository upserts the remote data into Room.
4. The Room Flow **re-emits automatically**, updating the UI in near real-time.
5. When the consumer cancels (e.g., group switch), **both** the local and cloud subscriptions are cancelled together.

### Incremental Reconciliation: Upsert (Not Delete + Insert)

When reconciling Room with a cloud snapshot, we use an **incremental upsert** strategy rather than a destructive delete + insert. This preserves any locally-created or locally-modified expenses that have not yet synced to the cloud.

```kotlin
// Room DAO
@Transaction
suspend fun replaceExpensesForGroup(groupId: String, expenses: List<ExpenseEntity>) {
    // Non-destructive: upsert only, never wipe local-pending rows
    insertExpenses(expenses)
}
```

> ⚠️ **Trade-off:** With upsert-only reconciliation, deletions made by other users will not automatically remove the corresponding local row. Those deletions are propagated explicitly via the write protocol (the deleting user's app calls `deleteExpense()` locally first, then syncs to Firestore, which triggers the snapshot listener on other devices — but those devices only upsert, not delete). A full incremental diff (apply adds/modifies/removes by ID) or a local `pendingSync` dirty flag would be required to handle remote deletions perfectly in all offline scenarios.

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
* [ ] **Atomic Reconciliation:** Does the local data source use `@Transaction` (delete + insert) when reconciling snapshot data?
* [ ] **Subcollection Cleanup:** When deleting a parent document, are subcollection documents deleted first to trigger listeners on other devices?

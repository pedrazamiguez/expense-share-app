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

## Summary Checklist

When implementing a new feature, verify these points:

* [ ] **No Network Block:** Does the repository save to Room *before* making any network call?
* [ ] **Local IDs:** Is the ID generated using `UUID` in the Repository (or UseCase)?
* [ ] **Local Dates:** Is `System.currentTimeMillis()` used for the creation date?
* [ ] **Conflict Resolution:** Does the DAO use `OnConflictStrategy.REPLACE`?
* [ ] **No Blind Deletes:** Ensure the sync process doesn't wipe unsynced local items.

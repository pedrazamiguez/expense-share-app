## Offline-First Sync Delegates

New repositories MUST use reusable sync delegates: `KeyedSubscriptionTracker`, `subscribeAndReconcile`, `syncCreateToCloud`, `syncDeletionToCloud`. Do not duplicate sync boilerplate.

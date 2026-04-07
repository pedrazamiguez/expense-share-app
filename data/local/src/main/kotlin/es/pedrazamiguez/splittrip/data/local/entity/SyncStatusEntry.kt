package es.pedrazamiguez.splittrip.data.local.entity

/**
 * Lightweight projection for querying sync status metadata.
 * Used by DAO @Transaction methods to preserve non-SYNCED statuses
 * during cloud snapshot reconciliation.
 */
data class SyncStatusEntry(val id: String, val syncStatus: String)

package es.pedrazamiguez.splittrip.domain.enums

/**
 * Tracks the cloud synchronization state of a locally-stored entity.
 *
 * Used exclusively as local-only metadata — NEVER serialized to Firestore.
 * Room entities store this as a TEXT column; the domain model uses the enum directly.
 *
 * Defaults to [SYNCED] so that:
 * - Existing data (which arrived via cloud snapshot reconciliation) is treated as synced.
 * - Items arriving from Firestore snapshots are always marked [SYNCED].
 */
enum class SyncStatus {
    /** Successfully synced to cloud. */
    SYNCED,

    /** Saved locally, cloud sync not yet attempted or in progress. */
    PENDING_SYNC,

    /** Cloud sync was attempted and failed. */
    SYNC_FAILED;

    companion object {
        fun fromString(value: String): SyncStatus = entries.find { it.name == value }
            ?: throw IllegalArgumentException("Unknown SyncStatus: $value")

        fun fromStringOrDefault(value: String?): SyncStatus =
            value?.let { entries.find { entry -> entry.name == it } } ?: SYNCED
    }
}

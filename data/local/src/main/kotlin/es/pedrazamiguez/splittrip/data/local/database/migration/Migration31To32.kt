package es.pedrazamiguez.splittrip.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Fixes settlement_records table for devices that ran Migration30To31 with:
 *   - Wrong index name prefix (`idx_` instead of Room's auto-generated `index_`)
 *   - A `DEFAULT 'PENDING_SYNC'` baked into `syncStatus` DDL
 *
 * The table is dropped and recreated because SQLite cannot rename indexes or
 * remove column defaults without recreating the table. Safe to drop: this table
 * was introduced in v31 with no UI that can create records yet.
 */
internal val MIGRATION_31_32 = object : Migration(31, 32) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Drop table created with wrong index names / stale column default
        db.execSQL("DROP TABLE IF EXISTS `settlement_records`")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `settlement_records` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `groupId` TEXT NOT NULL,
                `fromUserId` TEXT NOT NULL,
                `toUserId` TEXT NOT NULL,
                `amountCents` INTEGER NOT NULL,
                `currency` TEXT NOT NULL,
                `sourcePocket` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `syncStatus` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `confirmedByPayerAt` INTEGER,
                `confirmedByPayeeAt` INTEGER,
                `resolvedAt` INTEGER,
                `disputedBy` TEXT,
                `disputeReason` TEXT
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_settlement_records_groupId` ON `settlement_records` (`groupId`)")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_settlement_records_fromUserId` ON `settlement_records` (`fromUserId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_settlement_records_toUserId` ON `settlement_records` (`toUserId`)"
        )
    }
}

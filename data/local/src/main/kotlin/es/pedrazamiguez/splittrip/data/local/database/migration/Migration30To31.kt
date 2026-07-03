package es.pedrazamiguez.splittrip.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_30_31 = object : Migration(30, 31) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS settlement_records (
                id TEXT NOT NULL PRIMARY KEY,
                groupId TEXT NOT NULL,
                fromUserId TEXT NOT NULL,
                toUserId TEXT NOT NULL,
                amountCents INTEGER NOT NULL,
                currency TEXT NOT NULL,
                sourcePocket TEXT NOT NULL,
                status TEXT NOT NULL,
                syncStatus TEXT NOT NULL DEFAULT 'PENDING_SYNC',
                createdAt INTEGER NOT NULL,
                confirmedByPayerAt INTEGER,
                confirmedByPayeeAt INTEGER,
                resolvedAt INTEGER,
                disputedBy TEXT,
                disputeReason TEXT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_settlement_records_groupId ON settlement_records(groupId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_settlement_records_fromUserId ON settlement_records(fromUserId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_settlement_records_toUserId ON settlement_records(toUserId)")
    }
}

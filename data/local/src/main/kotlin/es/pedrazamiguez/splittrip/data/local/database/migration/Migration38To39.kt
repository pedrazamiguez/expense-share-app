package es.pedrazamiguez.splittrip.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_38_39 = object : Migration(38, 39) {
    @Suppress("LongMethod")
    override fun migrate(db: SupportSQLiteDatabase) {
        val cursor = db.query("PRAGMA table_info('contributions')")
        val columns = mutableListOf<String>()
        while (cursor.moveToNext()) {
            columns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
        }
        cursor.close()

        val hasLinkedSettlementId = columns.contains("linkedSettlementId")
        val hasEquivalentBaseAmount = columns.contains("equivalentBaseAmount")
        val hasExchangeRate = columns.contains("exchangeRate")

        if (!hasLinkedSettlementId) {
            db.execSQL("ALTER TABLE `contributions` ADD COLUMN `linkedSettlementId` TEXT DEFAULT null")
        }
        if (!hasEquivalentBaseAmount) {
            db.execSQL("ALTER TABLE `contributions` ADD COLUMN `equivalentBaseAmount` INTEGER DEFAULT null")
        }
        if (!hasExchangeRate) {
            db.execSQL("ALTER TABLE `contributions` ADD COLUMN `exchangeRate` TEXT DEFAULT null")
        }

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `_new_contributions` (
                `id` TEXT NOT NULL,
                `groupId` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `createdBy` TEXT NOT NULL,
                `contributionScope` TEXT NOT NULL,
                `subunitId` TEXT,
                `linkedExpenseId` TEXT,
                `linkedSettlementId` TEXT DEFAULT null,
                `amount` INTEGER NOT NULL,
                `currency` TEXT NOT NULL,
                `equivalentBaseAmount` INTEGER DEFAULT null,
                `exchangeRate` TEXT DEFAULT null,
                `createdAtMillis` INTEGER,
                `lastUpdatedAtMillis` INTEGER,
                `syncStatus` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`groupId`) REFERENCES `groups`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `_new_contributions` (
                `id`,`groupId`,`userId`,`createdBy`,`contributionScope`,`subunitId`,`linkedExpenseId`,
                `linkedSettlementId`,`amount`,`currency`,`equivalentBaseAmount`,`exchangeRate`,
                `createdAtMillis`,`lastUpdatedAtMillis`,`syncStatus`
            ) SELECT
                `id`,`groupId`,`userId`,`createdBy`,`contributionScope`,`subunitId`,`linkedExpenseId`,
                `linkedSettlementId`,`amount`,`currency`,`equivalentBaseAmount`,`exchangeRate`,
                `createdAtMillis`,`lastUpdatedAtMillis`,`syncStatus`
            FROM `contributions`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `contributions`")
        db.execSQL("ALTER TABLE `_new_contributions` RENAME TO `contributions`")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_contributions_groupId` ON `contributions` (`groupId`)"
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_contributions_groupId_linkedExpenseId`
            ON `contributions` (`groupId`, `linkedExpenseId`)
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_contributions_groupId_syncStatus`
            ON `contributions` (`groupId`, `syncStatus`)
            """.trimIndent()
        )
    }
}

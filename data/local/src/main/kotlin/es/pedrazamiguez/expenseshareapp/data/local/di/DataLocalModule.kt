package es.pedrazamiguez.expenseshareapp.data.local.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import es.pedrazamiguez.expenseshareapp.data.local.dao.CashWithdrawalDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ContributionDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.CurrencyDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ExchangeRateDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ExpenseDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ExpenseSplitDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.GroupDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.SubunitDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.UserDao
import es.pedrazamiguez.expenseshareapp.data.local.database.AppDatabase
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalCashWithdrawalDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalContributionDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalCurrencyDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalExpenseDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalGroupDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalSubunitDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalUserDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCashWithdrawalDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalContributionDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalSubunitDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalUserDataSource
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `groups` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT,
                `currencyCode` TEXT NOT NULL,
                `extraCurrencies` TEXT NOT NULL,
                `memberIds` TEXT NOT NULL,
                `mainImagePath` TEXT,
                `createdAtMillis` INTEGER,
                `lastUpdatedAtMillis` INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `expenses` (
                `id` TEXT NOT NULL,
                `groupId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `sourceAmount` INTEGER NOT NULL,
                `sourceCurrency` TEXT NOT NULL,
                `sourceTipAmount` INTEGER NOT NULL,
                `sourceFeeAmount` INTEGER NOT NULL,
                `groupAmount` INTEGER NOT NULL,
                `groupCurrency` TEXT NOT NULL,
                `exchangeRate` REAL NOT NULL,
                `paymentMethod` TEXT NOT NULL,
                `createdBy` TEXT NOT NULL,
                `payerType` TEXT NOT NULL,
                `createdAtMillis` INTEGER,
                `lastUpdatedAtMillis` INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_groupId` ON `expenses` (`groupId`)")
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // SQLite doesn't support adding foreign keys to existing tables
        // We need to recreate the table with the foreign key constraint

        // 1. Create new expenses table with foreign key constraint
        db.execSQL(
            """
            CREATE TABLE `expenses_new` (
                `id` TEXT NOT NULL,
                `groupId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `sourceAmount` INTEGER NOT NULL,
                `sourceCurrency` TEXT NOT NULL,
                `sourceTipAmount` INTEGER NOT NULL,
                `sourceFeeAmount` INTEGER NOT NULL,
                `groupAmount` INTEGER NOT NULL,
                `groupCurrency` TEXT NOT NULL,
                `exchangeRate` REAL NOT NULL,
                `paymentMethod` TEXT NOT NULL,
                `createdBy` TEXT NOT NULL,
                `payerType` TEXT NOT NULL,
                `createdAtMillis` INTEGER,
                `lastUpdatedAtMillis` INTEGER,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // 2. Copy data from old table to new table (explicit column listing for safety)
        db.execSQL(
            """
            INSERT INTO `expenses_new` (
                `id`, `groupId`, `title`, `sourceAmount`, `sourceCurrency`,
                `sourceTipAmount`, `sourceFeeAmount`, `groupAmount`, `groupCurrency`,
                `exchangeRate`, `paymentMethod`, `createdBy`, `payerType`,
                `createdAtMillis`, `lastUpdatedAtMillis`
            )
            SELECT
                `id`, `groupId`, `title`, `sourceAmount`, `sourceCurrency`,
                `sourceTipAmount`, `sourceFeeAmount`, `groupAmount`, `groupCurrency`,
                `exchangeRate`, `paymentMethod`, `createdBy`, `payerType`,
                `createdAtMillis`, `lastUpdatedAtMillis`
            FROM `expenses`
            """.trimIndent()
        )

        // 3. Drop old table
        db.execSQL("DROP TABLE `expenses`")

        // 4. Rename new table to original name
        db.execSQL("ALTER TABLE `expenses_new` RENAME TO `expenses`")

        // 5. Recreate the index (existed in version 3, see MIGRATION_2_3)
        db.execSQL("CREATE INDEX `index_expenses_groupId` ON `expenses` (`groupId`)")
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `contributions` (
                `id` TEXT NOT NULL,
                `groupId` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `amount` INTEGER NOT NULL,
                `currency` TEXT NOT NULL,
                `createdAtMillis` INTEGER,
                `lastUpdatedAtMillis` INTEGER,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX `index_contributions_groupId` ON `contributions` (`groupId`)")
    }
}

private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create cash_withdrawals table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cash_withdrawals` (
                `id` TEXT NOT NULL,
                `groupId` TEXT NOT NULL,
                `withdrawnBy` TEXT NOT NULL,
                `amountWithdrawn` INTEGER NOT NULL,
                `remainingAmount` INTEGER NOT NULL,
                `currency` TEXT NOT NULL,
                `deductedBaseAmount` INTEGER NOT NULL,
                `exchangeRate` REAL NOT NULL,
                `createdAtMillis` INTEGER,
                `lastUpdatedAtMillis` INTEGER,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX `index_cash_withdrawals_groupId` ON `cash_withdrawals` (`groupId`)")

        // 2. Add cashTranchesJson column to expenses table
        // SQLite supports ALTER TABLE ADD COLUMN for nullable columns without defaults
        db.execSQL("ALTER TABLE `expenses` ADD COLUMN `cashTranchesJson` TEXT")
    }
}

private val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `expenses` ADD COLUMN `category` TEXT")
        db.execSQL("ALTER TABLE `expenses` ADD COLUMN `vendor` TEXT")
        db.execSQL("ALTER TABLE `expenses` ADD COLUMN `paymentStatus` TEXT")
        db.execSQL("ALTER TABLE `expenses` ADD COLUMN `dueDateMillis` INTEGER")
        db.execSQL("ALTER TABLE `expenses` ADD COLUMN `receiptLocalUri` TEXT")
    }
}

/**
 * Migration 7 → 8: Convert exchangeRate from REAL (Double) to TEXT (String/BigDecimal)
 * in both `expenses` and `cash_withdrawals` tables.
 *
 * SQLite does not support ALTER COLUMN, so we recreate each table with the new schema,
 * copy data (casting REAL to TEXT), drop the old table, and rename.
 */
private val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ── expenses table ──────────────────────────────────────────────
        db.execSQL(
            """
            CREATE TABLE `expenses_new` (
                `id` TEXT NOT NULL,
                `groupId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `sourceAmount` INTEGER NOT NULL,
                `sourceCurrency` TEXT NOT NULL,
                `sourceTipAmount` INTEGER NOT NULL,
                `sourceFeeAmount` INTEGER NOT NULL,
                `groupAmount` INTEGER NOT NULL,
                `groupCurrency` TEXT NOT NULL,
                `exchangeRate` TEXT NOT NULL,
                `category` TEXT,
                `vendor` TEXT,
                `paymentMethod` TEXT NOT NULL,
                `paymentStatus` TEXT,
                `dueDateMillis` INTEGER,
                `receiptLocalUri` TEXT,
                `createdBy` TEXT NOT NULL,
                `payerType` TEXT NOT NULL,
                `createdAtMillis` INTEGER,
                `lastUpdatedAtMillis` INTEGER,
                `cashTranchesJson` TEXT,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `expenses_new` (
                `id`, `groupId`, `title`, `sourceAmount`, `sourceCurrency`,
                `sourceTipAmount`, `sourceFeeAmount`, `groupAmount`, `groupCurrency`,
                `exchangeRate`, `category`, `vendor`, `paymentMethod`, `paymentStatus`,
                `dueDateMillis`, `receiptLocalUri`, `createdBy`, `payerType`,
                `createdAtMillis`, `lastUpdatedAtMillis`, `cashTranchesJson`
            )
            SELECT
                `id`, `groupId`, `title`, `sourceAmount`, `sourceCurrency`,
                `sourceTipAmount`, `sourceFeeAmount`, `groupAmount`, `groupCurrency`,
                CAST(`exchangeRate` AS TEXT), `category`, `vendor`, `paymentMethod`, `paymentStatus`,
                `dueDateMillis`, `receiptLocalUri`, `createdBy`, `payerType`,
                `createdAtMillis`, `lastUpdatedAtMillis`, `cashTranchesJson`
            FROM `expenses`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `expenses`")
        db.execSQL("ALTER TABLE `expenses_new` RENAME TO `expenses`")
        db.execSQL("CREATE INDEX `index_expenses_groupId` ON `expenses` (`groupId`)")

        // ── cash_withdrawals table ──────────────────────────────────────
        db.execSQL(
            """
            CREATE TABLE `cash_withdrawals_new` (
                `id` TEXT NOT NULL,
                `groupId` TEXT NOT NULL,
                `withdrawnBy` TEXT NOT NULL,
                `amountWithdrawn` INTEGER NOT NULL,
                `remainingAmount` INTEGER NOT NULL,
                `currency` TEXT NOT NULL,
                `deductedBaseAmount` INTEGER NOT NULL,
                `exchangeRate` TEXT NOT NULL,
                `createdAtMillis` INTEGER,
                `lastUpdatedAtMillis` INTEGER,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `cash_withdrawals_new` (
                `id`, `groupId`, `withdrawnBy`, `amountWithdrawn`, `remainingAmount`,
                `currency`, `deductedBaseAmount`, `exchangeRate`,
                `createdAtMillis`, `lastUpdatedAtMillis`
            )
            SELECT
                `id`, `groupId`, `withdrawnBy`, `amountWithdrawn`, `remainingAmount`,
                `currency`, `deductedBaseAmount`, CAST(`exchangeRate` AS TEXT),
                `createdAtMillis`, `lastUpdatedAtMillis`
            FROM `cash_withdrawals`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `cash_withdrawals`")
        db.execSQL("ALTER TABLE `cash_withdrawals_new` RENAME TO `cash_withdrawals`")
        db.execSQL("CREATE INDEX `index_cash_withdrawals_groupId` ON `cash_withdrawals` (`groupId`)")
    }
}

private val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add splitType column to expenses table
        db.execSQL(
            "ALTER TABLE `expenses` ADD COLUMN `splitType` TEXT NOT NULL DEFAULT 'EQUAL'"
        )

        // Create expense_splits table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `expense_splits` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `expenseId` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `amountCents` INTEGER NOT NULL,
                `percentage` TEXT,
                `isExcluded` INTEGER NOT NULL,
                `isCoveredById` TEXT,
                FOREIGN KEY(`expenseId`) REFERENCES `expenses`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expense_splits_expenseId` ON `expense_splits` (`expenseId`)")
    }
}

private val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `expenses` ADD COLUMN `notes` TEXT DEFAULT NULL")
    }
}

private val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `users` (
                `userId` TEXT NOT NULL,
                `email` TEXT NOT NULL,
                `displayName` TEXT,
                `profileImagePath` TEXT,
                `lastUpdatedAtMillis` INTEGER,
                PRIMARY KEY(`userId`)
            )
            """.trimIndent()
        )
    }
}

private val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `users` ADD COLUMN `createdAtMillis` INTEGER DEFAULT NULL")
    }
}

private val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `subunits` (
                `id` TEXT NOT NULL,
                `groupId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `memberIds` TEXT NOT NULL,
                `memberShares` TEXT NOT NULL,
                `createdBy` TEXT NOT NULL,
                `createdAtMillis` INTEGER,
                `lastUpdatedAtMillis` INTEGER,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_subunits_groupId` ON `subunits` (`groupId`)")
    }
}

/**
 * No-op migration: memberShares is stored as TEXT (JSON), e.g. {"u1":0.5,"u2":0.5}.
 * The new StringBigDecimalMapConverter parses the same format — BigDecimal("0.5")
 * reads the existing serialized values correctly. No data transformation required.
 * Version bump is needed because the TypeConverter class reference changed
 * from StringDoubleMapConverter to StringBigDecimalMapConverter.
 */
private val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // No-op: existing TEXT column data is compatible with BigDecimal parsing
    }
}

/**
 * Adds optional subunitId column to contributions table.
 * When non-null, the contribution was made on behalf of a sub-unit.
 */
private val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE contributions ADD COLUMN subunitId TEXT DEFAULT NULL")
    }
}

/**
 * Adds withdrawalScope and subunitId columns to cash_withdrawals table.
 * withdrawalScope defaults to 'GROUP' for backward compatibility.
 * subunitId is only set when withdrawalScope is 'SUBUNIT'.
 */
private val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE cash_withdrawals ADD COLUMN withdrawalScope TEXT NOT NULL DEFAULT 'GROUP'")
        db.execSQL("ALTER TABLE cash_withdrawals ADD COLUMN subunitId TEXT DEFAULT NULL")
    }
}

/**
 * Adds subunitId column to expense_splits table.
 * When non-null, indicates the user's split belongs to a sub-unit entity.
 */
private val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expense_splits ADD COLUMN subunitId TEXT DEFAULT NULL")
    }
}

/**
 * Adds `contributionScope` column to contributions table.
 * Defaults to 'USER' for individual contributions and infers 'SUBUNIT'
 * for existing contributions that have a non-null subunitId.
 */
private val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE contributions ADD COLUMN contributionScope TEXT NOT NULL DEFAULT 'USER'")
        db.execSQL("UPDATE contributions SET contributionScope = 'SUBUNIT' WHERE subunitId IS NOT NULL")
    }
}

/**
 * 1. Recreates `expenses` table to drop unused `sourceTipAmount` / `sourceFeeAmount` columns
 *    and add `addOnsJson` for the new structured add-ons model.
 * 2. Adds `addOnsJson` column to `cash_withdrawals` via ALTER TABLE.
 */
private val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ── 1. Recreate expenses table ───────────────────────────────────
        db.execSQL(
            """
            CREATE TABLE `expenses_new` (
                `id` TEXT NOT NULL,
                `groupId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `sourceAmount` INTEGER NOT NULL,
                `sourceCurrency` TEXT NOT NULL,
                `groupAmount` INTEGER NOT NULL,
                `groupCurrency` TEXT NOT NULL,
                `exchangeRate` TEXT NOT NULL,
                `category` TEXT,
                `vendor` TEXT,
                `notes` TEXT,
                `paymentMethod` TEXT NOT NULL,
                `paymentStatus` TEXT,
                `dueDateMillis` INTEGER,
                `receiptLocalUri` TEXT,
                `createdBy` TEXT NOT NULL,
                `payerType` TEXT NOT NULL,
                `splitType` TEXT NOT NULL DEFAULT 'EQUAL',
                `createdAtMillis` INTEGER,
                `lastUpdatedAtMillis` INTEGER,
                `cashTranchesJson` TEXT,
                `addOnsJson` TEXT,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `expenses_new` (
                `id`, `groupId`, `title`, `sourceAmount`, `sourceCurrency`,
                `groupAmount`, `groupCurrency`, `exchangeRate`,
                `category`, `vendor`, `notes`, `paymentMethod`, `paymentStatus`,
                `dueDateMillis`, `receiptLocalUri`, `createdBy`, `payerType`, `splitType`,
                `createdAtMillis`, `lastUpdatedAtMillis`, `cashTranchesJson`
            )
            SELECT
                `id`, `groupId`, `title`, `sourceAmount`, `sourceCurrency`,
                `groupAmount`, `groupCurrency`, `exchangeRate`,
                `category`, `vendor`, `notes`, `paymentMethod`, `paymentStatus`,
                `dueDateMillis`, `receiptLocalUri`, `createdBy`, `payerType`, `splitType`,
                `createdAtMillis`, `lastUpdatedAtMillis`, `cashTranchesJson`
            FROM `expenses`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `expenses`")
        db.execSQL("ALTER TABLE `expenses_new` RENAME TO `expenses`")
        db.execSQL("CREATE INDEX `index_expenses_groupId` ON `expenses` (`groupId`)")

        // ── 2. Add addOnsJson to cash_withdrawals ───────────────────────
        db.execSQL("ALTER TABLE `cash_withdrawals` ADD COLUMN `addOnsJson` TEXT DEFAULT NULL")
    }
}

/**
 * Adds `title`, `notes`, and `receiptLocalUri` nullable columns to `cash_withdrawals`.
 * These are optional metadata fields for annotating where/why cash was obtained.
 */
private val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `cash_withdrawals` ADD COLUMN `title` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `cash_withdrawals` ADD COLUMN `notes` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `cash_withdrawals` ADD COLUMN `receiptLocalUri` TEXT DEFAULT NULL")
    }
}

val dataLocalModule = module {

    single {
        UserPreferences(
            context = androidContext(),
            authenticationService = get<AuthenticationService>()
        )
    }

    single<AppDatabase> {
        Room
            .databaseBuilder(
                context = get<Application>(),
                klass = AppDatabase::class.java,
                name = "expense_share_db"
            )
            .addMigrations(
                MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
                MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10,
                MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                MIGRATION_13_14,
                MIGRATION_14_15,
                MIGRATION_15_16,
                MIGRATION_16_17,
                MIGRATION_17_18,
                MIGRATION_18_19,
                MIGRATION_19_20
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Enable foreign key constraints for all connections
                    db.execSQL("PRAGMA foreign_keys=ON")
                }
            })
            .build()
    }

    single<CurrencyDao> { get<AppDatabase>().currencyDao() }

    single<ExchangeRateDao> { get<AppDatabase>().exchangeRateDao() }

    single<GroupDao> { get<AppDatabase>().groupDao() }

    single<ExpenseDao> { get<AppDatabase>().expenseDao() }

    single<ExpenseSplitDao> { get<AppDatabase>().expenseSplitDao() }

    single<ContributionDao> { get<AppDatabase>().contributionDao() }

    single<CashWithdrawalDao> { get<AppDatabase>().cashWithdrawalDao() }

    single<LocalCurrencyDataSource> {
        LocalCurrencyDataSourceImpl(
            currencyDao = get<CurrencyDao>(),
            exchangeRateDao = get<ExchangeRateDao>()
        )
    }

    single<LocalGroupDataSource> {
        LocalGroupDataSourceImpl(
            groupDao = get<GroupDao>()
        )
    }

    single<LocalExpenseDataSource> {
        LocalExpenseDataSourceImpl(
            appDatabase = get<AppDatabase>(),
            expenseDao = get<ExpenseDao>(),
            expenseSplitDao = get<ExpenseSplitDao>()
        )
    }

    single<LocalContributionDataSource> {
        LocalContributionDataSourceImpl(
            contributionDao = get<ContributionDao>()
        )
    }

    single<LocalCashWithdrawalDataSource> {
        LocalCashWithdrawalDataSourceImpl(
            cashWithdrawalDao = get<CashWithdrawalDao>()
        )
    }

    single<UserDao> { get<AppDatabase>().userDao() }

    single<SubunitDao> { get<AppDatabase>().subunitDao() }

    single<LocalUserDataSource> {
        LocalUserDataSourceImpl(
            userDao = get<UserDao>()
        )
    }

    single<LocalSubunitDataSource> {
        LocalSubunitDataSourceImpl(
            subunitDao = get<SubunitDao>()
        )
    }
}

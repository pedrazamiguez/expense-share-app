package es.pedrazamiguez.expenseshareapp.data.local.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import es.pedrazamiguez.expenseshareapp.data.local.dao.CurrencyDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ExchangeRateDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ExpenseDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.GroupDao
import es.pedrazamiguez.expenseshareapp.data.local.database.AppDatabase
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalCurrencyDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalExpenseDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalGroupDataSourceImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalGroupDataSource
import es.pedrazamiguez.expenseshareapp.data.local.datastore.UserPreferences
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

val dataLocalModule = module {

    single { UserPreferences(androidContext()) }

    single<AppDatabase> {
        Room
            .databaseBuilder(
                context = get<Application>(),
                klass = AppDatabase::class.java,
                name = "expense_share_db"
            )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
            expenseDao = get<ExpenseDao>()
        )
    }

}

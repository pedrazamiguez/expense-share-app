package es.pedrazamiguez.expenseshareapp.data.local.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import es.pedrazamiguez.expenseshareapp.data.local.dao.CurrencyDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ExchangeRateDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.GroupDao
import es.pedrazamiguez.expenseshareapp.data.local.database.AppDatabase
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalCurrencyDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalGroupDataSourceImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalGroupDataSource
import org.koin.dsl.module

/**
 * Migration from version 1 to 2: Add groups table
 */
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

val dataLocalModule = module {

    single<AppDatabase> {
        Room
            .databaseBuilder(
                context = get<Application>(),
                klass = AppDatabase::class.java,
                name = "expense_share_db"
            )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    single<CurrencyDao> { get<AppDatabase>().currencyDao() }

    single<ExchangeRateDao> { get<AppDatabase>().exchangeRateDao() }

    single<GroupDao> { get<AppDatabase>().groupDao() }

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

}

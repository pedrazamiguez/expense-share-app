package es.pedrazamiguez.expenseshareapp.data.local.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
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
import es.pedrazamiguez.expenseshareapp.data.local.database.ALL_MIGRATIONS
import es.pedrazamiguez.expenseshareapp.data.local.database.AppDatabase
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalCashWithdrawalDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalContributionDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalCurrencyDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalExpenseDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalGroupDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalSubunitDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datasource.impl.LocalUserDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.local.datastore.NotificationUserPreferences
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

val dataLocalModule = module {

    single {
        UserPreferences(
            context = androidContext(),
            authenticationService = get<AuthenticationService>()
        )
    }

    single {
        NotificationUserPreferences(
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
            .addMigrations(*ALL_MIGRATIONS)
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

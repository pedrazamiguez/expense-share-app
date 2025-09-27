package es.pedrazamiguez.expenseshareapp.data.local.di

import android.app.Application
import androidx.room.Room
import es.pedrazamiguez.expenseshareapp.data.local.dao.CurrencyDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ExchangeRateDao
import es.pedrazamiguez.expenseshareapp.data.local.database.AppDatabase
import es.pedrazamiguez.expenseshareapp.data.local.impl.LocalCurrencyDataSourceImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCurrencyDataSource
import org.koin.dsl.module

val dataLocalModule = module {

    single<AppDatabase> {
        Room.databaseBuilder(
            context = get<Application>(),
            klass = AppDatabase::class.java,
            name = "expense_share_db"
        ).build()
    }

    single<CurrencyDao> { get<AppDatabase>().currencyDao() }

    single<ExchangeRateDao> { get<AppDatabase>().exchangeRateDao() }

    single<LocalCurrencyDataSource> {
        LocalCurrencyDataSourceImpl(
            currencyDao = get<CurrencyDao>(),
            exchangeRateDao = get<ExchangeRateDao>()
        )
    }

}

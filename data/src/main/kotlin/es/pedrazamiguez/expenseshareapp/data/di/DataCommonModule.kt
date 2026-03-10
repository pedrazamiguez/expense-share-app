package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.data.BuildConfig
import es.pedrazamiguez.expenseshareapp.data.local.database.AppDatabase
import es.pedrazamiguez.expenseshareapp.data.repository.impl.CurrencyRepositoryImpl
import es.pedrazamiguez.expenseshareapp.data.local.service.LocalDatabaseCleanerImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.remote.RemoteCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import es.pedrazamiguez.expenseshareapp.domain.service.LocalDatabaseCleaner
import org.koin.dsl.module
import java.time.Duration

val dataCommonModule = module {

    single<CurrencyRepository> {
        CurrencyRepositoryImpl(
            localDataSource = get<LocalCurrencyDataSource>(),
            remoteDataSource = get<RemoteCurrencyDataSource>(),
            cacheDuration = Duration.ofHours(BuildConfig.EXCHANGE_RATES_CACHE_DURATION_HOURS)
        )
    }

    single<LocalDatabaseCleaner> {
        LocalDatabaseCleanerImpl(appDatabase = get<AppDatabase>())
    }

}

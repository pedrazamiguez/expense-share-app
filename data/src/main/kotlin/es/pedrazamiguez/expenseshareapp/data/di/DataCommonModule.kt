package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.data.BuildConfig
import es.pedrazamiguez.expenseshareapp.data.local.database.AppDatabase
import es.pedrazamiguez.expenseshareapp.data.local.service.LocalDatabaseCleanerServiceImpl
import es.pedrazamiguez.expenseshareapp.data.repository.impl.CurrencyRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.remote.RemoteCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import es.pedrazamiguez.expenseshareapp.domain.service.LocalDatabaseCleanerService
import java.time.Duration
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val dataCommonModule = module {

    single<CurrencyRepository> {
        CurrencyRepositoryImpl(
            localDataSource = get<LocalCurrencyDataSource>(),
            remoteDataSource = get<RemoteCurrencyDataSource>(),
            cacheDuration = Duration.ofHours(BuildConfig.EXCHANGE_RATES_CACHE_DURATION_HOURS)
        )
    }

    single<LocalDatabaseCleanerService> {
        LocalDatabaseCleanerServiceImpl(
            appDatabase = get<AppDatabase>(),
            ioDispatcher = Dispatchers.IO
        )
    }
}

package es.pedrazamiguez.splittrip.data.di

import es.pedrazamiguez.splittrip.data.BuildConfig
import es.pedrazamiguez.splittrip.data.local.database.AppDatabase
import es.pedrazamiguez.splittrip.data.local.service.LocalDatabaseCleanerServiceImpl
import es.pedrazamiguez.splittrip.data.repository.impl.CurrencyRepositoryImpl
import es.pedrazamiguez.splittrip.domain.datasource.local.LocalCurrencyDataSource
import es.pedrazamiguez.splittrip.domain.datasource.remote.RemoteCurrencyDataSource
import es.pedrazamiguez.splittrip.domain.repository.CurrencyRepository
import es.pedrazamiguez.splittrip.domain.service.LocalDatabaseCleanerService
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

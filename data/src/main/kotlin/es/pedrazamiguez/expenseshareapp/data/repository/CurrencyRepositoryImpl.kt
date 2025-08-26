package es.pedrazamiguez.expenseshareapp.data.repository

import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.remote.RemoteCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRate
import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import java.time.Duration
import java.time.Instant

class CurrencyRepositoryImpl(
    private val localDataSource: LocalCurrencyDataSource,
    private val remoteDataSource: RemoteCurrencyDataSource,
    private val cacheDuration: Duration
) : CurrencyRepository {

    override suspend fun getCurrencies(forceRefresh: Boolean): List<Currency> {

        return if (forceRefresh) {
            val remote = remoteDataSource.fetchCurrencies()
            localDataSource.saveCurrencies(remote)
            remote
        } else {
            val local = localDataSource.getCurrencies()
            local.ifEmpty {
                val remote = remoteDataSource.fetchCurrencies()
                localDataSource.saveCurrencies(remote)
                remote
            }
        }
    }

    override suspend fun getExchangeRates(
        baseCurrencyCode: String, timestamp: Instant?
    ): List<ExchangeRate> {
        val localRates = localDataSource.getExchangeRates(baseCurrencyCode, timestamp)

        val isStale = localRates.isEmpty() || localRates.any {
            Instant.ofEpochSecond(it.timestamp.epochSecond)
                .isBefore(Instant.now().minus(cacheDuration))
        }

        return if (isStale) {
            val remoteRates = remoteDataSource.fetchExchangeRates(baseCurrencyCode)
            localDataSource.saveExchangeRates(remoteRates.map {
                it.copy(timestamp = Instant.now()) // store now as lastUpdated
            })
            remoteRates
        } else {
            localRates
        }
    }
}
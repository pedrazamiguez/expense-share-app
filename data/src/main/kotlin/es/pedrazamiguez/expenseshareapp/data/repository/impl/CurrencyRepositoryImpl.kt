package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.remote.RemoteCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import es.pedrazamiguez.expenseshareapp.domain.result.ExchangeRateResult
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

    override suspend fun getExchangeRates(baseCurrencyCode: String): ExchangeRateResult {
        val localRates = localDataSource.getExchangeRates(baseCurrencyCode)
        val lastUpdated = localDataSource.getLastUpdated(baseCurrencyCode)

        val isStale = lastUpdated == null || Instant.ofEpochSecond(lastUpdated)
            .isBefore(Instant.now().minus(cacheDuration))

        return when {
            localRates.rates.isEmpty() -> {
                runCatching {
                    val remoteRates = remoteDataSource.fetchExchangeRates(baseCurrencyCode)
                    localDataSource.saveExchangeRates(remoteRates)
                    ExchangeRateResult.Fresh(remoteRates)
                }.getOrElse { ExchangeRateResult.Empty }
            }

            isStale -> {
                runCatching {
                    val remoteRates = remoteDataSource.fetchExchangeRates(baseCurrencyCode)
                    localDataSource.saveExchangeRates(remoteRates)
                    ExchangeRateResult.Fresh(remoteRates)
                }.getOrElse { ExchangeRateResult.Stale(localRates) }
            }

            else -> ExchangeRateResult.Fresh(localRates)
        }
    }

}
package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.remote.RemoteCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import es.pedrazamiguez.expenseshareapp.domain.result.ExchangeRateResult
import java.time.Duration
import java.time.Instant
import timber.log.Timber

class CurrencyRepositoryImpl(
    private val localDataSource: LocalCurrencyDataSource,
    private val remoteDataSource: RemoteCurrencyDataSource,
    private val cacheDuration: Duration
) : CurrencyRepository {

    override suspend fun getCurrencies(forceRefresh: Boolean): List<Currency> = if (forceRefresh) {
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

    override suspend fun getExchangeRates(baseCurrencyCode: String): ExchangeRateResult {
        val localRates = localDataSource.getExchangeRates(baseCurrencyCode)
        val lastUpdated = localDataSource.getLastUpdated(baseCurrencyCode)

        val isStale = lastUpdated == null ||
            Instant
                .ofEpochSecond(lastUpdated)
                .isBefore(
                    Instant
                        .now()
                        .minus(cacheDuration)
                )

        return when {
            localRates.exchangeRates.isEmpty() -> {
                runCatching {
                    val remoteRates = remoteDataSource.fetchExchangeRates(baseCurrencyCode)
                    localDataSource.saveExchangeRates(remoteRates)
                    ExchangeRateResult.Fresh(remoteRates)
                }.getOrElse { e ->
                    Timber.e(
                        e,
                        "Failed to fetch exchange rates for baseCurrencyCode=%s (no local cache)",
                        baseCurrencyCode
                    )
                    ExchangeRateResult.Empty
                }
            }

            isStale -> {
                runCatching {
                    val remoteRates = remoteDataSource.fetchExchangeRates(baseCurrencyCode)
                    localDataSource.saveExchangeRates(remoteRates)
                    ExchangeRateResult.Fresh(remoteRates)
                }.getOrElse { e ->
                    Timber.w(
                        e,
                        "Failed to refresh exchange rates for baseCurrencyCode=%s" +
                            " (lastUpdated=%s, cacheDuration=%s); using stale cache",
                        baseCurrencyCode,
                        lastUpdated?.let { timestamp -> Instant.ofEpochSecond(timestamp).toString() },
                        cacheDuration
                    )
                    ExchangeRateResult.Stale(localRates)
                }
            }

            else -> ExchangeRateResult.Fresh(localRates)
        }
    }
}

package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRate
import java.time.Instant

interface CurrencyRepository {

    suspend fun getCurrencies(forceRefresh: Boolean = false): List<Currency>

    suspend fun getExchangeRates(
        baseCurrencyCode: String,
        timestamp: Instant? = null
    ): List<ExchangeRate>

}

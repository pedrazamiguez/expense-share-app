package es.pedrazamiguez.expenseshareapp.domain.datasource.local

import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRate
import java.time.Instant

interface LocalCurrencyDataSource {
    suspend fun saveCurrencies(currencies: List<Currency>)
    suspend fun getCurrencies(): List<Currency>

    suspend fun saveExchangeRates(rates: List<ExchangeRate>)
    suspend fun getExchangeRates(
        baseCurrencyCode: String,
        timestamp: Instant? = null
    ): List<ExchangeRate>
}

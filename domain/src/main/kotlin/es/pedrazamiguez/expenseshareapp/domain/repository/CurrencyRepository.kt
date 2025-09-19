package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.result.ExchangeRateResult

interface CurrencyRepository {
    suspend fun getCurrencies(forceRefresh: Boolean = false): List<Currency>
    suspend fun getExchangeRates(baseCurrencyCode: String): ExchangeRateResult
}

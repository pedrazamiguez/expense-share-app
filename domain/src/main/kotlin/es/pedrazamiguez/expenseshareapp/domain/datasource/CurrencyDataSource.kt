package es.pedrazamiguez.expenseshareapp.domain.datasource

interface CurrencyDataSource {
    suspend fun refreshCurrencies()
    suspend fun getCurrencies(commonOnly: Boolean = false): List<Currency>
    suspend fun refreshExchangeRates(baseCurrency: String)
    suspend fun getExchangeRates(baseCurrency: String): List<ExchangeRateEntity>
}
package es.pedrazamiguez.expenseshareapp.data.remote.datasource

import es.pedrazamiguez.expenseshareapp.data.remote.api.OpenExchangeRatesApi
import es.pedrazamiguez.expenseshareapp.data.remote.mapper.CurrencyDtoMapper
import es.pedrazamiguez.expenseshareapp.domain.datasource.remote.RemoteCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRate

class RemoteCurrencyDataSourceImpl(
    private val api: OpenExchangeRatesApi, private val appId: String
) : RemoteCurrencyDataSource {

    override suspend fun fetchCurrencies(): List<Currency> {
        val response = api.getCurrencies(appId)
        return CurrencyDtoMapper.mapCurrencies(response)
    }

    override suspend fun fetchExchangeRates(baseCurrencyCode: String): List<ExchangeRate> {
        val currencies = fetchCurrencies().associateBy { it.code }
        val response = api.getExchangeRates(appId, baseCurrencyCode)
        return CurrencyDtoMapper.mapExchangeRates(response, currencies)
    }
}

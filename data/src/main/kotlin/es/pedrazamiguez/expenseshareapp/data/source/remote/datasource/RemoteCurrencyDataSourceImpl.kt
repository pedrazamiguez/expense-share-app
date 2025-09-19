package es.pedrazamiguez.expenseshareapp.data.source.remote.datasource

import es.pedrazamiguez.expenseshareapp.data.source.remote.api.OpenExchangeRatesApi
import es.pedrazamiguez.expenseshareapp.data.source.remote.mapper.CurrencyDtoMapper
import es.pedrazamiguez.expenseshareapp.domain.datasource.remote.RemoteCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates

class RemoteCurrencyDataSourceImpl(
    private val api: OpenExchangeRatesApi, private val appId: String
) : RemoteCurrencyDataSource {

    override suspend fun fetchCurrencies(): List<Currency> {
        val response = api.getCurrencies(appId)
        return CurrencyDtoMapper.mapCurrencies(response)
    }

    override suspend fun fetchExchangeRates(baseCurrencyCode: String): ExchangeRates {
        val response = api.getExchangeRates(appId, baseCurrencyCode)
        return CurrencyDtoMapper.mapExchangeRates(response)
    }

}

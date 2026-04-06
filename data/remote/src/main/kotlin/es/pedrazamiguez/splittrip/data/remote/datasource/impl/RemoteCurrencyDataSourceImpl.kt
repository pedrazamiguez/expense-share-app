package es.pedrazamiguez.splittrip.data.remote.datasource.impl

import es.pedrazamiguez.splittrip.data.remote.api.OpenExchangeRatesApi
import es.pedrazamiguez.splittrip.data.remote.mapper.CurrencyDtoMapper
import es.pedrazamiguez.splittrip.domain.datasource.remote.RemoteCurrencyDataSource
import es.pedrazamiguez.splittrip.domain.model.Currency
import es.pedrazamiguez.splittrip.domain.model.ExchangeRates

class RemoteCurrencyDataSourceImpl(private val api: OpenExchangeRatesApi, private val appId: String) :
    RemoteCurrencyDataSource {

    override suspend fun fetchCurrencies(): List<Currency> {
        val response = api.getCurrencies(appId)
        return CurrencyDtoMapper.mapCurrencies(response)
    }

    override suspend fun fetchExchangeRates(baseCurrencyCode: String): ExchangeRates {
        val response = api.getExchangeRates(
            appId,
            baseCurrencyCode
        )
        return CurrencyDtoMapper.mapExchangeRates(response)
    }
}

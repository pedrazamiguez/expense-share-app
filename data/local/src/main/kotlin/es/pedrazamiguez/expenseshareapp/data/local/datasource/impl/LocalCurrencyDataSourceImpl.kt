package es.pedrazamiguez.expenseshareapp.data.local.datasource.impl

import es.pedrazamiguez.expenseshareapp.data.local.dao.CurrencyDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ExchangeRateDao
import es.pedrazamiguez.expenseshareapp.data.local.mapper.CurrencyEntityMapper
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates

class LocalCurrencyDataSourceImpl(
    private val currencyDao: CurrencyDao,
    private val exchangeRateDao: ExchangeRateDao
) : LocalCurrencyDataSource {

    override suspend fun saveCurrencies(currencies: List<Currency>) {
        val entities = currencies.map { CurrencyEntityMapper.toEntity(it) }
        currencyDao.insertCurrencies(entities)
    }

    override suspend fun getCurrencies(): List<Currency> {
        return currencyDao.getCurrencies().map { CurrencyEntityMapper.toDomain(it) }
    }

    override suspend fun saveExchangeRates(rates: ExchangeRates) {
        val entities = CurrencyEntityMapper.toEntities(rates)
        exchangeRateDao.insertExchangeRates(entities)
    }

    override suspend fun getExchangeRates(base: String): ExchangeRates {
        val entities = exchangeRateDao.getExchangeRates(base)
        val baseCurrency = Currency(
            base,
            "",
            base,
            2
        ) // minimal, or fetch from currencies table
        return CurrencyEntityMapper.toDomain(
            entities,
            baseCurrency
        )
    }

    override suspend fun getLastUpdated(base: String): Long? {
        return exchangeRateDao.getLastUpdated(base)
    }

}

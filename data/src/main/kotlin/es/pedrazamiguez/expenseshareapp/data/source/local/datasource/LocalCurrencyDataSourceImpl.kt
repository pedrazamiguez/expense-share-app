package es.pedrazamiguez.expenseshareapp.data.source.local.datasource

import es.pedrazamiguez.expenseshareapp.data.source.local.dao.CurrencyDao
import es.pedrazamiguez.expenseshareapp.data.source.local.dao.ExchangeRateDao
import es.pedrazamiguez.expenseshareapp.data.source.local.mapper.CurrencyEntityMapper
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRate
import java.time.Instant

class LocalCurrencyDataSourceImpl(
    private val currencyDao: CurrencyDao, private val exchangeRateDao: ExchangeRateDao
) : LocalCurrencyDataSource {

    override suspend fun saveCurrencies(currencies: List<Currency>) {
        val entities = currencies.map { CurrencyEntityMapper.toEntity(it) }
        currencyDao.insertCurrencies(entities)
    }

    override suspend fun getCurrencies(): List<Currency> {
        return currencyDao.getCurrencies().map { CurrencyEntityMapper.toDomain(it) }
    }

    override suspend fun saveExchangeRates(rates: List<ExchangeRate>) {
        val entities = rates.map { CurrencyEntityMapper.toEntity(it) }
        exchangeRateDao.insertExchangeRates(entities)
    }

    override suspend fun getExchangeRates(
        baseCurrencyCode: String, timestamp: Instant?
    ): List<ExchangeRate> {
        return exchangeRateDao.getExchangeRates(baseCurrencyCode)
            .map { CurrencyEntityMapper.toDomain(it) }
    }
}

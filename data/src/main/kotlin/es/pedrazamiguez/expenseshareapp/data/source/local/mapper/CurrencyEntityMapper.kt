package es.pedrazamiguez.expenseshareapp.data.source.local.mapper

import es.pedrazamiguez.expenseshareapp.data.source.local.entity.CurrencyEntity
import es.pedrazamiguez.expenseshareapp.data.source.local.entity.ExchangeRateEntity
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRate
import java.time.Instant

object CurrencyEntityMapper {

    fun toDomain(entity: CurrencyEntity) = Currency(
        code = entity.code,
        symbol = entity.symbol,
        defaultName = entity.defaultName,
        decimalDigits = entity.decimalDigits
    )

    fun toEntity(model: Currency) = CurrencyEntity(
        code = model.code,
        symbol = model.symbol,
        defaultName = model.defaultName,
        decimalDigits = model.decimalDigits
    )

    fun toDomain(entity: ExchangeRateEntity) = ExchangeRate(
        baseCurrency = Currency(entity.baseCurrencyCode, "", entity.baseCurrencyCode, 2),
        currency = Currency(entity.currencyCode, "", entity.currencyCode, 2),
        rate = entity.rate,
        timestamp = Instant.ofEpochSecond(entity.timestamp)
    )

    fun toEntity(model: ExchangeRate) = ExchangeRateEntity(
        baseCurrencyCode = model.baseCurrency.code,
        currencyCode = model.currency.code,
        rate = model.rate,
        timestamp = model.timestamp.epochSecond,
        lastUpdated = Instant.now().epochSecond
    )
}

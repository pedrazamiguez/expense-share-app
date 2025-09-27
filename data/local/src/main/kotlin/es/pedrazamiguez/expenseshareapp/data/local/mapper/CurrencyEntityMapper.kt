package es.pedrazamiguez.expenseshareapp.data.local.mapper

import es.pedrazamiguez.expenseshareapp.data.local.entity.CurrencyEntity
import es.pedrazamiguez.expenseshareapp.data.local.entity.ExchangeRateEntity
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRate
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates
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

    fun toDomain(
        entities: List<ExchangeRateEntity>,
        base: Currency
    ): ExchangeRates {
        if (entities.isEmpty()) return ExchangeRates(
            base,
            emptyList(),
            Instant.EPOCH
        )

        val lastUpdated = entities.maxOf { it.lastUpdated }
        val exchangeRates = entities.map {
            ExchangeRate(
                currency = Currency(
                    it.currencyCode,
                    "",
                    it.currencyCode,
                    2
                ),
                rate = it.rate
            )
        }
        return ExchangeRates(
            base,
            exchangeRates,
            Instant.ofEpochSecond(lastUpdated)
        )
    }

    fun toEntities(model: ExchangeRates): List<ExchangeRateEntity> {
        val lastUpdated = model.lastUpdated.epochSecond
        return model.exchangeRates.map {
            ExchangeRateEntity(
                baseCurrencyCode = model.baseCurrency.code,
                currencyCode = it.currency.code,
                rate = it.rate,
                lastUpdated = lastUpdated
            )
        }
    }

}

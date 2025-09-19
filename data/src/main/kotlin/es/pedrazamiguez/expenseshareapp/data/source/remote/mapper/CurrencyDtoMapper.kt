package es.pedrazamiguez.expenseshareapp.data.source.remote.mapper

import es.pedrazamiguez.expenseshareapp.data.source.remote.dto.ExchangeRateResponse
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates
import java.time.Instant

object CurrencyDtoMapper {

    fun mapCurrencies(apiResponse: Map<String, String>): List<Currency> {
        return apiResponse.map { (code, name) ->
            Currency(
                code = code, symbol = "", // enrich later with a separate lookup
                defaultName = name, decimalDigits = 2 // ISO standard default
            )
        }
    }

    fun mapExchangeRates(
        response: ExchangeRateResponse
    ): ExchangeRates {
        val baseCurrency = Currency(response.base, "", response.base, 2)
        val rates = response.rates.map { (code, value) ->
            ExchangeRates.Rate(Currency(code, "", code, 2), value)
        }

        return ExchangeRates(baseCurrency, rates, Instant.ofEpochSecond(response.timestamp))
    }

}

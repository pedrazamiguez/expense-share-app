package es.pedrazamiguez.expenseshareapp.data.remote.mapper

import es.pedrazamiguez.expenseshareapp.data.remote.dto.ExchangeRateResponse
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRate
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates
import java.time.Instant

object CurrencyDtoMapper {

    fun mapCurrencies(apiResponse: Map<String, String>): List<Currency> {
        return apiResponse.map { (code, name) ->
            Currency(
                code = code,
                symbol = "", // enrich later with a separate lookup
                defaultName = name,
                decimalDigits = 2 // ISO standard default
            )
        }
    }

    fun mapExchangeRates(
        response: ExchangeRateResponse
    ): ExchangeRates {
        val baseCurrency = Currency(
            response.base,
            "",
            response.base,
            2
        )
        val exchangeRates = response.rates.map { (code, value) ->
            ExchangeRate(
                Currency(
                    code,
                    "",
                    code,
                    2
                ),
                value
            )
        }

        return ExchangeRates(
            baseCurrency,
            exchangeRates,
            Instant.ofEpochSecond(response.timestamp)
        )
    }

}

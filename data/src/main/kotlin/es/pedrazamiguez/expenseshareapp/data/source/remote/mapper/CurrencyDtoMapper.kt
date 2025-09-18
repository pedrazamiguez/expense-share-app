package es.pedrazamiguez.expenseshareapp.data.source.remote.mapper

import es.pedrazamiguez.expenseshareapp.data.source.remote.dto.ExchangeRateResponse
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRate
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
        response: ExchangeRateResponse, currencies: Map<String, Currency>
    ): List<ExchangeRate> {
        val baseCurrency =
            currencies[response.base] ?: Currency(response.base, "", response.base, 2)

        val timestamp = Instant.ofEpochSecond(response.timestamp)

        return response.rates.map { (code, rate) ->
            val targetCurrency = currencies[code] ?: Currency(code, "", code, 2)
            ExchangeRate(
                baseCurrency = baseCurrency,
                currency = targetCurrency,
                rate = rate,
                timestamp = timestamp
            )
        }
    }
}

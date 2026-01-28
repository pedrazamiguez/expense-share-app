package es.pedrazamiguez.expenseshareapp.data.remote.mapper

import es.pedrazamiguez.expenseshareapp.data.remote.dto.ExchangeRateResponse
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRate
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates
import java.time.Instant

object CurrencyDtoMapper {

    // Common currency symbols lookup
    private val currencySymbols = mapOf(
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "JPY" to "¥",
        "CNY" to "¥",
        "CHF" to "CHF",
        "CAD" to "C$",
        "AUD" to "A$",
        "NZD" to "NZ$",
        "INR" to "₹",
        "RUB" to "₽",
        "BRL" to "R$",
        "ZAR" to "R",
        "MXN" to "$",
        "SEK" to "kr",
        "NOK" to "kr",
        "DKK" to "kr",
        "PLN" to "zł",
        "THB" to "฿",
        "IDR" to "Rp",
        "HUF" to "Ft",
        "CZK" to "Kč",
        "ILS" to "₪",
        "CLP" to "$",
        "PHP" to "₱",
        "AED" to "د.إ",
        "COP" to "$",
        "SAR" to "﷼",
        "MYR" to "RM",
        "RON" to "lei",
        "SGD" to "S$",
        "HKD" to "HK$",
        "KRW" to "₩",
        "TRY" to "₺",
        "ARS" to "$",
        "VND" to "₫",
        "UAH" to "₴",
        "BGN" to "лв",
        "HRK" to "kn",
        "ISK" to "kr",
        "NGN" to "₦",
        "EGP" to "E£",
        "PKR" to "₨",
        "QAR" to "﷼",
        "KWD" to "د.ك",
        "BHD" to "د.ب",
        "OMR" to "﷼"
    )

    private fun getCurrencySymbol(code: String): String {
        return currencySymbols[code] ?: code
    }

    fun mapCurrencies(apiResponse: Map<String, String>): List<Currency> {
        return apiResponse.map { (code, name) ->
            Currency(
                code = code,
                symbol = getCurrencySymbol(code),
                defaultName = name,
                decimalDigits = when (code) {
                    // Zero decimal currencies
                    "JPY", "KRW", "VND", "CLP", "ISK" -> 0
                    // Three decimal currencies
                    "BHD", "KWD", "OMR", "TND" -> 3
                    // Standard two decimal
                    else -> 2
                }
            )
        }
    }

    fun mapExchangeRates(
        response: ExchangeRateResponse
    ): ExchangeRates {
        val baseCurrency = Currency(
            response.base,
            getCurrencySymbol(response.base),
            response.base,
            2
        )
        val exchangeRates = response.rates.map { (code, value) ->
            ExchangeRate(
                Currency(
                    code,
                    getCurrencySymbol(code),
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

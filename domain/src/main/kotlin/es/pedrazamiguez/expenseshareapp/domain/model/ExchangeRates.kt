package es.pedrazamiguez.expenseshareapp.domain.model

import java.math.BigDecimal
import java.time.Instant

data class ExchangeRates(
    val baseCurrency: Currency, val rates: List<Rate>, val lastUpdated: Instant
) {
    data class Rate(
        val currency: Currency, val rate: BigDecimal
    ) {
        init {
            require(rate >= BigDecimal.ZERO) { "Exchange rate cannot be negative" }
        }
    }
}

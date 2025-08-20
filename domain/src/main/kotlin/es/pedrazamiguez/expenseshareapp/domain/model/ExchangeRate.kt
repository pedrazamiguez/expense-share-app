package es.pedrazamiguez.expenseshareapp.domain.model

import java.math.BigDecimal
import java.time.Instant

data class ExchangeRate(
    val baseCurrency: Currency,
    val currency: Currency,
    val rate: BigDecimal,
    val timestamp: Instant
) {
    init {
        require(rate >= BigDecimal.ZERO) { "Exchange rate cannot be negative" }
        require(baseCurrency != currency) { "Base and target currencies must be different" }
    }
}

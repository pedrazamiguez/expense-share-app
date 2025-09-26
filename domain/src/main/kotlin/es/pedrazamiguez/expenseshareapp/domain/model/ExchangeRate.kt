package es.pedrazamiguez.expenseshareapp.domain.model

import java.math.BigDecimal

data class ExchangeRate(
    val currency: Currency,
    val rate: BigDecimal
) {
    init {
        require(rate >= BigDecimal.ZERO) { "Exchange rate cannot be negative" }
    }
}

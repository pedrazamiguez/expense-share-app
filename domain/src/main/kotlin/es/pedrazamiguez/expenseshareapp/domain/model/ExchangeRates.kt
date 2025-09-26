package es.pedrazamiguez.expenseshareapp.domain.model

import java.time.Instant

data class ExchangeRates(
    val baseCurrency: Currency,
    val exchangeRates: List<ExchangeRate>,
    val lastUpdated: Instant
)

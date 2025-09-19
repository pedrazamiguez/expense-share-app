package es.pedrazamiguez.expenseshareapp.domain.result

import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates

sealed interface ExchangeRateResult {
    data class Fresh(val exchangeRates: ExchangeRates) : ExchangeRateResult
    data class Stale(val exchangeRates: ExchangeRates) : ExchangeRateResult
    object Empty : ExchangeRateResult
}

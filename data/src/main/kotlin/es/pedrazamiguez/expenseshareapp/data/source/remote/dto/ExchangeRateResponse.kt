package es.pedrazamiguez.expenseshareapp.data.source.remote.dto

import java.math.BigDecimal

data class ExchangeRateResponse(
    val disclaimer: String?,
    val license: String?,
    val timestamp: Long,
    val base: String,
    val rates: Map<String, BigDecimal>
)

package es.pedrazamiguez.expenseshareapp.data.local.entity

import androidx.room.Entity
import java.math.BigDecimal

@Entity(
    tableName = "exchange_rates", primaryKeys = ["baseCurrencyCode", "currencyCode", "timestamp"]
)
data class ExchangeRateEntity(
    val baseCurrencyCode: String,
    val currencyCode: String,
    val rate: BigDecimal,
    val timestamp: Long,
    val lastUpdated: Long
)

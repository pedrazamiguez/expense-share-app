package es.pedrazamiguez.expenseshareapp.data.source.local.entity

import androidx.room.Entity
import java.math.BigDecimal

@Entity(
    tableName = "exchange_rates", primaryKeys = ["baseCurrencyCode", "currencyCode"]
)
data class ExchangeRateEntity(
    val baseCurrencyCode: String,
    val currencyCode: String,
    val rate: BigDecimal,
    val lastUpdated: Long // epoch seconds, shared across the batch
)

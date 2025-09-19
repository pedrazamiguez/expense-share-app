package es.pedrazamiguez.expenseshareapp.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey val code: String,
    val symbol: String,
    val defaultName: String,
    val decimalDigits: Int
)

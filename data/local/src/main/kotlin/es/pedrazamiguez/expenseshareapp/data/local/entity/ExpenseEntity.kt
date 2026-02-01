package es.pedrazamiguez.expenseshareapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["groupId"])]
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val title: String,
    val sourceAmount: Long,
    val sourceCurrency: String,
    val sourceTipAmount: Long,
    val sourceFeeAmount: Long,
    val groupAmount: Long,
    val groupCurrency: String,
    val exchangeRate: Double,
    val paymentMethod: String,
    val createdBy: String,
    val payerType: String,
    val createdAtMillis: Long?,
    val lastUpdatedAtMillis: Long?
)

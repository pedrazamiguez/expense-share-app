package es.pedrazamiguez.expenseshareapp.domain.model

import java.time.LocalDateTime
import java.util.Currency

data class Expense(
    val id: String = "",
    val groupId: String = "",
    val title: String = "",
    val amountCents: Long = 0,
    val currency: Currency = Currency.getInstance("EUR"),
    val createdBy: String = "",
    val payerType: String = "GROUP",
    val createdAt: LocalDateTime? = null,
    val lastUpdatedAt: LocalDateTime? = null
)

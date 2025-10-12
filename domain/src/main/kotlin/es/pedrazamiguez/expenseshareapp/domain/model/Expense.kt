package es.pedrazamiguez.expenseshareapp.domain.model

import java.time.LocalDateTime

data class Expense(
    val id: String = "", val groupId: String = "", val title: String = "", val amountCents: Long = 0, val currency: String = "EUR",
    val createdBy: String = "", val payerType: String = "GROUP", val createdAt: LocalDateTime? = null,
    val lastUpdatedAt: LocalDateTime? = null
)

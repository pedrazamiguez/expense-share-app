package es.pedrazamiguez.expenseshareapp.domain.model

import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import java.time.LocalDateTime

data class Contribution(
    val id: String = "",
    val groupId: String = "",
    val userId: String = "",
    val createdBy: String = "",
    val contributionScope: PayerType = PayerType.USER,
    val subunitId: String? = null,
    val amount: Long = 0,
    val currency: String = "EUR",
    val linkedExpenseId: String? = null,
    val createdAt: LocalDateTime? = null,
    val lastUpdatedAt: LocalDateTime? = null
)

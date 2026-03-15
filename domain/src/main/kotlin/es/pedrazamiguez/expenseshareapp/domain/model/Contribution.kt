package es.pedrazamiguez.expenseshareapp.domain.model

import java.time.LocalDateTime

data class Contribution(
    val id: String = "",
    val groupId: String = "",
    val userId: String = "",
    val amount: Long = 0,
    val currency: String = "EUR",
    val createdAt: LocalDateTime? = null,
    val lastUpdatedAt: LocalDateTime? = null
)

package es.pedrazamiguez.expenseshareapp.domain.model

import java.time.LocalDateTime

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val currency: String = "EUR",
    val extraCurrencies: List<String> = emptyList(),
    val members: List<String> = emptyList(),
    val mainImagePath: String? = null,
    val createdAt: LocalDateTime? = null,
    val lastUpdatedAt: LocalDateTime? = null
)

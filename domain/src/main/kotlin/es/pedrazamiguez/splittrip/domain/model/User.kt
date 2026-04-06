package es.pedrazamiguez.splittrip.domain.model

import java.time.LocalDateTime

data class User(
    val userId: String,
    val email: String,
    val displayName: String? = null,
    val profileImagePath: String? = null,
    val createdAt: LocalDateTime? = null
)

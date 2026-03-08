package es.pedrazamiguez.expenseshareapp.domain.model

data class User(
    val userId: String,
    val email: String,
    val displayName: String? = null,
    val profileImagePath: String? = null
)


package es.pedrazamiguez.expenseshareapp.domain.model

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val currency: String = "EUR",
    val members: List<String> = emptyList(),
)

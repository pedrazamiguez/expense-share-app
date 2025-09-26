package es.pedrazamiguez.expenseshareapp.domain.model

data class Balance(
    val userId: String,
    val amount: Double,
    val currency: Currency
)

package es.pedrazamiguez.expenseshareapp.domain.model

data class Expense(
    val groupId: String,
    val title: String,
    val amountCents: Long,
    val currency: String = "EUR",
    val createdBy: String,
    val payerType: String = "GROUP"
)

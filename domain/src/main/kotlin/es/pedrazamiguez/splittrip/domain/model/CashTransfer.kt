package es.pedrazamiguez.splittrip.domain.model

data class CashTransfer(
    val id: String,
    val groupId: String,
    val fromUserId: String,
    val toUserId: String,
    val amountCents: Long,
    val currency: String,
    val equivalentBaseAmountCents: Long,
    val createdAt: Long
)

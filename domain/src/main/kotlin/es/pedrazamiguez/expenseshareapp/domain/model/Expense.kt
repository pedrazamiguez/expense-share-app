package es.pedrazamiguez.expenseshareapp.domain.model

import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import java.time.LocalDateTime

data class Expense(
    val id: String = "",
    val groupId: String = "",
    val title: String = "",

    // 1. Source (Wallet)
    val sourceAmount: Long = 0,
    val sourceCurrency: String = "EUR",
    val sourceTipAmount: Long = 0,
    val sourceFeeAmount: Long = 0,

    // 2. Target (Group Debt)
    val groupAmount: Long = 0,
    val groupCurrency: String = "EUR",

    // 3. Bridge (Rate)
    val exchangeRate: Double = 1.0,

    val paymentMethod: PaymentMethod = PaymentMethod.OTHER,
    val createdBy: String = "",
    val payerType: String = "GROUP",
    val createdAt: LocalDateTime? = null,
    val lastUpdatedAt: LocalDateTime? = null
)

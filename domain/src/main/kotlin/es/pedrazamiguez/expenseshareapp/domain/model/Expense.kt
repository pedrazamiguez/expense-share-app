package es.pedrazamiguez.expenseshareapp.domain.model

import es.pedrazamiguez.expenseshareapp.domain.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import java.math.BigDecimal
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
    val exchangeRate: BigDecimal = BigDecimal.ONE,

    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val vendor: String? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.OTHER,
    val paymentStatus: PaymentStatus = PaymentStatus.FINISHED,
    val dueDate: LocalDateTime? = null,
    val receiptLocalUri: String? = null,
    val cashTranches: List<CashTranche> = emptyList(),
    val createdBy: String = "",
    val payerType: String = "GROUP",
    val createdAt: LocalDateTime? = null,
    val lastUpdatedAt: LocalDateTime? = null
)

package es.pedrazamiguez.expenseshareapp.data.source.remote.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import es.pedrazamiguez.expenseshareapp.data.source.remote.enums.Currency
import es.pedrazamiguez.expenseshareapp.data.source.remote.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.data.source.remote.enums.PayerType
import es.pedrazamiguez.expenseshareapp.data.source.remote.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.data.source.remote.enums.PaymentStatus

data class Expense(
    @PropertyName("expenseId") val expenseId: String = "",
    @PropertyName("operationDate") val operationDate: Timestamp = Timestamp.now(),
    @PropertyName("expenseCategory") val expenseCategory: ExpenseCategory = ExpenseCategory.OTHER,
    @PropertyName("title") val title: String = "",
    @PropertyName("description") val description: String? = null,
    @PropertyName("vendor") val vendor: String? = null,
    @PropertyName("amount") val amount: Double = 0.0,
    @PropertyName("currency") val currency: Currency = Currency.EUR,
    @PropertyName("groupCurrency") val groupCurrency: Currency = Currency.EUR,
    @PropertyName("exchangeRate") val exchangeRate: Double = 1.0,
    @PropertyName("addOns") val addOns: List<AddOn> = emptyList(),
    @PropertyName("paymentMethod") val paymentMethod: PaymentMethod = PaymentMethod.DEBIT_CARD,
    @PropertyName("paymentStatus") val paymentStatus: PaymentStatus = PaymentStatus.FINISHED,
    @PropertyName("payerType") val payerType: PayerType = PayerType.GROUP,
    @PropertyName("payerId") val payerId: String = "",
    @PropertyName("paidAt") val paidAt: Timestamp = Timestamp.now(),
    @PropertyName("notes") val notes: String? = null,
    @PropertyName("createdBy") val createdBy: String = "",
    @PropertyName("createdAt") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("lastUpdatedBy") val lastUpdatedBy: String = "",
    @PropertyName("lastUpdatedAt") val lastUpdatedAt: Timestamp = Timestamp.now()
)

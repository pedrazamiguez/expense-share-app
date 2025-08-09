package es.pedrazamiguez.expenseshareapp.data.remote.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class Expense(
    @PropertyName("title") val title: String = "",
    @PropertyName("amount") val amount: Double = 0.0,
    @PropertyName("currency") val currency: String = "",
    @PropertyName("paidBy") val paidBy: String = "",
    @PropertyName("paidForSubunitId") val paidForSubunitId: String? = null,
    @PropertyName("notes") val notes: String? = null,
    @PropertyName("paidAt") val paidAt: Date = Date()
)

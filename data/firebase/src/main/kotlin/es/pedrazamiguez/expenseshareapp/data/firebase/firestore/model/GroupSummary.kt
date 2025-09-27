package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.enums.Currency

data class GroupSummary(
    @PropertyName("groupId") val groupId: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("currency") val currency: Currency = Currency.EUR,
    @PropertyName("totalBalance") val totalBalance: Double = 0.0,
    @PropertyName("userBalances") val userBalances: Map<String, Double> = emptyMap(),
    @PropertyName("totalMembers") val totalMembers: Int = 0,
    @PropertyName("totalExpenses") val totalExpenses: Int = 0,
    @PropertyName("totalIncome") val totalIncome: Double = 0.0,
    @PropertyName("totalDebts") val totalDebts: Double = 0.0,
    @PropertyName("mainImagePath") val mainImagePath: String = "",
    @PropertyName("lastActivityLog") val lastActivityLog: String? = null,
    @PropertyName("lastUpdatedAt") val lastUpdatedAt: Timestamp = Timestamp.now()
)

package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class GroupSummaryDocument(
    val groupId: String = "",
    val name: String = "",
    val currency: String = "EUR",
    val totalBalanceCents: Long = 0L,
    val userBalancesCents: Map<String, Long> = emptyMap(),
    val totalMembers: Int = 0,
    val totalExpenses: Int = 0,
    val totalIncomeCents: Long = 0L,
    val totalDebtsCents: Long = 0L,
    val mainImagePath: String = "",
    val lastActivityLog: String? = null,
    @ServerTimestamp var lastUpdatedAt: Timestamp? = null
)

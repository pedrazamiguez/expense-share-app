package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class CashWithdrawalDocument(
    val withdrawalId: String = "",
    val groupId: String = "",
    val groupRef: DocumentReference? = null,
    val withdrawnBy: String = "",
    val amountWithdrawn: Long = 0L,
    val remainingAmount: Long = 0L,
    val currency: String = "EUR",
    val deductedBaseAmount: Long = 0L,
    val exchangeRate: String = "1",
    val createdBy: String = "",
    val createdByRef: DocumentReference? = null,
    var createdAt: Timestamp? = null,
    var lastUpdatedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION_PATH = "cash_withdrawals"
    }
}


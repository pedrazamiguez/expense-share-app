package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp

data class ExpenseDocument(
    val expenseId: String = "",
    val groupId: String = "",
    val groupRef: DocumentReference? = null,
    val operationDate: Timestamp? = null,
    val expenseCategory: String = "OTHER",
    val title: String = "",
    val description: String? = null,
    val vendor: String? = null,
    val amountCents: Long = 0L,
    val currency: String = "EUR",
    val groupCurrency: String = "EUR",
    val exchangeRate: Double? = null,
    val rateSource: String? = null,
    val rateTimestamp: Timestamp? = null,
    val groupAmountCents: Long? = null,
    val addOns: List<AddOnDocument> = emptyList(),
    val paymentMethod: String = "DEBIT_CARD",
    val paymentStatus: String = "FINISHED",
    val payerType: String = "GROUP",
    val payerId: String? = null,
    val payerRef: DocumentReference? = null,
    val paidAt: Timestamp? = null,
    val splits: List<ExpenseSplitDocument> = emptyList(),
    val attachments: List<AttachmentDocument> = emptyList(),
    val notes: String? = null,
    val createdBy: String = "",
    val createdByRef: DocumentReference? = null,
    @ServerTimestamp var createdAt: Timestamp? = null,
    val lastUpdatedBy: String? = null,
    @ServerTimestamp var lastUpdatedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION_PATH = "expenses"
    }
}

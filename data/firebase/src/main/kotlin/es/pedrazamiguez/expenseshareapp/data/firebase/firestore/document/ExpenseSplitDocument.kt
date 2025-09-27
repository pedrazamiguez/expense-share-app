package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

import com.google.firebase.firestore.DocumentReference

data class ExpenseSplitDocument(
    val userId: String = "",
    val userRef: DocumentReference? = null,
    val subunitId: String? = null,
    val subunitRef: DocumentReference? = null,
    val amountCents: Long? = null,
    val percentage: Double? = null,
    val isExcluded: Boolean = false,
    val isCoveredById: String? = null,
    val isCoveredByRef: DocumentReference? = null
)

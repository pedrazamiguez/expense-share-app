package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class ContributionDocument(
    val contributionId: String = "",
    val groupId: String = "",
    val groupRef: DocumentReference? = null,
    val userId: String = "",
    val amountCents: Long = 0L,
    val currency: String = "EUR",
    val createdBy: String = "",
    val createdByRef: DocumentReference? = null,
    var createdAt: Timestamp? = null,
    var lastUpdatedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION_PATH = "contributions"
    }
}


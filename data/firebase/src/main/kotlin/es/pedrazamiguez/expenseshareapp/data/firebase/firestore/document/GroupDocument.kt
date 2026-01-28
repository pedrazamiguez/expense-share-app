package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp

data class GroupDocument(
    val groupId: String = "",
    val name: String = "",
    val description: String = "",
    val currency: String = "EUR",
    val extraCurrencies: List<String> = emptyList(),
    val mainImagePath: String = "",
    val createdBy: String = "",
    val createdByRef: DocumentReference? = null,
    @ServerTimestamp
    var createdAt: Timestamp? = null,
    @ServerTimestamp
    var lastUpdatedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION_PATH = "groups"
    }
}

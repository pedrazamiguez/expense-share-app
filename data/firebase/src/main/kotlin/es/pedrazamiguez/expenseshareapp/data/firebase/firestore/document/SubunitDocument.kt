package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class SubunitDocument(
    val subunitId: String = "",
    val groupId: String = "",
    val groupRef: DocumentReference? = null,
    val name: String = "",
    val memberIds: List<String> = emptyList(),
    val memberShares: Map<String, Double> = emptyMap(),
    val createdBy: String = "",
    val createdByRef: DocumentReference? = null,
    var createdAt: Timestamp? = null,
    var lastUpdatedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION_PATH = "subunits"
    }
}

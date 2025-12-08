package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class UserDocument(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val displayName: String? = null,
    val profileImagePath: String? = null,
    val createdBy: String = "",
    @ServerTimestamp var createdAt: Timestamp? = null,
    val lastUpdatedBy: String? = null,
    @ServerTimestamp var lastUpdatedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION_PATH = "users"
        const val DEVICES_COLLECTION_PATH = "devices"
    }
}


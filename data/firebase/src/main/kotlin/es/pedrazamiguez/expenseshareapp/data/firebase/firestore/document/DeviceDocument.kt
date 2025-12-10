package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class DeviceDocument(
    val deviceId: String = "",
    val token: String = "",
    val platform: String = "android",
    val model: String = "",
    val androidVersion: String = "",
    @ServerTimestamp var lastUpdatedAt: Timestamp? = null
) {
    companion object {
        fun collectionPath(userId: String) = "users/$userId/devices"
        const val TOKEN_FIELD = "token"
    }
}

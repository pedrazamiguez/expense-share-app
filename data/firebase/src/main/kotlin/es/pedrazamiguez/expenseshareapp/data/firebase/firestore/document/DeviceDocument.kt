package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class DeviceDocument(
    val deviceId: String = "", // Unique Device ID (Installation ID)
    val token: String = "",
    val platform: String = "android",
    val model: String = "", // E.g., "Pixel 6"
    val androidVersion: String = "", // E.g., "14"
    @ServerTimestamp var lastUpdatedAt: Timestamp? = null
)


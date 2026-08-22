package es.pedrazamiguez.splittrip.data.firebase.firestore.document

import com.google.firebase.Timestamp

data class AttachmentDocument(
    val path: String = "",
    val mime: String? = null,
    val sizeBytes: Long? = null,
    val uploadedById: String? = null,
    val uploadedAt: Timestamp? = null
)

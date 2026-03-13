package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class AttachmentDocument(
    val path: String = "",
    val mime: String? = null,
    val sizeBytes: Long? = null,
    val uploadedByRef: DocumentReference? = null,
    val uploadedById: String? = null,
    val uploadedAt: Timestamp? = null
)

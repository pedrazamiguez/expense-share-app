package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp

data class ActivityLogDocument(
    val activityId: String = "",
    val type: String = "UNKNOWN",
    val byUserRef: DocumentReference? = null,
    val byUserId: String = "",
    val onGroupRef: DocumentReference? = null,
    val onGroupId: String = "",
    val targetExpenseRef: DocumentReference? = null,
    val targetExpenseId: String? = null,
    @ServerTimestamp var loggedAt: Timestamp? = null
)

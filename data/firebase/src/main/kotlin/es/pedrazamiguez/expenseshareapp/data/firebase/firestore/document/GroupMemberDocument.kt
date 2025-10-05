package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp

data class GroupMemberDocument(
    val memberId: String = "",
    val groupId: String = "",
    val groupRef: DocumentReference? = null,
    val userId: String = "",
    val userRef: DocumentReference? = null,
    val role: String = "MEMBER",
    val alias: String? = null,
    @ServerTimestamp var joinedAt: Timestamp? = null
) {
    companion object {
        fun collectionPath(groupId: String) = "groups/$groupId/members"
        const val SUBCOLLECTION_PATH = "members"
    }
}

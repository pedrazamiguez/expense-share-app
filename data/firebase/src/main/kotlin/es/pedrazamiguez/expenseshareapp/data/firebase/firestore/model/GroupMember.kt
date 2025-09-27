package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.enums.GroupRole

data class GroupMember(
    @PropertyName("userId") val userId: String = "",
    @PropertyName("groupId") val groupId: String = "",
    @PropertyName("role") val role: GroupRole = GroupRole.MEMBER,
    @PropertyName("alias") val alias: String? = null,
    @PropertyName("joinedAt") val joinedAt: Timestamp = Timestamp.now(),
)

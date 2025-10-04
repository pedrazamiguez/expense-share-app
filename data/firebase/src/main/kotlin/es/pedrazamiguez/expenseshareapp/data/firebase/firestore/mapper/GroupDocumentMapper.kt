package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupMemberDocument
import es.pedrazamiguez.expenseshareapp.domain.model.Group

fun Group.toDocument(
    groupId: String,
    userId: String
) = GroupDocument(
    groupId = groupId,
    name = name,
    description = description,
    currency = currency,
    createdBy = userId
)

fun toAdminMemberDocument(
    groupDocRef: DocumentReference,
    userId: String
) = GroupMemberDocument(
    memberId = userId,
    groupId = groupDocRef.id,
    groupRef = groupDocRef,
    userId = userId,
    role = "ADMIN"
)

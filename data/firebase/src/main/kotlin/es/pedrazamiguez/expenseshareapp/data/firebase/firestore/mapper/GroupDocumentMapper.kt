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
    extraCurrencies = extraCurrencies,
    memberIds = members,
    createdBy = userId,
    createdAt = createdAt?.toTimestampUtc(),
    lastUpdatedAt = lastUpdatedAt?.toTimestampUtc()
)

fun GroupDocument.toDomain() = Group(
    id = groupId,
    name = name,
    description = description,
    currency = currency,
    extraCurrencies = extraCurrencies,
    members = memberIds,
    mainImagePath = mainImagePath,
    createdAt = createdAt?.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAt?.toLocalDateTimeUtc()
)

fun toAdminMemberDocument(
    groupDocRef: DocumentReference,
    userId: String,
    addedBy: String = userId
) = GroupMemberDocument(
    memberId = userId,
    groupId = groupDocRef.id,
    groupRef = groupDocRef,
    userId = userId,
    role = "ADMIN",
    addedBy = addedBy
)

fun toRegularMemberDocument(
    groupDocRef: DocumentReference,
    memberId: String,
    addedBy: String
) = GroupMemberDocument(
    memberId = memberId,
    groupId = groupDocRef.id,
    groupRef = groupDocRef,
    userId = memberId,
    role = "MEMBER",
    addedBy = addedBy
)


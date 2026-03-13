package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.SubunitDocument
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import java.time.LocalDateTime

fun Subunit.toDocument(
    subunitId: String,
    groupId: String,
    groupDocRef: DocumentReference,
    userId: String
) = SubunitDocument(
    subunitId = subunitId,
    groupId = groupId,
    groupRef = groupDocRef,
    name = name,
    memberIds = memberIds,
    memberShares = memberShares,
    createdBy = userId,
    createdAt = (createdAt ?: LocalDateTime.now()).toTimestampUtc(),
    lastUpdatedAt = (lastUpdatedAt ?: LocalDateTime.now()).toTimestampUtc()
)

fun SubunitDocument.toDomain() = Subunit(
    id = subunitId,
    groupId = groupId,
    name = name,
    memberIds = memberIds,
    memberShares = memberShares,
    createdBy = createdBy,
    createdAt = createdAt.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAt.toLocalDateTimeUtc()
)

fun List<SubunitDocument>.toDomainSubunits(): List<Subunit> = map { it.toDomain() }


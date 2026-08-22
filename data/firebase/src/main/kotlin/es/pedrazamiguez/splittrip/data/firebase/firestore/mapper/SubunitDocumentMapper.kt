package es.pedrazamiguez.splittrip.data.firebase.firestore.mapper

import es.pedrazamiguez.splittrip.data.firebase.firestore.document.SubunitDocument
import es.pedrazamiguez.splittrip.domain.model.Subunit
import java.math.BigDecimal
import java.time.LocalDateTime

fun Subunit.toDocument(
    subunitId: String,
    groupId: String,
    userId: String
): SubunitDocument {
    val now = LocalDateTime.now()
    return SubunitDocument(
        subunitId = subunitId,
        groupId = groupId,
        name = name,
        memberIds = memberIds,
        memberShares = memberShares.mapValues { it.value.toPlainString() },
        createdBy = createdBy.ifBlank { userId },
        createdAt = (createdAt ?: now).toTimestampUtc(),
        lastUpdatedAt = (lastUpdatedAt ?: now).toTimestampUtc()
    )
}

fun SubunitDocument.toDomain() = Subunit(
    id = subunitId,
    groupId = groupId,
    name = name,
    memberIds = memberIds.sorted(),
    memberShares = memberShares.mapValues { it.value.toBigDecimalOrNull() ?: BigDecimal.ZERO },
    createdBy = createdBy,
    createdAt = createdAt.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAt.toLocalDateTimeUtc()
)

fun List<SubunitDocument>.toDomainSubunits(): List<Subunit> = map { it.toDomain() }

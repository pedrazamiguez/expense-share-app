package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.core.common.extensions.toEpochMillisUtc
import es.pedrazamiguez.splittrip.core.common.extensions.toLocalDateTimeUtc
import es.pedrazamiguez.splittrip.data.local.entity.SubunitEntity
import es.pedrazamiguez.splittrip.domain.model.Subunit

fun SubunitEntity.toDomain(): Subunit = Subunit(
    id = id,
    groupId = groupId,
    name = name,
    memberIds = memberIds.sorted(),
    memberShares = memberShares,
    createdBy = createdBy,
    createdAt = createdAtMillis?.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAtMillis?.toLocalDateTimeUtc()
)

fun Subunit.toEntity(): SubunitEntity {
    val effectiveCreatedAtMillis = createdAt?.toEpochMillisUtc() ?: System.currentTimeMillis()
    val effectiveLastUpdatedAtMillis = lastUpdatedAt?.toEpochMillisUtc() ?: effectiveCreatedAtMillis

    return SubunitEntity(
        id = id,
        groupId = groupId,
        name = name,
        memberIds = memberIds,
        memberShares = memberShares,
        createdBy = createdBy,
        createdAtMillis = effectiveCreatedAtMillis,
        lastUpdatedAtMillis = effectiveLastUpdatedAtMillis
    )
}

fun List<SubunitEntity>.toDomain(): List<Subunit> = map { it.toDomain() }

fun List<Subunit>.toEntity(): List<SubunitEntity> = map { it.toEntity() }

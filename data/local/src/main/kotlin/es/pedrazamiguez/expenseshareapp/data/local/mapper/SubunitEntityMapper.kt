package es.pedrazamiguez.expenseshareapp.data.local.mapper

import es.pedrazamiguez.expenseshareapp.data.local.entity.SubunitEntity
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun SubunitEntity.toDomain(): Subunit = Subunit(
    id = id,
    groupId = groupId,
    name = name,
    memberIds = memberIds,
    memberShares = memberShares,
    createdBy = createdBy,
    createdAt = createdAtMillis?.toLocalDateTime(),
    lastUpdatedAt = lastUpdatedAtMillis?.toLocalDateTime()
)

fun Subunit.toEntity(): SubunitEntity {
    val effectiveCreatedAtMillis = createdAt?.toEpochMillis() ?: System.currentTimeMillis()
    val effectiveLastUpdatedAtMillis = lastUpdatedAt?.toEpochMillis() ?: effectiveCreatedAtMillis

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

private fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

private fun LocalDateTime.toEpochMillis(): Long = this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

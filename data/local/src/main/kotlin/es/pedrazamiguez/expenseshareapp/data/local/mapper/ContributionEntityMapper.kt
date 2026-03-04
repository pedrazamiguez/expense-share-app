package es.pedrazamiguez.expenseshareapp.data.local.mapper

import es.pedrazamiguez.expenseshareapp.data.local.entity.ContributionEntity
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun ContributionEntity.toDomain(): Contribution = Contribution(
    id = id,
    groupId = groupId,
    userId = userId,
    amount = amount,
    currency = currency,
    createdAt = createdAtMillis?.toLocalDateTime(),
    lastUpdatedAt = lastUpdatedAtMillis?.toLocalDateTime()
)

fun Contribution.toEntity(): ContributionEntity {
    val effectiveCreatedAtMillis = createdAt?.toEpochMillis() ?: System.currentTimeMillis()
    val effectiveLastUpdatedAtMillis = lastUpdatedAt?.toEpochMillis() ?: effectiveCreatedAtMillis

    return ContributionEntity(
        id = id,
        groupId = groupId,
        userId = userId,
        amount = amount,
        currency = currency,
        createdAtMillis = effectiveCreatedAtMillis,
        lastUpdatedAtMillis = effectiveLastUpdatedAtMillis
    )
}

fun List<ContributionEntity>.toDomain(): List<Contribution> = map { it.toDomain() }

fun List<Contribution>.toEntity(): List<ContributionEntity> = map { it.toEntity() }

private fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

private fun LocalDateTime.toEpochMillis(): Long =
    this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()


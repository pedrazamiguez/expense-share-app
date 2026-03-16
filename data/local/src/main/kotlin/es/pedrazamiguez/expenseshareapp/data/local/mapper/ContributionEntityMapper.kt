package es.pedrazamiguez.expenseshareapp.data.local.mapper

import es.pedrazamiguez.expenseshareapp.core.common.extensions.toEpochMillisUtc
import es.pedrazamiguez.expenseshareapp.core.common.extensions.toLocalDateTimeUtc
import es.pedrazamiguez.expenseshareapp.data.local.entity.ContributionEntity
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution

fun ContributionEntity.toDomain(): Contribution = Contribution(
    id = id,
    groupId = groupId,
    userId = userId,
    subunitId = subunitId,
    amount = amount,
    currency = currency,
    createdAt = createdAtMillis?.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAtMillis?.toLocalDateTimeUtc()
)

fun Contribution.toEntity(): ContributionEntity {
    val effectiveCreatedAtMillis = createdAt?.toEpochMillisUtc() ?: System.currentTimeMillis()
    val effectiveLastUpdatedAtMillis = lastUpdatedAt?.toEpochMillisUtc() ?: effectiveCreatedAtMillis

    return ContributionEntity(
        id = id,
        groupId = groupId,
        userId = userId,
        subunitId = subunitId,
        amount = amount,
        currency = currency,
        createdAtMillis = effectiveCreatedAtMillis,
        lastUpdatedAtMillis = effectiveLastUpdatedAtMillis
    )
}

fun List<ContributionEntity>.toDomain(): List<Contribution> = map { it.toDomain() }

fun List<Contribution>.toEntity(): List<ContributionEntity> = map { it.toEntity() }

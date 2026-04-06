package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.core.common.extensions.toEpochMillisUtc
import es.pedrazamiguez.splittrip.core.common.extensions.toLocalDateTimeUtc
import es.pedrazamiguez.splittrip.data.local.entity.ContributionEntity
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.Contribution

fun ContributionEntity.toDomain(): Contribution = Contribution(
    id = id,
    groupId = groupId,
    userId = userId,
    createdBy = createdBy,
    contributionScope = runCatching { PayerType.fromString(contributionScope) }.getOrDefault(PayerType.USER),
    subunitId = subunitId,
    linkedExpenseId = linkedExpenseId,
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
        createdBy = createdBy,
        contributionScope = contributionScope.name,
        subunitId = subunitId,
        linkedExpenseId = linkedExpenseId,
        amount = amount,
        currency = currency,
        createdAtMillis = effectiveCreatedAtMillis,
        lastUpdatedAtMillis = effectiveLastUpdatedAtMillis
    )
}

fun List<ContributionEntity>.toDomain(): List<Contribution> = map { it.toDomain() }

fun List<Contribution>.toEntity(): List<ContributionEntity> = map { it.toEntity() }

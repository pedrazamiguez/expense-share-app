package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.core.common.extensions.toEpochMillisUtc
import es.pedrazamiguez.splittrip.core.common.extensions.toLocalDateTimeUtc
import es.pedrazamiguez.splittrip.data.local.entity.SettlementRecordEntity
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus

fun SettlementRecordEntity.toDomain(): SettlementRecord = SettlementRecord(
    id = id,
    groupId = groupId,
    settlement = Settlement(
        fromUserId = fromUserId,
        toUserId = toUserId,
        amount = amountCents,
        currency = currency,
        sourcePocket = SettlementPocketType.valueOf(sourcePocket)
    ),
    status = SettlementStatus.fromString(status),
    createdAt = createdAt.toLocalDateTimeUtc(),
    confirmedByPayerAt = confirmedByPayerAt?.toLocalDateTimeUtc(),
    confirmedByPayeeAt = confirmedByPayeeAt?.toLocalDateTimeUtc(),
    resolvedAt = resolvedAt?.toLocalDateTimeUtc(),
    disputedBy = disputedBy,
    disputeReason = disputeReason
)

fun SettlementRecord.toEntity(
    syncStatus: SyncStatus = SyncStatus.SYNCED
): SettlementRecordEntity = SettlementRecordEntity(
    id = id,
    groupId = groupId,
    fromUserId = settlement.fromUserId,
    toUserId = settlement.toUserId,
    amountCents = settlement.amount,
    currency = settlement.currency,
    sourcePocket = settlement.sourcePocket.name,
    status = status.name,
    syncStatus = syncStatus.name,
    createdAt = createdAt.toEpochMillisUtc(),
    confirmedByPayerAt = confirmedByPayerAt?.toEpochMillisUtc(),
    confirmedByPayeeAt = confirmedByPayeeAt?.toEpochMillisUtc(),
    resolvedAt = resolvedAt?.toEpochMillisUtc(),
    disputedBy = disputedBy,
    disputeReason = disputeReason
)

fun List<SettlementRecordEntity>.toDomain(): List<SettlementRecord> = map { it.toDomain() }

fun List<SettlementRecord>.toEntity(
    syncStatus: SyncStatus = SyncStatus.SYNCED
): List<SettlementRecordEntity> = map { it.toEntity(syncStatus) }

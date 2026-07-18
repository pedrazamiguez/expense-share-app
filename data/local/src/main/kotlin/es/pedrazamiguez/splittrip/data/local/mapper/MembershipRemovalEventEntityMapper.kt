package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.core.common.extensions.toEpochMillisUtc
import es.pedrazamiguez.splittrip.core.common.extensions.toLocalDateTimeUtc
import es.pedrazamiguez.splittrip.data.local.entity.MembershipRemovalEventEntity
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.MembershipRemovalEvent

fun MembershipRemovalEventEntity.toDomain(): MembershipRemovalEvent = MembershipRemovalEvent(
    id = id,
    groupId = groupId,
    userId = userId,
    createdAt = createdAtMillis.toLocalDateTimeUtc(),
    processed = processed,
    syncStatus = SyncStatus.fromStringOrDefault(syncStatus)
)

fun MembershipRemovalEvent.toEntity(): MembershipRemovalEventEntity = MembershipRemovalEventEntity(
    id = id,
    groupId = groupId,
    userId = userId,
    createdAtMillis = createdAt.toEpochMillisUtc(),
    processed = processed,
    syncStatus = syncStatus.name
)

fun List<MembershipRemovalEventEntity>.toDomain(): List<MembershipRemovalEvent> = map { it.toDomain() }

fun List<MembershipRemovalEvent>.toEntity(): List<MembershipRemovalEventEntity> = map { it.toEntity() }

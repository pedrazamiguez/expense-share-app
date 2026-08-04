package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.data.local.entity.CashTransferEntity
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.CashTransfer

fun CashTransfer.toEntity(syncStatus: SyncStatus): CashTransferEntity {
    return CashTransferEntity(
        id = id,
        groupId = groupId,
        fromUserId = fromUserId,
        toUserId = toUserId,
        amountCents = amountCents,
        currency = currency,
        equivalentBaseAmountCents = equivalentBaseAmountCents,
        createdAt = createdAt,
        syncStatus = syncStatus
    )
}

fun CashTransferEntity.toDomain(): CashTransfer {
    return CashTransfer(
        id = id,
        groupId = groupId,
        fromUserId = fromUserId,
        toUserId = toUserId,
        amountCents = amountCents,
        currency = currency,
        equivalentBaseAmountCents = equivalentBaseAmountCents,
        createdAt = createdAt
    )
}

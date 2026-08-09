package es.pedrazamiguez.splittrip.data.firebase.firestore.mapper

import es.pedrazamiguez.splittrip.data.firebase.firestore.document.CashTransferDocument
import es.pedrazamiguez.splittrip.domain.model.CashTransfer

fun CashTransfer.toDocument(): CashTransferDocument {
    return CashTransferDocument(
        groupId = groupId,
        fromUserId = fromUserId,
        toUserId = toUserId,
        amountCents = amountCents,
        currency = currency,
        equivalentBaseAmountCents = equivalentBaseAmountCents,
        createdAt = createdAt
    )
}

fun CashTransferDocument.toDomain(id: String): CashTransfer {
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

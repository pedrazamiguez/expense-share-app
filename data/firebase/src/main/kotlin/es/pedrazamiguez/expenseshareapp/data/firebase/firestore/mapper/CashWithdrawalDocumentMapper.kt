package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.CashWithdrawalDocument
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal

fun CashWithdrawal.toDocument(
    withdrawalId: String,
    groupId: String,
    groupDocRef: DocumentReference,
    userId: String
) = CashWithdrawalDocument(
    withdrawalId = withdrawalId,
    groupId = groupId,
    groupRef = groupDocRef,
    withdrawnBy = withdrawnBy.ifBlank { userId },
    amountWithdrawn = amountWithdrawn,
    remainingAmount = remainingAmount,
    currency = currency,
    deductedBaseAmount = deductedBaseAmount,
    exchangeRate = exchangeRate,
    createdBy = userId
)

fun CashWithdrawalDocument.toDomain() = CashWithdrawal(
    id = withdrawalId,
    groupId = groupId,
    withdrawnBy = withdrawnBy,
    amountWithdrawn = amountWithdrawn,
    remainingAmount = remainingAmount,
    currency = currency,
    deductedBaseAmount = deductedBaseAmount,
    exchangeRate = exchangeRate,
    createdAt = createdAt.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAt.toLocalDateTimeUtc()
)


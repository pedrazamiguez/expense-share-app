package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.CashWithdrawalDocument
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import java.math.BigDecimal
import java.time.LocalDateTime

fun CashWithdrawal.toDocument(withdrawalId: String, groupId: String, groupDocRef: DocumentReference, userId: String) =
    CashWithdrawalDocument(
        withdrawalId = withdrawalId,
        groupId = groupId,
        groupRef = groupDocRef,
        withdrawnBy = withdrawnBy.ifBlank { userId },
        withdrawalScope = withdrawalScope.name,
        subunitId = subunitId,
        amountWithdrawn = amountWithdrawn,
        remainingAmount = remainingAmount,
        currency = currency,
        deductedBaseAmount = deductedBaseAmount,
        exchangeRate = exchangeRate.toPlainString(),
        createdBy = userId,
        createdAt = (createdAt ?: LocalDateTime.now()).toTimestampUtc(),
        lastUpdatedAt = (lastUpdatedAt ?: LocalDateTime.now()).toTimestampUtc()
    )

fun CashWithdrawalDocument.toDomain() = CashWithdrawal(
    id = withdrawalId,
    groupId = groupId,
    withdrawnBy = withdrawnBy,
    withdrawalScope = runCatching { PayerType.fromString(withdrawalScope) }.getOrDefault(PayerType.GROUP),
    subunitId = subunitId,
    amountWithdrawn = amountWithdrawn,
    remainingAmount = remainingAmount,
    currency = currency,
    deductedBaseAmount = deductedBaseAmount,
    exchangeRate = BigDecimal(exchangeRate),
    createdAt = createdAt.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAt.toLocalDateTimeUtc()
)

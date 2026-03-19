package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.AddOnDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.CashWithdrawalDocument
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.AddOn
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
        addOns = addOns.map { it.toAddOnDocument() },
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
    addOns = addOns.map { it.toDomainAddOn() },
    createdAt = createdAt.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAt.toLocalDateTimeUtc()
)

// ── AddOn ↔ AddOnDocument mappers ────────────────────────────────────

private fun AddOn.toAddOnDocument() = AddOnDocument(
    id = id,
    type = type.name,
    mode = mode.name,
    valueType = valueType.name,
    amountCents = amountCents,
    currency = currency,
    exchangeRate = exchangeRate.toPlainString(),
    groupAmountCents = groupAmountCents,
    paymentMethod = paymentMethod.name,
    description = description
)

private fun AddOnDocument.toDomainAddOn() = AddOn(
    id = id,
    type = runCatching { AddOnType.fromString(type) }.getOrDefault(AddOnType.FEE),
    mode = runCatching { AddOnMode.fromString(mode) }.getOrDefault(AddOnMode.ON_TOP),
    valueType = runCatching { AddOnValueType.fromString(valueType) }.getOrDefault(AddOnValueType.EXACT),
    amountCents = amountCents,
    currency = currency,
    exchangeRate = exchangeRate?.toBigDecimalOrNull() ?: BigDecimal.ONE,
    groupAmountCents = groupAmountCents,
    paymentMethod = runCatching { PaymentMethod.fromString(paymentMethod) }.getOrDefault(
        PaymentMethod.OTHER
    ),
    description = description
)

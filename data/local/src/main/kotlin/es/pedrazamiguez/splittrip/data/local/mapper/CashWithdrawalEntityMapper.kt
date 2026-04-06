package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.core.common.extensions.toEpochMillisUtc
import es.pedrazamiguez.splittrip.core.common.extensions.toLocalDateTimeUtc
import es.pedrazamiguez.splittrip.data.local.converter.AddOnListConverter
import es.pedrazamiguez.splittrip.data.local.entity.CashWithdrawalEntity
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import java.math.BigDecimal

private val addOnConverter = AddOnListConverter()

fun CashWithdrawalEntity.toDomain(): CashWithdrawal = CashWithdrawal(
    id = id,
    groupId = groupId,
    withdrawnBy = withdrawnBy,
    createdBy = createdBy,
    withdrawalScope = runCatching { PayerType.fromString(withdrawalScope) }.getOrDefault(PayerType.GROUP),
    subunitId = subunitId,
    amountWithdrawn = amountWithdrawn,
    remainingAmount = remainingAmount,
    currency = currency,
    deductedBaseAmount = deductedBaseAmount,
    exchangeRate = exchangeRate.toBigDecimalOrNull() ?: BigDecimal.ONE,
    addOns = addOnConverter.toAddOnList(addOnsJson) ?: emptyList(),
    title = title,
    notes = notes,
    receiptLocalUri = receiptLocalUri,
    createdAt = createdAtMillis?.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAtMillis?.toLocalDateTimeUtc()
)

fun CashWithdrawal.toEntity(): CashWithdrawalEntity {
    val effectiveCreatedAtMillis = createdAt?.toEpochMillisUtc() ?: System.currentTimeMillis()
    val effectiveLastUpdatedAtMillis = lastUpdatedAt?.toEpochMillisUtc() ?: effectiveCreatedAtMillis

    return CashWithdrawalEntity(
        id = id,
        groupId = groupId,
        withdrawnBy = withdrawnBy,
        createdBy = createdBy,
        withdrawalScope = withdrawalScope.name,
        subunitId = subunitId,
        amountWithdrawn = amountWithdrawn,
        remainingAmount = remainingAmount,
        currency = currency,
        deductedBaseAmount = deductedBaseAmount,
        exchangeRate = exchangeRate.toPlainString(),
        createdAtMillis = effectiveCreatedAtMillis,
        lastUpdatedAtMillis = effectiveLastUpdatedAtMillis,
        addOnsJson = addOnConverter.fromAddOnList(addOns.ifEmpty { null }),
        title = title,
        notes = notes,
        receiptLocalUri = receiptLocalUri
    )
}

fun List<CashWithdrawalEntity>.toDomain(): List<CashWithdrawal> = map { it.toDomain() }

fun List<CashWithdrawal>.toEntity(): List<CashWithdrawalEntity> = map { it.toEntity() }

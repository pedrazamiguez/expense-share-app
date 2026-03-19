package es.pedrazamiguez.expenseshareapp.data.local.mapper

import es.pedrazamiguez.expenseshareapp.core.common.extensions.toEpochMillisUtc
import es.pedrazamiguez.expenseshareapp.core.common.extensions.toLocalDateTimeUtc
import es.pedrazamiguez.expenseshareapp.data.local.converter.AddOnListConverter
import es.pedrazamiguez.expenseshareapp.data.local.entity.CashWithdrawalEntity
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import java.math.BigDecimal

private val addOnConverter = AddOnListConverter()

fun CashWithdrawalEntity.toDomain(): CashWithdrawal = CashWithdrawal(
    id = id,
    groupId = groupId,
    withdrawnBy = withdrawnBy,
    withdrawalScope = runCatching { PayerType.fromString(withdrawalScope) }.getOrDefault(PayerType.GROUP),
    subunitId = subunitId,
    amountWithdrawn = amountWithdrawn,
    remainingAmount = remainingAmount,
    currency = currency,
    deductedBaseAmount = deductedBaseAmount,
    exchangeRate = exchangeRate.toBigDecimalOrNull() ?: BigDecimal.ONE,
    addOns = addOnConverter.toAddOnList(addOnsJson) ?: emptyList(),
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
        withdrawalScope = withdrawalScope.name,
        subunitId = subunitId,
        amountWithdrawn = amountWithdrawn,
        remainingAmount = remainingAmount,
        currency = currency,
        deductedBaseAmount = deductedBaseAmount,
        exchangeRate = exchangeRate.toPlainString(),
        createdAtMillis = effectiveCreatedAtMillis,
        lastUpdatedAtMillis = effectiveLastUpdatedAtMillis,
        addOnsJson = addOnConverter.fromAddOnList(addOns.ifEmpty { null })
    )
}

fun List<CashWithdrawalEntity>.toDomain(): List<CashWithdrawal> = map { it.toDomain() }

fun List<CashWithdrawal>.toEntity(): List<CashWithdrawalEntity> = map { it.toEntity() }

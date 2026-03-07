package es.pedrazamiguez.expenseshareapp.data.local.mapper

import es.pedrazamiguez.expenseshareapp.data.local.entity.CashWithdrawalEntity
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun CashWithdrawalEntity.toDomain(): CashWithdrawal = CashWithdrawal(
    id = id,
    groupId = groupId,
    withdrawnBy = withdrawnBy,
    amountWithdrawn = amountWithdrawn,
    remainingAmount = remainingAmount,
    currency = currency,
    deductedBaseAmount = deductedBaseAmount,
    exchangeRate = exchangeRate.toBigDecimalOrNull() ?: BigDecimal.ONE,
    createdAt = createdAtMillis?.toLocalDateTime(),
    lastUpdatedAt = lastUpdatedAtMillis?.toLocalDateTime()
)

fun CashWithdrawal.toEntity(): CashWithdrawalEntity {
    val effectiveCreatedAtMillis = createdAt?.toEpochMillis() ?: System.currentTimeMillis()
    val effectiveLastUpdatedAtMillis = lastUpdatedAt?.toEpochMillis() ?: effectiveCreatedAtMillis

    return CashWithdrawalEntity(
        id = id,
        groupId = groupId,
        withdrawnBy = withdrawnBy,
        amountWithdrawn = amountWithdrawn,
        remainingAmount = remainingAmount,
        currency = currency,
        deductedBaseAmount = deductedBaseAmount,
        exchangeRate = exchangeRate.toPlainString(),
        createdAtMillis = effectiveCreatedAtMillis,
        lastUpdatedAtMillis = effectiveLastUpdatedAtMillis
    )
}

fun List<CashWithdrawalEntity>.toDomain(): List<CashWithdrawal> = map { it.toDomain() }

fun List<CashWithdrawal>.toEntity(): List<CashWithdrawalEntity> = map { it.toEntity() }

private fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

private fun LocalDateTime.toEpochMillis(): Long =
    this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()


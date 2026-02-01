package es.pedrazamiguez.expenseshareapp.data.local.mapper

import es.pedrazamiguez.expenseshareapp.data.local.entity.ExpenseEntity
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    groupId = groupId,
    title = title,
    sourceAmount = sourceAmount,
    sourceCurrency = sourceCurrency,
    sourceTipAmount = sourceTipAmount,
    sourceFeeAmount = sourceFeeAmount,
    groupAmount = groupAmount,
    groupCurrency = groupCurrency,
    exchangeRate = exchangeRate,
    paymentMethod = PaymentMethod.entries.find { it.name == paymentMethod } ?: PaymentMethod.OTHER,
    createdBy = createdBy,
    payerType = payerType,
    createdAt = createdAtMillis?.toLocalDateTime(),
    lastUpdatedAt = lastUpdatedAtMillis?.toLocalDateTime()
)

fun Expense.toEntity(): ExpenseEntity {
    val effectiveCreatedAtMillis = createdAt?.toEpochMillis() ?: System.currentTimeMillis()
    val effectiveLastUpdatedAtMillis = lastUpdatedAt?.toEpochMillis() ?: effectiveCreatedAtMillis

    return ExpenseEntity(
        id = id,
        groupId = groupId,
        title = title,
        sourceAmount = sourceAmount,
        sourceCurrency = sourceCurrency,
        sourceTipAmount = sourceTipAmount,
        sourceFeeAmount = sourceFeeAmount,
        groupAmount = groupAmount,
        groupCurrency = groupCurrency,
        exchangeRate = exchangeRate,
        paymentMethod = paymentMethod.name,
        createdBy = createdBy,
        payerType = payerType,
        createdAtMillis = effectiveCreatedAtMillis,
        lastUpdatedAtMillis = effectiveLastUpdatedAtMillis
    )
}

fun List<ExpenseEntity>.toDomain(): List<Expense> = map { it.toDomain() }

fun List<Expense>.toEntity(): List<ExpenseEntity> = map { it.toEntity() }

private fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

private fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

package es.pedrazamiguez.expenseshareapp.data.local.mapper

import es.pedrazamiguez.expenseshareapp.data.local.converter.CashTrancheListConverter
import es.pedrazamiguez.expenseshareapp.data.local.entity.ExpenseEntity
import es.pedrazamiguez.expenseshareapp.domain.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

private val cashTrancheConverter = CashTrancheListConverter()

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
    exchangeRate = exchangeRate.toBigDecimalOrNull() ?: BigDecimal.ONE,
    category = category?.let {
        runCatching { ExpenseCategory.fromString(it) }.getOrDefault(ExpenseCategory.OTHER)
    } ?: ExpenseCategory.OTHER,
    vendor = vendor,
    paymentMethod = PaymentMethod.entries.find { it.name == paymentMethod } ?: PaymentMethod.OTHER,
    paymentStatus = paymentStatus?.let {
        runCatching { PaymentStatus.fromString(it) }.getOrDefault(PaymentStatus.FINISHED)
    } ?: PaymentStatus.FINISHED,
    dueDate = dueDateMillis?.toLocalDateTime(),
    receiptLocalUri = receiptLocalUri,
    cashTranches = cashTrancheConverter.toCashTrancheList(cashTranchesJson) ?: emptyList(),
    splitType = runCatching { SplitType.fromString(splitType) }.getOrDefault(SplitType.EQUAL),
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
        exchangeRate = exchangeRate.toPlainString(),
        category = category.name,
        vendor = vendor,
        paymentMethod = paymentMethod.name,
        paymentStatus = paymentStatus.name,
        dueDateMillis = dueDate?.toEpochMillis(),
        receiptLocalUri = receiptLocalUri,
        createdBy = createdBy,
        payerType = payerType,
        splitType = splitType.name,
        createdAtMillis = effectiveCreatedAtMillis,
        lastUpdatedAtMillis = effectiveLastUpdatedAtMillis,
        cashTranchesJson = cashTrancheConverter.fromCashTrancheList(cashTranches.ifEmpty { null })
    )
}

fun List<ExpenseEntity>.toDomain(): List<Expense> = map { it.toDomain() }

fun List<Expense>.toEntity(): List<ExpenseEntity> = map { it.toEntity() }

private fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

private fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

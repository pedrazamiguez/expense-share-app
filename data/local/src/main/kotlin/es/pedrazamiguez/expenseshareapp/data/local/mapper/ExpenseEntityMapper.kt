package es.pedrazamiguez.expenseshareapp.data.local.mapper

import es.pedrazamiguez.expenseshareapp.core.common.extensions.toEpochMillisUtc
import es.pedrazamiguez.expenseshareapp.core.common.extensions.toLocalDateTimeUtc
import es.pedrazamiguez.expenseshareapp.data.local.converter.AddOnListConverter
import es.pedrazamiguez.expenseshareapp.data.local.converter.CashTrancheListConverter
import es.pedrazamiguez.expenseshareapp.data.local.entity.ExpenseEntity
import es.pedrazamiguez.expenseshareapp.domain.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import java.math.BigDecimal

private val cashTrancheConverter = CashTrancheListConverter()
private val addOnConverter = AddOnListConverter()

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    groupId = groupId,
    title = title,
    sourceAmount = sourceAmount,
    sourceCurrency = sourceCurrency,
    groupAmount = groupAmount,
    groupCurrency = groupCurrency,
    exchangeRate = exchangeRate.toBigDecimalOrNull() ?: BigDecimal.ONE,
    category = category?.let {
        runCatching { ExpenseCategory.fromString(it) }.getOrDefault(ExpenseCategory.OTHER)
    } ?: ExpenseCategory.OTHER,
    vendor = vendor,
    notes = notes,
    paymentMethod = PaymentMethod.entries.find { it.name == paymentMethod } ?: PaymentMethod.OTHER,
    paymentStatus = paymentStatus?.let {
        runCatching { PaymentStatus.fromString(it) }.getOrDefault(PaymentStatus.FINISHED)
    } ?: PaymentStatus.FINISHED,
    dueDate = dueDateMillis?.toLocalDateTimeUtc(),
    receiptLocalUri = receiptLocalUri,
    cashTranches = cashTrancheConverter.toCashTrancheList(cashTranchesJson) ?: emptyList(),
    addOns = addOnConverter.toAddOnList(addOnsJson) ?: emptyList(),
    splitType = runCatching { SplitType.fromString(splitType) }.getOrDefault(SplitType.EQUAL),
    createdBy = createdBy,
    payerType = runCatching { PayerType.fromString(payerType) }.getOrDefault(PayerType.GROUP),
    payerId = payerId,
    createdAt = createdAtMillis?.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAtMillis?.toLocalDateTimeUtc()
)

fun Expense.toEntity(): ExpenseEntity {
    val effectiveCreatedAtMillis = createdAt?.toEpochMillisUtc() ?: System.currentTimeMillis()
    val effectiveLastUpdatedAtMillis = lastUpdatedAt?.toEpochMillisUtc() ?: effectiveCreatedAtMillis

    return ExpenseEntity(
        id = id,
        groupId = groupId,
        title = title,
        sourceAmount = sourceAmount,
        sourceCurrency = sourceCurrency,
        groupAmount = groupAmount,
        groupCurrency = groupCurrency,
        exchangeRate = exchangeRate.toPlainString(),
        category = category.name,
        vendor = vendor,
        notes = notes,
        paymentMethod = paymentMethod.name,
        paymentStatus = paymentStatus.name,
        dueDateMillis = dueDate?.toEpochMillisUtc(),
        receiptLocalUri = receiptLocalUri,
        createdBy = createdBy,
        payerType = payerType.name,
        payerId = payerId,
        splitType = splitType.name,
        createdAtMillis = effectiveCreatedAtMillis,
        lastUpdatedAtMillis = effectiveLastUpdatedAtMillis,
        cashTranchesJson = cashTrancheConverter.fromCashTrancheList(cashTranches.ifEmpty { null }),
        addOnsJson = addOnConverter.fromAddOnList(addOns.ifEmpty { null })
    )
}

fun List<ExpenseEntity>.toDomain(): List<Expense> = map { it.toDomain() }

fun List<Expense>.toEntity(): List<ExpenseEntity> = map { it.toEntity() }

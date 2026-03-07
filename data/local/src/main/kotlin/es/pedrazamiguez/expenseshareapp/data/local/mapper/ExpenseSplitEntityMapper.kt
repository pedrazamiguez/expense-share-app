package es.pedrazamiguez.expenseshareapp.data.local.mapper

import es.pedrazamiguez.expenseshareapp.data.local.entity.ExpenseSplitEntity
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import java.math.BigDecimal

fun ExpenseSplitEntity.toDomain(): ExpenseSplit = ExpenseSplit(
    userId = userId,
    amountCents = amountCents,
    percentage = percentage?.let { BigDecimal(it) },
    isExcluded = isExcluded,
    isCoveredById = isCoveredById
)

fun ExpenseSplit.toEntity(expenseId: String): ExpenseSplitEntity = ExpenseSplitEntity(
    expenseId = expenseId,
    userId = userId,
    amountCents = amountCents,
    percentage = percentage?.toPlainString(),
    isExcluded = isExcluded,
    isCoveredById = isCoveredById
)

fun List<ExpenseSplitEntity>.toDomainSplits(): List<ExpenseSplit> = map { it.toDomain() }

fun List<ExpenseSplit>.toSplitEntities(expenseId: String): List<ExpenseSplitEntity> =
    map { it.toEntity(expenseId) }


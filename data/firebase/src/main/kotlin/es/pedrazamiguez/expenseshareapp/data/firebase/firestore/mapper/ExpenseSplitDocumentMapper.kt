package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.ExpenseSplitDocument
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import java.math.BigDecimal

fun ExpenseSplitDocument.toDomain(): ExpenseSplit = ExpenseSplit(
    userId = userId,
    amountCents = amountCents ?: 0L,
    percentage = percentage?.let { BigDecimal.valueOf(it) },
    isExcluded = isExcluded,
    isCoveredById = isCoveredById
)

fun ExpenseSplit.toDocument(): ExpenseSplitDocument = ExpenseSplitDocument(
    userId = userId,
    amountCents = amountCents,
    percentage = percentage?.toDouble(),
    isExcluded = isExcluded,
    isCoveredById = isCoveredById
)

fun List<ExpenseSplitDocument>.toDomainSplits(): List<ExpenseSplit> = map { it.toDomain() }

fun List<ExpenseSplit>.toSplitDocuments(): List<ExpenseSplitDocument> = map { it.toDocument() }

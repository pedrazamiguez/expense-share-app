package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.ExpenseSplitDocument
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit

fun ExpenseSplitDocument.toDomain(): ExpenseSplit = ExpenseSplit(
    userId = userId,
    amountCents = amountCents ?: 0L,
    percentage = percentage?.toBigDecimalOrNull(),
    isExcluded = isExcluded,
    isCoveredById = isCoveredById
)

fun ExpenseSplit.toDocument(): ExpenseSplitDocument = ExpenseSplitDocument(
    userId = userId,
    amountCents = amountCents,
    percentage = percentage?.toPlainString(),
    isExcluded = isExcluded,
    isCoveredById = isCoveredById
)

fun List<ExpenseSplitDocument>.toDomainSplits(): List<ExpenseSplit> = map { it.toDomain() }

fun List<ExpenseSplit>.toSplitDocuments(): List<ExpenseSplitDocument> = map { it.toDocument() }

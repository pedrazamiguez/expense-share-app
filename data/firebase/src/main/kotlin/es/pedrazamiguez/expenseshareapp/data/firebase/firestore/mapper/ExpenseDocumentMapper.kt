package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.ExpenseDocument
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import java.time.LocalDateTime

fun Expense.toDocument(
    expenseId: String, groupId: String, groupDocRef: DocumentReference, userId: String
) = ExpenseDocument(
    expenseId = expenseId,
    groupId = groupId,
    groupRef = groupDocRef,
    title = title,
    amountCents = amountCents,
    operationDate = LocalDateTime
        .now()
        .toTimestampUtc(),
    currency = currency,
    createdBy = userId,
    lastUpdatedBy = userId
)

fun ExpenseDocument.toDomain() = Expense(
    id = expenseId,
    groupId = groupId,
    title = title,
    amountCents = amountCents,
    currency = currency,
    createdBy = createdBy,
    payerType = payerType,
    createdAt = createdAt.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAt.toLocalDateTimeUtc()
)

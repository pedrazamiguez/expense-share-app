package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.ExpenseDocument
import es.pedrazamiguez.expenseshareapp.domain.model.Expense

fun Expense.toDocument(
    expenseId: String,
    groupId: String,
    groupDocRef: DocumentReference,
    userId: String
) = ExpenseDocument(
    expenseId = expenseId,
    groupId = groupId,
    groupRef = groupDocRef,
    title = title,
    amountCents = amountCents,
    currency = currency,
    createdBy = userId,
    payerType = payerType
)

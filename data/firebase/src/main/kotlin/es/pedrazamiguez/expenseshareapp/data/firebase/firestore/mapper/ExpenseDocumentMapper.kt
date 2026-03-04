package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.ExpenseDocument
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.CashTranche
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import java.time.LocalDateTime

fun Expense.toDocument(
    expenseId: String, groupId: String, groupDocRef: DocumentReference, userId: String
) = ExpenseDocument(
    expenseId = expenseId,
    groupId = groupId,
    groupRef = groupDocRef,
    title = title,
    amountCents = sourceAmount,
    currency = sourceCurrency,
    groupCurrency = groupCurrency,
    groupAmountCents = groupAmount,
    exchangeRate = exchangeRate,
    operationDate = LocalDateTime
        .now()
        .toTimestampUtc(),
    paymentMethod = paymentMethod.name,
    cashTranches = cashTranches.map { tranche ->
        mapOf(
            "withdrawalId" to tranche.withdrawalId,
            "amountConsumed" to tranche.amountConsumed
        )
    },
    createdBy = userId,
    lastUpdatedBy = userId
)

fun ExpenseDocument.toDomain() = Expense(
    id = expenseId,
    groupId = groupId,
    title = title,
    sourceAmount = amountCents,
    sourceCurrency = currency,
    groupAmount = groupAmountCents ?: amountCents,
    groupCurrency = groupCurrency,
    exchangeRate = exchangeRate ?: 1.0,
    paymentMethod = runCatching { PaymentMethod.fromString(paymentMethod) }.getOrDefault(
        PaymentMethod.OTHER
    ),
    cashTranches = cashTranches.mapNotNull { map ->
        val withdrawalId = map["withdrawalId"] as? String ?: return@mapNotNull null
        val amountConsumed = (map["amountConsumed"] as? Number)?.toLong() ?: return@mapNotNull null
        CashTranche(withdrawalId = withdrawalId, amountConsumed = amountConsumed)
    },
    createdBy = createdBy,
    payerType = payerType,
    createdAt = createdAt.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAt.toLocalDateTimeUtc()
)

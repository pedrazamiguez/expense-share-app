package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.ContributionDocument
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution

fun Contribution.toDocument(
    contributionId: String,
    groupId: String,
    groupDocRef: DocumentReference,
    userId: String
) = ContributionDocument(
    contributionId = contributionId,
    groupId = groupId,
    groupRef = groupDocRef,
    userId = userId,
    amountCents = amount,
    currency = currency,
    createdBy = userId
)

fun ContributionDocument.toDomain() = Contribution(
    id = contributionId,
    groupId = groupId,
    userId = userId,
    amount = amountCents,
    currency = currency,
    createdAt = createdAt.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAt.toLocalDateTimeUtc()
)


package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.ContributionDocument
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import java.time.LocalDateTime

fun Contribution.toDocument(contributionId: String, groupId: String, groupDocRef: DocumentReference, userId: String) =
    ContributionDocument(
        contributionId = contributionId,
        groupId = groupId,
        groupRef = groupDocRef,
        userId = userId,
        contributionScope = contributionScope.name,
        subunitId = subunitId,
        amountCents = amount,
        currency = currency,
        createdBy = userId,
        createdAt = (createdAt ?: LocalDateTime.now()).toTimestampUtc(),
        lastUpdatedAt = (lastUpdatedAt ?: LocalDateTime.now()).toTimestampUtc()
    )

fun ContributionDocument.toDomain() = Contribution(
    id = contributionId,
    groupId = groupId,
    userId = userId,
    contributionScope = inferContributionScope(contributionScope, subunitId),
    subunitId = subunitId,
    amount = amountCents,
    currency = currency,
    createdAt = createdAt.toLocalDateTimeUtc(),
    lastUpdatedAt = lastUpdatedAt.toLocalDateTimeUtc()
)

/**
 * Infers the contribution scope from the document field, with backward-compatible
 * fallback for old documents that lack the field (empty string default).
 *
 * Old documents only had `subunitId`:
 * - `subunitId != null` → SUBUNIT
 * - `subunitId == null` → USER (individual)
 */
private fun inferContributionScope(scope: String, subunitId: String?): PayerType {
    if (scope.isBlank()) {
        return if (subunitId != null) PayerType.SUBUNIT else PayerType.USER
    }
    return runCatching { PayerType.fromString(scope) }.getOrDefault(PayerType.USER)
}


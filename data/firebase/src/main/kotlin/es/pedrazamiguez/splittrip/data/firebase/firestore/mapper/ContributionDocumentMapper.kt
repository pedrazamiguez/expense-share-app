package es.pedrazamiguez.splittrip.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.splittrip.data.firebase.firestore.document.ContributionDocument
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.Contribution
import java.math.BigDecimal
import java.time.LocalDateTime

fun Contribution.toDocument(contributionId: String, groupId: String, groupDocRef: DocumentReference, userId: String) =
    ContributionDocument(
        contributionId = contributionId,
        groupId = groupId,
        groupRef = groupDocRef,
        userId = this.userId.ifBlank { userId },
        contributionScope = contributionScope.name,
        subunitId = subunitId,
        linkedExpenseId = linkedExpenseId,
        linkedSettlementId = linkedSettlementId,
        amountCents = amount,
        currency = currency,
        equivalentBaseAmountCents = equivalentBaseAmount,
        exchangeRate = exchangeRate?.toPlainString(),
        createdBy = this.createdBy.ifBlank { userId },
        contributionDate = contributionDate?.toTimestampUtc(),
        createdAt = (createdAt ?: LocalDateTime.now()).toTimestampUtc(),
        lastUpdatedAt = (lastUpdatedAt ?: LocalDateTime.now()).toTimestampUtc()
    )

fun ContributionDocument.toDomain() = Contribution(
    id = contributionId,
    groupId = groupId,
    userId = userId,
    createdBy = createdBy,
    contributionScope = inferContributionScope(contributionScope, subunitId),
    subunitId = subunitId,
    linkedExpenseId = linkedExpenseId,
    linkedSettlementId = linkedSettlementId,
    amount = amountCents,
    currency = currency,
    equivalentBaseAmount = equivalentBaseAmountCents,
    exchangeRate = exchangeRate?.let { runCatching { BigDecimal(it) }.getOrNull() },
    contributionDate = contributionDate?.toLocalDateTimeUtc(),
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

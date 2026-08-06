package es.pedrazamiguez.splittrip.features.settlement.presentation.model

import kotlinx.collections.immutable.ImmutableList

data class MemberSpendingBarUiModel(
    val userId: String,
    val displayName: String,
    val isCurrentUser: Boolean,
    val allowanceCents: Long,
    val formattedAllowance: String,
    val formattedTotalSpent: String,
    val ownSpendingCents: Long,
    val spilloverSegments: ImmutableList<SpilloverSegment>,
    val memberColorIndex: Int
)

data class SpilloverSegment(
    val ownerColorIndex: Int,
    val amountCents: Long
)

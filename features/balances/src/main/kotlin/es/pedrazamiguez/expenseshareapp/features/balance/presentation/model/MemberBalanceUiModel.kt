package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * UI model representing a single member's financial position within the group.
 * All amounts are pre-formatted by the mapper for direct display.
 *
 * Mirrors the domain [MemberBalance] model's cash/non-cash split:
 * - [formattedPocketBalance]: virtual pocket share (contributed − withdrawn − nonCashSpent)
 * - [formattedCashInHand]: physical cash remaining (withdrawn − cashSpent)
 * - [formattedTotalSpent]: all expenses (cashSpent + nonCashSpent)
 *
 * Per-currency breakdowns enable the expandable card to show multi-currency detail.
 *
 * [displayName] holds the resolved human-readable name (not a raw userId).
 */
data class MemberBalanceUiModel(
    val userId: String = "",
    val displayName: String = "",
    val isCurrentUser: Boolean = false,
    val formattedContributed: String = "",
    val formattedCashInHand: String = "",
    val formattedTotalSpent: String = "",
    val formattedPocketBalance: String = "",
    val formattedCashSpent: String = "",
    val formattedNonCashSpent: String = "",
    val isPositiveBalance: Boolean = true,
    val cashInHandByCurrency: ImmutableList<CurrencyBreakdownUiModel> = persistentListOf(),
    val cashSpentByCurrency: ImmutableList<CurrencyBreakdownUiModel> = persistentListOf(),
    val nonCashSpentByCurrency: ImmutableList<CurrencyBreakdownUiModel> = persistentListOf()
)

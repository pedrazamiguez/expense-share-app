package es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.extensions.toEpochMillisUtc
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatAmountWithCurrency
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatCurrencyAmount
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatShortDate
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.CurrencyAmount
import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import es.pedrazamiguez.expenseshareapp.domain.model.MemberBalance
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ActivityItemUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CashBalanceUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CashWithdrawalUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ContributionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CurrencyBreakdownUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.GroupPocketBalanceUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.MemberBalanceUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class BalancesUiMapper(
    private val localeProvider: LocaleProvider,
    private val resourceProvider: ResourceProvider
) {

    fun mapBalance(balance: GroupPocketBalance, groupName: String): GroupPocketBalanceUiModel {
        val locale = localeProvider.getCurrentLocale()
        val cashBalanceUiModels = balance.cashBalances.entries
            .sortedBy { (currency, _) -> currency }
            .map { (currency, amountCents) ->
                val equivalent = balance.cashEquivalents[currency]
                CashBalanceUiModel(
                    currency = currency,
                    formattedAmount = formatCurrencyAmount(amountCents, currency, locale),
                    formattedEquivalent = if (currency != balance.currency && equivalent != null && equivalent > 0) {
                        formatCurrencyAmount(equivalent, balance.currency, locale)
                    } else {
                        ""
                    }
                )
            }.toImmutableList()

        return GroupPocketBalanceUiModel(
            groupName = groupName,
            formattedBalance = formatCurrencyAmount(balance.virtualBalance, balance.currency, locale),
            formattedTotalContributed = formatCurrencyAmount(
                balance.totalContributions,
                balance.currency,
                locale
            ),
            formattedTotalSpent = formatCurrencyAmount(
                balance.totalExpenses,
                balance.currency,
                locale
            ),
            currency = balance.currency,
            cashBalances = cashBalanceUiModels,
            formattedTotalCashEquivalent = if (balance.totalCashEquivalent > 0) {
                formatCurrencyAmount(balance.totalCashEquivalent, balance.currency, locale)
            } else {
                ""
            },
            formattedAvailableBalance = if (balance.scheduledHoldAmount > 0) {
                val available = balance.virtualBalance - balance.scheduledHoldAmount
                formatCurrencyAmount(available, balance.currency, locale)
            } else {
                null
            },
            formattedTotalExtras = if (balance.totalExtras > 0) {
                formatCurrencyAmount(balance.totalExtras, balance.currency, locale)
            } else {
                null
            }
        )
    }

    fun mapContributions(
        contributions: List<Contribution>,
        currentUserId: String?,
        memberProfiles: Map<String, User> = emptyMap(),
        subunits: Map<String, Subunit> = emptyMap()
    ): ImmutableList<ContributionUiModel> {
        val locale = localeProvider.getCurrentLocale()
        return contributions.map { contribution ->
            val isSubunit = contribution.contributionScope == PayerType.SUBUNIT
            val isPersonal = contribution.contributionScope == PayerType.USER
            val isGroup = contribution.contributionScope == PayerType.GROUP
            val scopeLabel = when {
                isSubunit -> contribution.subunitId?.let { subunits[it]?.name }
                isPersonal -> resourceProvider.getString(R.string.balances_contribution_scope_personal)
                isGroup -> resourceProvider.getString(R.string.balances_contribution_scope_group)
                else -> null
            }
            ContributionUiModel(
                id = contribution.id,
                displayName = resolveDisplayName(contribution.userId, memberProfiles),
                isCurrentUser = contribution.userId == currentUserId,
                formattedAmount = formatCurrencyAmount(
                    contribution.amount,
                    contribution.currency,
                    locale
                ),
                dateText = contribution.createdAt?.formatShortDate(locale) ?: "",
                scopeLabel = scopeLabel,
                isSubunitContribution = isSubunit,
                isPersonalContribution = isPersonal,
                isGroupContribution = isGroup
            )
        }.toImmutableList()
    }

    fun mapCashWithdrawals(
        withdrawals: List<CashWithdrawal>,
        groupCurrency: String,
        currentUserId: String?,
        memberProfiles: Map<String, User> = emptyMap(),
        subunits: Map<String, Subunit> = emptyMap()
    ): ImmutableList<CashWithdrawalUiModel> {
        val locale = localeProvider.getCurrentLocale()
        return withdrawals.map { withdrawal ->
            val isForeign = withdrawal.currency != groupCurrency
            val isSubunit = withdrawal.withdrawalScope == PayerType.SUBUNIT
            val isPersonal = withdrawal.withdrawalScope == PayerType.USER
            val isGroup = withdrawal.withdrawalScope == PayerType.GROUP
            val scopeLabel = when {
                isSubunit -> withdrawal.subunitId?.let { subunits[it]?.name }
                isPersonal -> resourceProvider.getString(R.string.balances_withdraw_cash_scope_personal)
                isGroup -> resourceProvider.getString(R.string.balances_withdraw_cash_scope_group)
                else -> null
            }
            CashWithdrawalUiModel(
                id = withdrawal.id,
                displayName = resolveDisplayName(withdrawal.withdrawnBy, memberProfiles),
                isCurrentUser = withdrawal.withdrawnBy == currentUserId,
                formattedAmount = formatCurrencyAmount(
                    withdrawal.amountWithdrawn,
                    withdrawal.currency,
                    locale
                ),
                formattedDeducted = if (isForeign) {
                    formatCurrencyAmount(
                        withdrawal.deductedBaseAmount,
                        groupCurrency,
                        locale
                    )
                } else {
                    ""
                },
                currency = withdrawal.currency,
                isForeignCurrency = isForeign,
                dateText = withdrawal.createdAt?.formatShortDate(locale) ?: "",
                scopeLabel = scopeLabel,
                isSubunitWithdrawal = isSubunit,
                isPersonalWithdrawal = isPersonal,
                isGroupWithdrawal = isGroup,
                title = withdrawal.title,
                notes = withdrawal.notes
            )
        }.toImmutableList()
    }

    /**
     * Merges contributions and cash withdrawals into a single activity list,
     * sorted by date descending (newest first).
     *
     * Reuses [mapContributions] and [mapCashWithdrawals] for UiModel construction
     * to avoid duplicating formatting/mapping logic.
     */
    fun mapActivity(
        contributions: List<Contribution>,
        withdrawals: List<CashWithdrawal>,
        groupCurrency: String,
        currentUserId: String?,
        memberProfiles: Map<String, User> = emptyMap(),
        subunits: Map<String, Subunit> = emptyMap()
    ): ImmutableList<ActivityItemUiModel> {
        // Precompute sort timestamps from domain models
        val contributionTimestampsById = contributions.associate { contribution ->
            val timestamp = contribution.createdAt?.toEpochMillisUtc() ?: 0L
            contribution.id to timestamp
        }

        val withdrawalTimestampsById = withdrawals.associate { withdrawal ->
            val timestamp = withdrawal.createdAt?.toEpochMillisUtc() ?: 0L
            withdrawal.id to timestamp
        }

        // Reuse existing mappers for UiModel construction
        val contributionUiModels = mapContributions(
            contributions = contributions,
            currentUserId = currentUserId,
            memberProfiles = memberProfiles,
            subunits = subunits
        )

        val withdrawalUiModels = mapCashWithdrawals(
            withdrawals = withdrawals,
            groupCurrency = groupCurrency,
            currentUserId = currentUserId,
            memberProfiles = memberProfiles,
            subunits = subunits
        )

        val contributionItems = contributionUiModels.map { uiModel ->
            ActivityItemUiModel.ContributionItem(
                contribution = uiModel,
                sortTimestamp = contributionTimestampsById[uiModel.id] ?: 0L
            )
        }

        val withdrawalItems = withdrawalUiModels.map { uiModel ->
            ActivityItemUiModel.CashWithdrawalItem(
                withdrawal = uiModel,
                sortTimestamp = withdrawalTimestampsById[uiModel.id] ?: 0L
            )
        }

        return (contributionItems + withdrawalItems)
            .sortedByDescending { it.sortTimestamp }
            .toImmutableList()
    }

    /**
     * Maps per-member domain balances to UI models with formatted amounts.
     * Sort order: current user first, then by |pocketBalance| descending (most extreme first).
     *
     * @param groupCurrency The group's base currency code, used to determine whether
     *                      to show equivalents for per-currency breakdowns.
     */
    fun mapMemberBalances(
        balances: List<MemberBalance>,
        currency: String,
        currentUserId: String?,
        memberProfiles: Map<String, User> = emptyMap(),
        groupCurrency: String = currency
    ): ImmutableList<MemberBalanceUiModel> {
        val locale = localeProvider.getCurrentLocale()
        return balances
            .sortedWith(
                compareByDescending<MemberBalance> { it.userId == currentUserId }
                    .thenByDescending { kotlin.math.abs(it.pocketBalance) }
            )
            .map { balance ->
                MemberBalanceUiModel(
                    userId = balance.userId,
                    displayName = resolveDisplayName(balance.userId, memberProfiles),
                    isCurrentUser = balance.userId == currentUserId,
                    formattedContributed = formatCurrencyAmount(balance.contributed, currency, locale),
                    formattedCashInHand = formatCurrencyAmount(balance.cashInHand, currency, locale),
                    formattedTotalSpent = formatCurrencyAmount(balance.totalSpent, currency, locale),
                    formattedPocketBalance = formatCurrencyAmount(balance.pocketBalance, currency, locale),
                    formattedCashSpent = formatCurrencyAmount(balance.cashSpent, currency, locale),
                    formattedNonCashSpent = formatCurrencyAmount(balance.nonCashSpent, currency, locale),
                    isPositiveBalance = balance.pocketBalance >= 0,
                    cashInHandByCurrency = mapCurrencyBreakdowns(
                        balance.cashInHandByCurrency,
                        groupCurrency,
                        locale
                    ),
                    cashSpentByCurrency = mapCurrencyBreakdowns(
                        balance.cashSpentByCurrency,
                        groupCurrency,
                        locale
                    ),
                    nonCashSpentByCurrency = mapCurrencyBreakdowns(
                        balance.nonCashSpentByCurrency,
                        groupCurrency,
                        locale
                    )
                )
            }
            .toImmutableList()
    }

    /**
     * Maps a list of [CurrencyAmount] domain models to formatted [CurrencyBreakdownUiModel]s.
     * Equivalents are only shown when the currency differs from the group currency
     * **and** [CurrencyAmount.equivalentCents] is positive; zero or negative equivalents
     * are suppressed (empty string) to avoid displaying meaningless "0.00" values.
     */
    private fun mapCurrencyBreakdowns(
        amounts: List<CurrencyAmount>,
        groupCurrency: String,
        locale: java.util.Locale
    ): ImmutableList<CurrencyBreakdownUiModel> {
        return amounts.map { ca ->
            CurrencyBreakdownUiModel(
                currency = ca.currency,
                formattedAmount = formatCurrencyAmount(ca.amountCents, ca.currency, locale),
                formattedEquivalent = if (ca.currency != groupCurrency && ca.equivalentCents > 0) {
                    formatCurrencyAmount(ca.equivalentCents, groupCurrency, locale)
                } else {
                    ""
                }
            )
        }.toImmutableList()
    }

    /**
     * Resolves a userId to a human-readable display name using the
     * fallback hierarchy: displayName → email → raw userId.
     */
    private fun resolveDisplayName(userId: String, memberProfiles: Map<String, User>): String {
        val user = memberProfiles[userId] ?: return userId
        return user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email.takeIf { it.isNotBlank() }
            ?: userId
    }

    /**
     * Formats a raw user-entered amount string with currency symbol and locale formatting.
     *
     * Delegates to [formatAmountWithCurrency] with the current locale.
     */
    fun formatInputAmountWithCurrency(amountInput: String, currencyCode: String): String =
        formatAmountWithCurrency(amountInput, currencyCode, localeProvider.getCurrentLocale())

    /**
     * Resolves the currency symbol for a given ISO 4217 currency code using the current locale.
     *
     * @return The locale-aware symbol (e.g., "€" for EUR, "US$" for USD), or an empty string
     *         if the code is blank or unresolvable.
     */
    fun resolveCurrencySymbol(currencyCode: String): String {
        if (currencyCode.isBlank()) return ""
        return runCatching {
            java.util.Currency.getInstance(currencyCode)
                .getSymbol(localeProvider.getCurrentLocale())
        }.getOrDefault("")
    }
}

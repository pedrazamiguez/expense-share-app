package es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.core.common.extensions.toEpochMillisUtc
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatCurrencyAmount
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatShortDate
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import es.pedrazamiguez.expenseshareapp.domain.model.MemberBalance
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ActivityItemUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CashBalanceUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CashWithdrawalUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ContributionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.GroupPocketBalanceUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.MemberBalanceUiModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency
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
                subunitName = contribution.subunitId?.let { subunits[it]?.name }
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
            val scopeLabel = when {
                isSubunit -> withdrawal.subunitId?.let { subunits[it]?.name }
                isPersonal -> resourceProvider.getString(R.string.balances_withdraw_cash_scope_personal)
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
                isPersonalWithdrawal = isPersonal
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
     * Sort order: current user first, then by |netBalance| descending (most extreme first).
     */
    fun mapMemberBalances(
        balances: List<MemberBalance>,
        currency: String,
        currentUserId: String?,
        memberProfiles: Map<String, User> = emptyMap()
    ): ImmutableList<MemberBalanceUiModel> {
        val locale = localeProvider.getCurrentLocale()
        return balances
            .sortedWith(
                compareByDescending<MemberBalance> { it.userId == currentUserId }
                    .thenByDescending { kotlin.math.abs(it.netBalance) }
            )
            .map { balance ->
                MemberBalanceUiModel(
                    userId = balance.userId,
                    displayName = resolveDisplayName(balance.userId, memberProfiles),
                    isCurrentUser = balance.userId == currentUserId,
                    formattedContributed = formatCurrencyAmount(balance.contributed, currency, locale),
                    formattedAvailable = formatCurrencyAmount(balance.available, currency, locale),
                    formattedSpent = formatCurrencyAmount(balance.spent, currency, locale),
                    formattedNetBalance = formatCurrencyAmount(balance.netBalance, currency, locale),
                    isPositiveBalance = balance.netBalance >= 0
                )
            }
            .toImmutableList()
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
     * Parses a user-entered amount string to the currency's smallest unit (e.g., cents for EUR,
     * yen for JPY, millimes for TND).
     *
     * Correctly handles:
     * - Any currency's decimal places (0 for JPY, 2 for EUR/USD, 3 for KWD/TND)
     * - Locale-specific separators via [CurrencyConverter.normalizeAmountString]
     * - Deterministic rounding via [RoundingMode.HALF_UP] (no silent truncation)
     *
     * Examples:
     * - "25.50" with EUR (2 decimals) → 2550
     * - "1000"  with JPY (0 decimals) → 1000
     * - "10.500" with TND (3 decimals) → 10500
     * - "1.999" with EUR (2 decimals) → 200 (rounds, never truncates)
     *
     * @param amountString The raw user input (may use locale-specific separators)
     * @param currencyCode ISO 4217 currency code used to determine decimal places
     * @return Amount in the currency's smallest unit, or 0 if input is unparseable
     */
    fun parseAmountToSmallestUnit(amountString: String, currencyCode: String): Long {
        val normalizedString = CurrencyConverter.normalizeAmountString(amountString.trim())
        val amount = normalizedString.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val decimalPlaces = runCatching {
            Currency.getInstance(currencyCode).defaultFractionDigits
        }.getOrElse {
            Currency.getInstance(AppConstants.DEFAULT_CURRENCY_CODE).defaultFractionDigits
        }
        val multiplier = BigDecimal.TEN.pow(decimalPlaces)
        return amount.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).toLong()
    }
}

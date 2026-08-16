package es.pedrazamiguez.splittrip.features.balance.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.enums.GrammaticalGenderEnum
import es.pedrazamiguez.splittrip.core.common.enums.SelfIdentificationContextEnum
import es.pedrazamiguez.splittrip.core.common.extensions.localeAwareComparator
import es.pedrazamiguez.splittrip.core.common.extensions.toEpochMillisUtc
import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatCurrencyAmount
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatForDisplay
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatShortDate
import es.pedrazamiguez.splittrip.core.designsystem.presentation.mapper.UserUiMapper
import es.pedrazamiguez.splittrip.domain.enums.AddOnType
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.model.GroupPocketBalance
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.balance.R
import es.pedrazamiguez.splittrip.features.balance.presentation.model.ActivityItemUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.CashBreakdownUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.CashWithdrawalUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.ContributionUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.CurrencyBreakdownUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.ExtrasBreakdownContext
import es.pedrazamiguez.splittrip.features.balance.presentation.model.ExtrasBreakdownUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.GroupPocketBalanceUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.MemberBalanceCashContext
import es.pedrazamiguez.splittrip.features.balance.presentation.model.MemberBalanceUiModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class BalancesUiMapper(
    private val localeProvider: LocaleProvider,
    private val resourceProvider: ResourceProvider,
    private val userUiMapper: UserUiMapper
) {

    companion object {
        internal const val EM_DASH = "\u2014"
    }

    fun mapBalance(balance: GroupPocketBalance, groupName: String): GroupPocketBalanceUiModel {
        val locale = localeProvider.getCurrentLocale()
        val cashBalanceUiModels = mapCashBalances(balance, locale)

        return GroupPocketBalanceUiModel(
            groupName = groupName,
            formattedBalance = formatCurrencyAmount(balance.virtualBalance, balance.currency, locale),
            formattedTotalContributed = formatCurrencyAmount(balance.totalContributions, balance.currency, locale),
            formattedTotalSpent = formatCurrencyAmount(balance.totalExpenses, balance.currency, locale),
            currency = balance.currency,
            cashBalances = cashBalanceUiModels,
            formattedTotalCashEquivalent = formatIfPos(balance.totalCashEquivalent, balance.currency, locale) ?: "",
            formattedAvailableBalance = if (balance.scheduledHoldAmount > 0 || balance.refundableHoldAmount > 0) {
                val available = balance.virtualBalance - balance.scheduledHoldAmount
                formatCurrencyAmount(available, balance.currency, locale)
            } else {
                null
            },
            formattedScheduledHoldAmount = formatIfPos(balance.scheduledHoldAmount, balance.currency, locale),
            formattedRefundableHoldAmount = formatIfPos(balance.refundableHoldAmount, balance.currency, locale),
            formattedTotalExtras = formatIfPos(balance.totalExtras, balance.currency, locale)
        )
    }

    fun mapContributions(
        contributions: List<Contribution>,
        groupCurrency: String,
        currentUserId: String?,
        memberProfiles: Map<String, User> = emptyMap(),
        subunits: Map<String, Subunit> = emptyMap(),
        groupMemberIds: List<String> = emptyList()
    ): ImmutableList<ContributionUiModel> {
        val locale = localeProvider.getCurrentLocale()
        return contributions.map { contribution ->
            buildContributionItem(
                contribution = contribution,
                groupCurrency = groupCurrency,
                currentUserId = currentUserId,
                memberProfiles = memberProfiles,
                subunits = subunits,
                groupMemberIds = groupMemberIds,
                locale = locale
            )
        }.toImmutableList()
    }

    private fun buildContributionItem(
        contribution: Contribution,
        groupCurrency: String,
        currentUserId: String?,
        memberProfiles: Map<String, User>,
        subunits: Map<String, Subunit>,
        groupMemberIds: List<String>,
        locale: Locale
    ): ContributionUiModel {
        val memberDisplay = resolveMemberDisplay(
            userId = contribution.userId,
            groupMemberIds = groupMemberIds,
            memberProfiles = memberProfiles,
            currentUserId = currentUserId,
            userUiMapper = userUiMapper
        )
        return ContributionUiModel(
            id = contribution.id,
            memberDisplay = memberDisplay,
            isCurrentUser = contribution.userId == currentUserId,
            formattedAmount = formatCurrencyAmount(contribution.amount, contribution.currency, locale),
            formattedEquivalentAmount = contribution.equivalentBaseAmount
                ?.takeIf { contribution.currency != groupCurrency }
                ?.let { formatCurrencyAmount(it, groupCurrency, locale) }
                ?: "",
            isForeignCurrency = contribution.currency != groupCurrency,
            dateText = (contribution.contributionDate ?: contribution.createdAt)?.formatShortDate(locale).orEmpty(),
            scopeLabel = when (contribution.contributionScope) {
                PayerType.SUBUNIT -> contribution.subunitId?.let { subunits[it]?.name }
                PayerType.USER -> resourceProvider.getString(R.string.balances_contribution_scope_personal)
                PayerType.GROUP -> resourceProvider.getString(R.string.balances_contribution_scope_group)
            },
            isSubunitContribution = contribution.contributionScope == PayerType.SUBUNIT,
            isPersonalContribution = contribution.contributionScope == PayerType.USER,
            isGroupContribution = contribution.contributionScope == PayerType.GROUP,
            createdByDisplayName = resolveCreatedByDisplayName(
                createdBy = contribution.createdBy,
                targetUserId = contribution.userId,
                memberProfiles = memberProfiles,
                currentUserId = currentUserId,
                userUiMapper = userUiMapper
            ),
            isLinkedContribution = contribution.linkedExpenseId != null,
            isSettlementContribution = contribution.linkedSettlementId != null,
            syncStatus = contribution.syncStatus,
            actionsTitle = if (contribution.userId == currentUserId) {
                val you = userUiMapper.mapToSelfIdentification(
                    SelfIdentificationContextEnum.POSSESSIVE_PRONOUN,
                    GrammaticalGenderEnum.FEMININE
                )
                resourceProvider.getString(R.string.balances_contribution_actions_title_you, you)
            } else {
                resourceProvider.getString(R.string.balances_contribution_actions_title, memberDisplay.displayName)
            }
        )
    }

    fun mapCashWithdrawals(
        withdrawals: List<CashWithdrawal>,
        groupCurrency: String,
        currentUserId: String?,
        memberProfiles: Map<String, User> = emptyMap(),
        subunits: Map<String, Subunit> = emptyMap(),
        groupMemberIds: List<String> = emptyList()
    ): ImmutableList<CashWithdrawalUiModel> {
        val locale = localeProvider.getCurrentLocale()
        return withdrawals.map { withdrawal ->
            buildCashWithdrawalItem(
                withdrawal = withdrawal,
                groupCurrency = groupCurrency,
                currentUserId = currentUserId,
                memberProfiles = memberProfiles,
                subunits = subunits,
                groupMemberIds = groupMemberIds,
                locale = locale
            )
        }.toImmutableList()
    }

    private fun buildCashWithdrawalItem(
        withdrawal: CashWithdrawal,
        groupCurrency: String,
        currentUserId: String?,
        memberProfiles: Map<String, User>,
        subunits: Map<String, Subunit>,
        groupMemberIds: List<String>,
        locale: Locale
    ): CashWithdrawalUiModel {
        val memberDisplay = resolveMemberDisplay(
            userId = withdrawal.withdrawnBy,
            groupMemberIds = groupMemberIds,
            memberProfiles = memberProfiles,
            currentUserId = currentUserId,
            userUiMapper = userUiMapper
        )
        return CashWithdrawalUiModel(
            id = withdrawal.id,
            memberDisplay = memberDisplay,
            isCurrentUser = withdrawal.withdrawnBy == currentUserId,
            formattedAmount = formatCurrencyAmount(withdrawal.amountWithdrawn, withdrawal.currency, locale),
            formattedDeducted = withdrawal.deductedBaseAmount
                .takeIf { withdrawal.currency != groupCurrency }
                ?.let { formatCurrencyAmount(it, groupCurrency, locale) }
                ?: "",
            currency = withdrawal.currency,
            isForeignCurrency = withdrawal.currency != groupCurrency,
            dateText = withdrawal.createdAt?.formatShortDate(locale).orEmpty(),
            scopeLabel = when (withdrawal.withdrawalScope) {
                PayerType.SUBUNIT -> withdrawal.subunitId?.let { subunits[it]?.name }
                PayerType.USER -> resourceProvider.getString(R.string.balances_withdraw_cash_scope_personal)
                PayerType.GROUP -> resourceProvider.getString(R.string.balances_withdraw_cash_scope_group)
            },
            isSubunitWithdrawal = withdrawal.withdrawalScope == PayerType.SUBUNIT,
            isPersonalWithdrawal = withdrawal.withdrawalScope == PayerType.USER,
            isGroupWithdrawal = withdrawal.withdrawalScope == PayerType.GROUP,
            title = withdrawal.title,
            notes = withdrawal.notes,
            createdByDisplayName = resolveCreatedByDisplayName(
                createdBy = withdrawal.createdBy,
                targetUserId = withdrawal.withdrawnBy,
                memberProfiles = memberProfiles,
                currentUserId = currentUserId,
                userUiMapper = userUiMapper
            ),
            syncStatus = withdrawal.syncStatus,
            actionsTitle = if (withdrawal.withdrawnBy == currentUserId) {
                val you = userUiMapper.mapToSelfIdentification(
                    SelfIdentificationContextEnum.POSSESSIVE_PRONOUN,
                    GrammaticalGenderEnum.FEMININE
                )
                resourceProvider.getString(R.string.balances_withdrawal_actions_title_you, you)
            } else {
                resourceProvider.getString(R.string.balances_withdrawal_actions_title, memberDisplay.displayName)
            }
        )
    }

    fun mapActivity(
        contributions: List<Contribution>,
        withdrawals: List<CashWithdrawal>,
        groupCurrency: String,
        currentUserId: String?,
        memberProfiles: Map<String, User> = emptyMap(),
        subunits: Map<String, Subunit> = emptyMap(),
        groupMemberIds: List<String> = emptyList()
    ): ImmutableList<ActivityItemUiModel> {
        val contributionUiModels = mapContributions(
            contributions = contributions,
            groupCurrency = groupCurrency,
            currentUserId = currentUserId,
            memberProfiles = memberProfiles,
            subunits = subunits,
            groupMemberIds = groupMemberIds
        )
        val contributionItems = contributions.zip(contributionUiModels) { domain, ui ->
            ActivityItemUiModel.ContributionItem(
                contribution = ui,
                sortTimestamp = (domain.contributionDate ?: domain.createdAt)?.toEpochMillisUtc() ?: 0L
            )
        }

        val withdrawalUiModels = mapCashWithdrawals(
            withdrawals = withdrawals,
            groupCurrency = groupCurrency,
            currentUserId = currentUserId,
            memberProfiles = memberProfiles,
            subunits = subunits,
            groupMemberIds = groupMemberIds
        )
        val withdrawalItems = withdrawals.zip(withdrawalUiModels) { domain, ui ->
            ActivityItemUiModel.CashWithdrawalItem(
                withdrawal = ui,
                sortTimestamp = domain.createdAt?.toEpochMillisUtc() ?: 0L
            )
        }

        return (contributionItems + withdrawalItems)
            .sortedByDescending { it.sortTimestamp }
            .toImmutableList()
    }

    fun mapExtrasBreakdown(
        expenses: List<Expense>,
        withdrawals: List<CashWithdrawal>,
        groupCurrency: String,
        memberProfiles: Map<String, User>,
        subunitsMap: Map<String, Subunit>,
        currentUserId: String?
    ): ImmutableList<ExtrasBreakdownUiModel> {
        val context = ExtrasBreakdownContext(
            groupCurrency = groupCurrency,
            memberProfiles = memberProfiles,
            subunitsMap = subunitsMap,
            currentUserId = currentUserId,
            locale = localeProvider.getCurrentLocale(),
            resourceProvider = resourceProvider,
            userUiMapper = userUiMapper
        )
        return mapExtrasBreakdown(
            expenses = expenses,
            withdrawals = withdrawals,
            context = context
        )
    }

    fun mapMemberBalances(
        balances: List<MemberBalance>,
        currency: String,
        currentUserId: String?,
        memberProfiles: Map<String, User> = emptyMap(),
        groupCurrency: String = currency,
        cashContext: MemberBalanceCashContext = MemberBalanceCashContext(),
        groupMemberIds: List<String> = emptyList()
    ): ImmutableList<MemberBalanceUiModel> {
        val locale = localeProvider.getCurrentLocale()
        val mappedBalances = balances.map { balance ->
            mapSingleMemberBalance(
                balance = balance,
                currentUserId = currentUserId,
                currency = currency,
                groupCurrency = groupCurrency,
                memberProfiles = memberProfiles,
                cashContext = cashContext,
                groupMemberIds = groupMemberIds
            )
        }

        val displayNameComparator = localeAwareComparator<MemberBalanceUiModel>(locale) { it.displayName }

        return mappedBalances
            .sortedWith(
                compareByDescending<MemberBalanceUiModel> { it.isCurrentUser }
                    .thenComparing(displayNameComparator)
            )
            .toImmutableList()
    }

    private fun computeFormattedTotalFees(
        userId: String,
        isNegativeCash: Boolean,
        cashContext: MemberBalanceCashContext,
        groupCurrency: String,
        locale: Locale
    ): String {
        if (isNegativeCash) return ""
        val totalFeeCents = computeMemberTotalFees(
            userId = userId,
            withdrawals = cashContext.withdrawals,
            groupMemberIds = cashContext.groupMemberIds,
            subunitsMap = cashContext.subunitsMap
        )
        return if (totalFeeCents > 0L) {
            formatCurrencyAmount(totalFeeCents, groupCurrency, locale)
        } else {
            ""
        }
    }

    private fun mapSingleMemberBalance(
        balance: MemberBalance,
        currentUserId: String?,
        currency: String,
        groupCurrency: String,
        memberProfiles: Map<String, User>,
        cashContext: MemberBalanceCashContext,
        groupMemberIds: List<String>
    ): MemberBalanceUiModel {
        val locale = localeProvider.getCurrentLocale()
        val isNegativeCash = balance.cashInHand < 0
        val formattedTotalFees = computeFormattedTotalFees(
            userId = balance.userId,
            isNegativeCash = isNegativeCash,
            cashContext = cashContext,
            groupCurrency = groupCurrency,
            locale = locale
        )
        val memberDisplay = resolveMemberDisplay(
            userId = balance.userId,
            groupMemberIds = groupMemberIds,
            memberProfiles = memberProfiles,
            currentUserId = currentUserId,
            userUiMapper = userUiMapper
        )
        val (cashInHand, cashInHandByCurrency, cashBreakdown) = mapCashInHandAndBreakdown(
            balance,
            isNegativeCash,
            currency,
            groupCurrency,
            locale,
            cashContext
        )

        return MemberBalanceUiModel(
            userId = balance.userId,
            memberDisplay = memberDisplay,
            isCurrentUser = balance.userId == currentUserId,
            formattedContributed = formatCurrencyAmount(balance.contributed, currency, locale),
            formattedCashInHand = cashInHand,
            formattedTotalSpent = formatCurrencyAmount(balance.totalSpent, currency, locale),
            formattedPocketBalance = formatCurrencyAmount(balance.pocketBalance, currency, locale),
            formattedTotalBalance = formatCurrencyAmount(balance.totalBalance, currency, locale),
            formattedCashSpent = formatCurrencyAmount(balance.cashSpent, currency, locale),
            formattedNonCashSpent = formatCurrencyAmount(balance.nonCashSpent, currency, locale),
            isPositiveBalance = balance.totalBalance >= 0,
            hasNegativeCashInHand = isNegativeCash,
            cashInHandByCurrency = cashInHandByCurrency,
            cashSpentByCurrency = mapCurrencyBreakdowns(balance.cashSpentByCurrency, groupCurrency, locale),
            nonCashSpentByCurrency = mapCurrencyBreakdowns(balance.nonCashSpentByCurrency, groupCurrency, locale),
            formattedRefundableSpent = if (balance.refundableSpent > 0L) {
                formatCurrencyAmount(balance.refundableSpent, currency, locale)
            } else {
                null
            },
            refundableSpentByCurrency = mapCurrencyBreakdowns(balance.refundableSpentByCurrency, groupCurrency, locale),
            cashBreakdown = cashBreakdown,
            formattedTotalFees = formattedTotalFees
        )
    }

    private fun mapCashInHandAndBreakdown(
        balance: MemberBalance,
        isNegativeCash: Boolean,
        currency: String,
        groupCurrency: String,
        locale: Locale,
        cashContext: MemberBalanceCashContext
    ): Triple<String, ImmutableList<CurrencyBreakdownUiModel>, ImmutableList<CashBreakdownUiModel>> {
        if (isNegativeCash) {
            return Triple(EM_DASH, persistentListOf(), persistentListOf())
        }
        return Triple(
            formatCurrencyAmount(balance.cashInHand, currency, locale),
            mapCurrencyBreakdowns(balance.cashInHandByCurrency, groupCurrency, locale),
            mapCashBreakdown(
                userId = balance.userId,
                withdrawals = cashContext.withdrawals,
                subunitsMap = cashContext.subunitsMap,
                groupMemberIds = cashContext.groupMemberIds,
                groupCurrency = groupCurrency,
                locale = locale
            )
        )
    }

    private fun mapCashBreakdown(
        userId: String,
        withdrawals: List<CashWithdrawal>,
        subunitsMap: Map<String, Subunit>,
        groupMemberIds: List<String>,
        groupCurrency: String,
        locale: Locale
    ): ImmutableList<CashBreakdownUiModel> {
        val scopeOrder = mapOf(PayerType.GROUP to 0, PayerType.SUBUNIT to 1, PayerType.USER to 2)
        return withdrawals
            .sortedWith(
                compareBy<CashWithdrawal> { scopeOrder[it.withdrawalScope] ?: 3 }
                    .thenByDescending { it.createdAt }
            )
            .mapNotNull { withdrawal ->
                if (withdrawal.amountWithdrawn == 0L || withdrawal.remainingAmount <= 0L) return@mapNotNull null
                val nativeShare = computeUserNativeShare(withdrawal, userId, groupMemberIds, subunitsMap)
                if (nativeShare <= 0L) return@mapNotNull null
                buildCashBreakdownEntry(
                    withdrawal = withdrawal,
                    nativeShare = nativeShare,
                    userId = userId,
                    groupMemberIds = groupMemberIds,
                    groupCurrency = groupCurrency,
                    locale = locale,
                    subunitsMap = subunitsMap
                )
            }
            .toImmutableList()
    }

    private fun buildCashBreakdownEntry(
        withdrawal: CashWithdrawal,
        nativeShare: Long,
        userId: String,
        groupMemberIds: List<String>,
        groupCurrency: String,
        locale: Locale,
        subunitsMap: Map<String, Subunit>
    ): CashBreakdownUiModel {
        val groupEquivalent = BigDecimal(nativeShare)
            .multiply(BigDecimal(withdrawal.deductedBaseAmount))
            .divide(BigDecimal(withdrawal.amountWithdrawn), 0, RoundingMode.HALF_UP)
            .toLong()
        val isForeign = withdrawal.currency != groupCurrency
        val dateText = withdrawal.createdAt?.formatShortDate(locale) ?: ""
        val label = if (withdrawal.title.isNullOrBlank()) {
            resourceProvider.getString(R.string.balances_cash_breakdown_atm_fallback, dateText)
        } else {
            withdrawal.title ?: ""
        }
        return CashBreakdownUiModel(
            withdrawalLabel = label,
            dateText = dateText,
            formattedRate = if (isForeign) {
                resourceProvider.getString(
                    R.string.balances_cash_breakdown_rate,
                    withdrawal.exchangeRate.formatForDisplay(locale, maxDecimalPlaces = 6),
                    withdrawal.currency,
                    groupCurrency
                )
            } else {
                ""
            },
            formattedNativeRemaining = formatCurrencyAmount(nativeShare, withdrawal.currency, locale),
            formattedEquivalent = if (isForeign) {
                formatCurrencyAmount(groupEquivalent, groupCurrency, locale)
            } else {
                ""
            },
            scopeLabel = when (withdrawal.withdrawalScope) {
                PayerType.GROUP -> resourceProvider.getString(R.string.balances_cash_breakdown_group_scope)
                PayerType.USER -> resourceProvider.getString(R.string.balances_cash_breakdown_personal_scope)
                PayerType.SUBUNIT -> withdrawal.subunitId?.let { subunitsMap[it]?.name }
                    ?: resourceProvider.getString(R.string.balances_cash_breakdown_unknown_subunit)
            },
            isEstimatedShare = withdrawal.withdrawalScope == PayerType.GROUP,
            formattedAddOns = formatWithdrawalAddOns(
                withdrawal = withdrawal,
                userId = userId,
                groupMemberIds = groupMemberIds,
                subunitsMap = subunitsMap,
                groupCurrency = groupCurrency,
                locale = locale
            )
        )
    }

    private fun formatWithdrawalAddOns(
        withdrawal: CashWithdrawal,
        userId: String,
        groupMemberIds: List<String>,
        subunitsMap: Map<String, Subunit>,
        groupCurrency: String,
        locale: Locale
    ): String {
        val nonDiscountAddOns = withdrawal.addOns.filter { it.type != AddOnType.DISCOUNT }
        if (nonDiscountAddOns.isEmpty()) return ""
        val subunit = withdrawal.subunitId?.let { subunitsMap[it] }
        val totalUserAddOnCents = nonDiscountAddOns.sumOf { addOn ->
            computeAddOnShare(
                addOn = addOn,
                scope = withdrawal.withdrawalScope,
                withdrawnBy = withdrawal.withdrawnBy,
                userId = userId,
                groupMemberIds = groupMemberIds,
                subunit = subunit
            )
        }
        if (totalUserAddOnCents <= 0L) return ""
        return formatCurrencyAmount(totalUserAddOnCents, groupCurrency, locale)
    }
}

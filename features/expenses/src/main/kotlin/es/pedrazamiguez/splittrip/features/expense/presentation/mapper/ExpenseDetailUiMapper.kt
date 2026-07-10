package es.pedrazamiguez.splittrip.features.expense.presentation.mapper

import androidx.compose.ui.graphics.vector.ImageVector
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Calendar
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.CircleCheck
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Receipt
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ReceiptRefund
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.splittrip.core.designsystem.presentation.mapper.UserUiMapper
import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.MemberDisplay
import es.pedrazamiguez.splittrip.domain.enums.AddOnMode
import es.pedrazamiguez.splittrip.domain.enums.AddOnType
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.enums.PaymentStatus
import es.pedrazamiguez.splittrip.domain.enums.SplitType
import es.pedrazamiguez.splittrip.domain.model.AddOn
import es.pedrazamiguez.splittrip.domain.model.CashTranche
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.model.ExpenseSplit
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.domain.service.AddOnCalculationService
import es.pedrazamiguez.splittrip.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.splittrip.features.expense.R
import es.pedrazamiguez.splittrip.features.expense.presentation.extensions.toIconVector
import es.pedrazamiguez.splittrip.features.expense.presentation.extensions.toStringRes
import es.pedrazamiguez.splittrip.features.expense.presentation.model.AddOnDetailUiModel
import es.pedrazamiguez.splittrip.features.expense.presentation.model.CashTrancheDetailUiModel
import es.pedrazamiguez.splittrip.features.expense.presentation.model.ExpenseDetailUiModel
import es.pedrazamiguez.splittrip.features.expense.presentation.model.SplitDetailUiModel
import es.pedrazamiguez.splittrip.features.expense.presentation.model.SubunitSplitGroupUiModel
import java.math.BigDecimal
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class ExpenseDetailUiMapper(
    private val formattingHelper: FormattingHelper,
    private val resourceProvider: ResourceProvider,
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val addOnCalculationService: AddOnCalculationService,
    private val paymentStatusBadgeUiMapper: PaymentStatusBadgeUiMapper,
    private val userUiMapper: UserUiMapper
) {

    fun map(
        expense: Expense,
        memberProfiles: Map<String, User>,
        currentUserId: String?,
        withdrawalLookup: Map<String, CashWithdrawal> = emptyMap(),
        subunitNameLookup: Map<String, String> = emptyMap(),
        groupMemberIds: List<String> = emptyList()
    ): ExpenseDetailUiModel {
        return ModelBuilder(
            expense,
            memberProfiles,
            currentUserId,
            withdrawalLookup,
            subunitNameLookup,
            groupMemberIds
        ).build()
    }

    private inner class ModelBuilder(
        val expense: Expense,
        val memberProfiles: Map<String, User>,
        val currentUserId: String?,
        val withdrawalLookup: Map<String, CashWithdrawal>,
        val subunitNameLookup: Map<String, String>,
        val groupMemberIds: List<String>
    ) {
        @Suppress("LongMethod")
        fun build(): ExpenseDetailUiModel {
            val youLabel = resourceProvider.getString(R.string.you_label)
            val (soloSplits, splitGroups) = resolveSplits()
            val isForeign = expense.sourceCurrency != expense.groupCurrency
            val badgeData = paymentStatusBadgeUiMapper.buildBadge(expense)
            val badgeIcon = badgeData?.let {
                when (expense.paymentStatus) {
                    PaymentStatus.SCHEDULED -> if (it.isPassed) {
                        TablerIcons.Outline.CircleCheck
                    } else {
                        TablerIcons.Outline.Calendar
                    }
                    PaymentStatus.REFUNDABLE -> if (it.isPassed) {
                        TablerIcons.Outline.Receipt
                    } else {
                        TablerIcons.Outline.ReceiptRefund
                    }
                    else -> null
                }
            }

            val effectivePayerId = expense.payerId ?: expense.createdBy.takeIf { it.isNotBlank() }
            val payerDisplay = resolvePayerDisplay(effectivePayerId, youLabel)
            val creatorDisplay = resolveCreatorDisplay(youLabel)

            return ExpenseDetailUiModel(
                id = expense.id,
                groupId = expense.groupId,
                title = expense.title,
                category = expense.category,
                categoryText = resourceProvider.getString(expense.category.toStringRes()),
                formattedGroupAmount = formattingHelper.formatCentsWithCurrency(
                    expense.groupAmount,
                    expense.groupCurrency
                ),
                groupCurrency = expense.groupCurrency,
                formattedSourceAmount = resolveSourceAmountFormatted(expense, formattingHelper, isForeign),
                sourceCurrency = expense.sourceCurrency,
                formattedExchangeRate = resolveExchangeRateFormatted(expense, formattingHelper, isForeign),
                isForeignCurrency = isForeign,
                paymentMethodText = resourceProvider.getString(expense.paymentMethod.toStringRes()),
                paymentMethodIcon = expense.paymentMethod.toIconVector(),
                paymentStatusText = when (expense.paymentStatus) {
                    PaymentStatus.SCHEDULED -> resourceProvider.getString(R.string.payment_status_pending)
                    PaymentStatus.REFUNDABLE -> resourceProvider.getString(R.string.expense_detail_on_hold)
                    else -> resourceProvider.getString(expense.paymentStatus.toStringRes())
                },
                paymentStatusIcon = expense.paymentStatus.toIconVector(),
                expenseScopeLabel = buildExpenseScopeLabel(expense.payerType, resourceProvider),
                paidByText = getPaidByText(
                    expense,
                    currentUserId,
                    youLabel,
                    memberProfiles,
                    userUiMapper,
                    resourceProvider
                ),
                payerDisplay = payerDisplay,
                creatorDisplay = creatorDisplay,
                dateText = formattingHelper.formatShortDate(expense.createdAt),
                secondaryDateText = resolveSecondaryDateText(expense, formattingHelper),
                secondaryDateIcon = resolveSecondaryDateIcon(expense),
                vendorText = expense.vendor?.takeIf { it.isNotBlank() },
                notesText = expense.notes?.takeIf { it.isNotBlank() },
                badgeText = null,
                badgeIcon = badgeIcon,
                isBadgeUrgent = badgeData?.isPassed == true,
                isOutOfPocket = expense.payerType == PayerType.USER,
                fundingSourceText = buildFundingSourceText(
                    expense,
                    currentUserId,
                    memberProfiles,
                    resourceProvider,
                    userUiMapper
                ),
                splitTypeText = resourceProvider.getString(expense.splitType.toStringRes()),
                splits = soloSplits,
                splitGroups = splitGroups,
                hasAddOns = expense.addOns.isNotEmpty(),
                hasIncludedAddOns = expense.addOns.any { it.mode == AddOnMode.INCLUDED },
                addOns = mapAddOns(expense.addOns, expense.groupCurrency),
                formattedEffectiveTotal = formatEffectiveTotal(expense, formattingHelper, addOnCalculationService),
                formattedIncludedBaseCost = formatIncludedBaseCost(expense, formattingHelper),
                formattedOriginalEnteredTotal = formatOriginalEnteredTotal(expense, formattingHelper),
                cashTranches = mapCashTranches(
                    expense.cashTranches,
                    expense.sourceCurrency,
                    expense.groupCurrency,
                    withdrawalLookup,
                    subunitNameLookup
                ),
                receiptUri = expense.receiptAttachment?.let { it.localUri.ifBlank { it.remoteUrl } },
                receiptMimeType = expense.receiptAttachment?.mimeType,
                createdByText = getCreatedByText(
                    expense,
                    currentUserId,
                    youLabel,
                    memberProfiles,
                    userUiMapper,
                    resourceProvider
                ),
                createdAtText = formattingHelper.formatShortDate(expense.createdAt),
                syncStatus = expense.syncStatus,
                isCancelled = expense.paymentStatus == PaymentStatus.CANCELLED,
                isRefundable = expense.paymentStatus == PaymentStatus.REFUNDABLE && badgeData?.isPassed != true
            )
        }

        private fun resolvePayerDisplay(effectivePayerId: String?, youLabel: String): MemberDisplay {
            val payerResolvedName = if (effectivePayerId != null) {
                resolveDisplayName(effectivePayerId, memberProfiles, currentUserId, youLabel, userUiMapper)
            } else {
                ""
            }
            return if (effectivePayerId == null || effectivePayerId !in groupMemberIds) {
                MemberDisplay.Former(effectivePayerId ?: "", payerResolvedName)
            } else {
                MemberDisplay.Active(effectivePayerId, payerResolvedName)
            }
        }

        private fun resolveCreatorDisplay(youLabel: String): MemberDisplay {
            val creatorResolvedName =
                resolveDisplayName(expense.createdBy, memberProfiles, currentUserId, youLabel, userUiMapper)
            return if (expense.createdBy.isBlank() || expense.createdBy !in groupMemberIds) {
                MemberDisplay.Former(expense.createdBy, creatorResolvedName)
            } else {
                MemberDisplay.Active(expense.createdBy, creatorResolvedName)
            }
        }

        private fun resolveSplits() = mapSplits(
            expense = expense,
            memberProfiles = memberProfiles,
            currentUserId = currentUserId,
            subunitNameLookup = subunitNameLookup,
            groupMemberIds = groupMemberIds
        )
    }

    private fun mapSplits(
        expense: Expense,
        memberProfiles: Map<String, User>,
        currentUserId: String?,
        subunitNameLookup: Map<String, String>,
        groupMemberIds: List<String>
    ): Pair<ImmutableList<SplitDetailUiModel>, ImmutableList<SubunitSplitGroupUiModel>> {
        val rows = expense.splits.map { split ->
            split to mapSplitRow(
                split = split,
                expense = expense,
                memberProfiles = memberProfiles,
                currentUserId = currentUserId,
                subunitNameLookup = subunitNameLookup,
                groupMemberIds = groupMemberIds
            )
        }
        val solo = rows.filter { it.first.subunitId.isNullOrBlank() }.map { it.second }
        val grouped = rows
            .filter { !it.first.subunitId.isNullOrBlank() }
            .groupBy { it.first.subunitId!! }
            .map { (subunitId, entries) ->
                buildSubunitGroup(subunitId, entries.map { it.second }, expense, subunitNameLookup)
            }
        return solo.toImmutableList() to grouped.toImmutableList()
    }

    private fun buildSubunitGroup(
        subunitId: String,
        members: List<SplitDetailUiModel>,
        expense: Expense,
        subunitNameLookup: Map<String, String>
    ): SubunitSplitGroupUiModel {
        val label = subunitNameLookup[subunitId]
            ?: resourceProvider.getString(R.string.expense_detail_subunit_fallback_label)
        val totalSourceCents = expense.splits
            .filter { it.subunitId == subunitId }
            .sumOf { it.amountCents }
        val totalGroupCents = expenseCalculatorService.computeProportionalAmount(
            amount = totalSourceCents,
            targetAmount = expense.groupAmount,
            totalAmount = expense.sourceAmount
        )
        val isForeignCurrency = expense.sourceCurrency != expense.groupCurrency
        val intraType = expense.splits
            .firstOrNull { it.subunitId == subunitId }
            ?.splitType
            ?: SplitType.EQUAL
        return SubunitSplitGroupUiModel(
            subunitId = subunitId,
            subunitLabel = label,
            formattedTotalAmount = formattingHelper.formatCentsWithCurrency(
                totalGroupCents,
                expense.groupCurrency
            ),
            formattedSourceTotalAmount = if (isForeignCurrency) {
                formattingHelper.formatCentsWithCurrency(totalSourceCents, expense.sourceCurrency)
            } else {
                null
            },
            memberCount = members.size,
            members = members.toImmutableList(),
            splitTypeText = resourceProvider.getString(intraType.toStringRes())
        )
    }

    private fun mapSplitRow(
        split: ExpenseSplit,
        expense: Expense,
        memberProfiles: Map<String, User>,
        currentUserId: String?,
        subunitNameLookup: Map<String, String>,
        groupMemberIds: List<String>
    ): SplitDetailUiModel {
        val youLabel = resourceProvider.getString(R.string.you_label)
        val groupAmountCents = expenseCalculatorService.computeProportionalAmount(
            amount = split.amountCents,
            targetAmount = expense.groupAmount,
            totalAmount = expense.sourceAmount
        )
        val isForeignCurrency = expense.sourceCurrency != expense.groupCurrency
        val formattedAmount = if (split.isExcluded) {
            resourceProvider.getString(R.string.add_expense_split_member_excluded)
        } else {
            formattingHelper.formatCentsWithCurrency(groupAmountCents, expense.groupCurrency)
        }
        val formattedSourceAmount = if (isForeignCurrency && !split.isExcluded) {
            formattingHelper.formatCentsWithCurrency(split.amountCents, expense.sourceCurrency)
        } else {
            null
        }
        val shareText = split.percentage?.let { pct ->
            "${formattingHelper.formatForDisplay(pct.toPlainString(), 1)}%"
        }
        val resolvedName = resolveDisplayName(split.userId, memberProfiles, currentUserId, youLabel, userUiMapper)
        val memberDisplay = if (split.userId !in groupMemberIds) {
            MemberDisplay.Former(split.userId, resolvedName)
        } else {
            MemberDisplay.Active(split.userId, resolvedName)
        }
        return SplitDetailUiModel(
            displayName = resolvedName,
            formattedAmount = formattedAmount,
            formattedSourceAmount = formattedSourceAmount,
            shareText = shareText,
            isCurrentUser = currentUserId != null && split.userId == currentUserId,
            isExcluded = split.isExcluded,
            subunitId = split.subunitId,
            subunitLabel = split.subunitId?.let { subunitNameLookup[it] },
            memberDisplay = memberDisplay
        )
    }

    private fun mapAddOns(
        addOns: List<AddOn>,
        groupCurrency: String
    ): ImmutableList<AddOnDetailUiModel> = addOns.map { addOn ->
        val isForeign = addOn.currency != groupCurrency
        AddOnDetailUiModel(
            labelText = buildAddOnLabel(addOn, resourceProvider),
            modeText = resourceProvider.getString(addOn.mode.toStringRes()),
            formattedAmount = formattingHelper.formatCentsWithCurrency(
                addOn.groupAmountCents,
                groupCurrency
            ),
            formattedSourceAmount = if (isForeign) {
                formattingHelper.formatCentsWithCurrency(addOn.amountCents, addOn.currency)
            } else {
                null
            },
            addOnCurrency = addOn.currency,
            formattedRate = if (isForeign) {
                resourceProvider.getString(
                    R.string.expense_detail_exchange_rate_full,
                    addOn.currency,
                    formattingHelper.formatRateForDisplay(addOn.exchangeRate.toPlainString()),
                    groupCurrency
                )
            } else {
                null
            },
            isForeignCurrency = isForeign,
            isIncluded = addOn.mode == AddOnMode.INCLUDED,
            isDiscount = addOn.type == AddOnType.DISCOUNT
        )
    }.toImmutableList()

    private fun mapCashTranches(
        tranches: List<CashTranche>,
        sourceCurrency: String,
        groupCurrency: String,
        withdrawalLookup: Map<String, CashWithdrawal>,
        subunitNameLookup: Map<String, String>
    ): ImmutableList<CashTrancheDetailUiModel> = tranches.map { tranche ->
        val withdrawal = withdrawalLookup[tranche.withdrawalId]
        val withdrawalTitle = withdrawal?.title
        val label = if (!withdrawalTitle.isNullOrBlank()) {
            withdrawalTitle
        } else {
            val formattedDate = formattingHelper.formatShortDate(withdrawal?.createdAt)
            if (formattedDate.isNotBlank()) {
                resourceProvider.getString(R.string.add_expense_cash_tranche_atm_label, formattedDate)
            } else {
                resourceProvider.getString(R.string.add_expense_cash_tranche_atm_label_no_date)
            }
        }
        val scopeText = resolveTrancheScopeText(withdrawal, subunitNameLookup, resourceProvider)
        val formattedRate = buildTrancheRate(withdrawal, groupCurrency, formattingHelper, resourceProvider)
        CashTrancheDetailUiModel(
            withdrawalLabel = label,
            formattedAmountConsumed = formattingHelper.formatCentsWithCurrency(
                tranche.amountConsumed,
                sourceCurrency
            ),
            scopeText = scopeText,
            formattedRate = formattedRate
        )
    }.toImmutableList()
}

private fun resolveSourceAmountFormatted(
    expense: Expense,
    formattingHelper: FormattingHelper,
    isForeign: Boolean
): String? {
    return if (isForeign) {
        formattingHelper.formatCentsWithCurrency(expense.sourceAmount, expense.sourceCurrency)
    } else {
        null
    }
}

private fun resolveExchangeRateFormatted(
    expense: Expense,
    formattingHelper: FormattingHelper,
    isForeign: Boolean
): String? {
    return if (isForeign) {
        formattingHelper.formatRateForDisplay(
            expense.exchangeRate.toPlainString()
        )
    } else {
        null
    }
}

private fun formatIncludedBaseCost(expense: Expense, formattingHelper: FormattingHelper): String? {
    val hasIncludedNonDiscounts = expense.addOns.any {
        it.mode == AddOnMode.INCLUDED && it.type != AddOnType.DISCOUNT
    }
    return if (hasIncludedNonDiscounts) {
        formattingHelper.formatCentsWithCurrency(
            expense.groupAmount,
            expense.groupCurrency
        )
    } else {
        null
    }
}

private fun formatOriginalEnteredTotal(expense: Expense, formattingHelper: FormattingHelper): String? {
    val hasIncludedNonDiscounts = expense.addOns.any {
        it.mode == AddOnMode.INCLUDED && it.type != AddOnType.DISCOUNT
    }
    return if (hasIncludedNonDiscounts) {
        formattingHelper.formatCentsWithCurrency(
            buildOriginalEnteredTotal(expense.groupAmount, expense.addOns),
            expense.groupCurrency
        )
    } else {
        null
    }
}

private fun formatEffectiveTotal(
    expense: Expense,
    formattingHelper: FormattingHelper,
    addOnCalculationService: AddOnCalculationService
): String? {
    return resolveEffectiveTotal(expense.groupAmount, expense.addOns, addOnCalculationService)?.let {
        formattingHelper.formatCentsWithCurrency(it, expense.groupCurrency)
    }
}

private fun getPaidByText(
    expense: Expense,
    currentUserId: String?,
    youLabel: String,
    memberProfiles: Map<String, User>,
    userUiMapper: UserUiMapper,
    resourceProvider: ResourceProvider
): String {
    val paidByName = resolveDisplayName(expense.createdBy, memberProfiles, currentUserId, youLabel, userUiMapper)
    return resolvePaidByText(expense.createdBy, currentUserId, paidByName, resourceProvider)
}

private fun getCreatedByText(
    expense: Expense,
    currentUserId: String?,
    youLabel: String,
    memberProfiles: Map<String, User>,
    userUiMapper: UserUiMapper,
    resourceProvider: ResourceProvider
): String {
    val paidByName = resolveDisplayName(expense.createdBy, memberProfiles, currentUserId, youLabel, userUiMapper)
    return resolveCreatedByText(expense.createdBy, currentUserId, paidByName, resourceProvider)
}

private fun buildOriginalEnteredTotal(baseGroupAmount: Long, addOns: List<AddOn>): Long {
    val includedNonDiscountTotal = addOns
        .filter { it.mode == AddOnMode.INCLUDED && it.type != AddOnType.DISCOUNT }
        .sumOf { it.groupAmountCents }
    return (baseGroupAmount + includedNonDiscountTotal).coerceAtLeast(0L)
}

private fun resolveDisplayName(
    userId: String,
    memberProfiles: Map<String, User>,
    currentUserId: String?,
    youLabel: String,
    userUiMapper: UserUiMapper
): String {
    val user = memberProfiles[userId]
    return userUiMapper.mapToDisplayName(
        user = user,
        fallbackUserId = userId,
        currentUserId = currentUserId,
        youLabel = youLabel
    )
}

private fun resolvePaidByText(
    createdBy: String,
    currentUserId: String?,
    paidByName: String,
    resourceProvider: ResourceProvider
): String {
    return if (createdBy == currentUserId) {
        resourceProvider.getString(R.string.paid_by_you)
    } else {
        resourceProvider.getString(R.string.paid_by, paidByName)
    }
}

private fun resolveEffectiveTotal(
    groupAmount: Long,
    addOns: List<AddOn>,
    addOnCalculationService: AddOnCalculationService
): Long? {
    return if (addOns.isNotEmpty()) {
        addOnCalculationService.calculateEffectiveGroupAmount(groupAmount, addOns)
    } else {
        null
    }
}

private fun resolveSecondaryDateText(expense: Expense, formattingHelper: FormattingHelper): String? {
    return when (expense.paymentStatus) {
        PaymentStatus.SCHEDULED, PaymentStatus.REFUNDABLE -> expense.dueDate?.let {
            formattingHelper.formatShortDate(it)
        }
        else -> null
    }
}

private fun resolveSecondaryDateIcon(expense: Expense): ImageVector? {
    return when (expense.paymentStatus) {
        PaymentStatus.SCHEDULED -> TablerIcons.Outline.Calendar
        PaymentStatus.REFUNDABLE -> TablerIcons.Outline.ReceiptRefund
        else -> null
    }
}

private fun resolveCreatedByText(
    createdBy: String,
    currentUserId: String?,
    paidByName: String,
    resourceProvider: ResourceProvider
): String {
    return if (createdBy == currentUserId) {
        resourceProvider.getString(R.string.expense_detail_created_by_you)
    } else {
        resourceProvider.getString(R.string.expense_detail_created_by, paidByName)
    }
}

private fun buildAddOnLabel(addOn: AddOn, resourceProvider: ResourceProvider): String {
    val typeName = resourceProvider.getString(addOn.type.toStringRes())
    return if (!addOn.description.isNullOrBlank()) {
        "${addOn.description} ($typeName)"
    } else {
        typeName
    }
}

private fun buildTrancheRate(
    withdrawal: CashWithdrawal?,
    groupCurrency: String,
    formattingHelper: FormattingHelper,
    resourceProvider: ResourceProvider
): String? {
    if (withdrawal == null) return null
    if (withdrawal.currency == groupCurrency) return null
    if (withdrawal.exchangeRate.compareTo(BigDecimal.ZERO) == 0) return null
    return resourceProvider.getString(
        R.string.expense_detail_exchange_rate_full,
        withdrawal.currency,
        formattingHelper.formatRateForDisplay(withdrawal.exchangeRate.toPlainString()),
        groupCurrency
    )
}

private fun resolveTrancheScopeText(
    withdrawal: CashWithdrawal?,
    subunitNameLookup: Map<String, String>,
    resourceProvider: ResourceProvider
): String? {
    if (withdrawal == null) return null
    return when (withdrawal.withdrawalScope) {
        PayerType.GROUP -> resourceProvider.getString(R.string.expense_detail_tranche_scope_group)
        PayerType.USER -> resourceProvider.getString(R.string.expense_detail_tranche_scope_personal)
        PayerType.SUBUNIT -> {
            val name = withdrawal.subunitId?.let { subunitNameLookup[it] }
            if (!name.isNullOrBlank()) {
                resourceProvider.getString(R.string.expense_detail_tranche_scope_subunit, name)
            } else {
                null
            }
        }
    }
}

private fun buildFundingSourceText(
    expense: Expense,
    currentUserId: String?,
    memberProfiles: Map<String, User>,
    resourceProvider: ResourceProvider,
    userUiMapper: UserUiMapper
): String? {
    val payerId = expense.payerId ?: expense.createdBy.takeIf { it.isNotBlank() }
    if (expense.payerType != PayerType.USER || payerId == null) return null
    return if (currentUserId != null && payerId == currentUserId) {
        resourceProvider.getString(R.string.expense_paid_by_me)
    } else {
        resourceProvider.getString(
            R.string.expense_paid_by_member,
            resolveDisplayName(payerId, memberProfiles, currentUserId = null, youLabel = "", userUiMapper)
        )
    }
}

private fun buildExpenseScopeLabel(payerType: PayerType, resourceProvider: ResourceProvider): String =
    when (payerType) {
        PayerType.GROUP -> resourceProvider.getString(R.string.expense_scope_group)
        PayerType.SUBUNIT -> resourceProvider.getString(R.string.expense_scope_subunit)
        PayerType.USER -> resourceProvider.getString(R.string.expense_scope_personal)
    }

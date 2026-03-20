package es.pedrazamiguez.expenseshareapp.features.balance.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.MappedPreview
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import es.pedrazamiguez.expenseshareapp.domain.model.MemberBalance
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.BalancesUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ActivityItemUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CashWithdrawalUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ContributionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.GroupPocketBalanceUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.MemberBalanceUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
fun BalanceCardPreviewHelper(
    domainBalance: GroupPocketBalance = PREVIEW_POCKET_BALANCE,
    groupName: String = PREVIEW_GROUP_NAME,
    content: @Composable (GroupPocketBalanceUiModel) -> Unit
) {
    MappedPreview(
        domain = domainBalance,
        mapper = { localeProvider, resourceProvider ->
            BalancesUiMapper(localeProvider, resourceProvider)
        },
        transform = { mapper, domain ->
            mapper.mapBalance(domain, groupName)
        },
        content = content
    )
}

@Composable
fun ContributionItemPreviewHelper(
    domainContribution: Contribution = PREVIEW_CONTRIBUTION_GROUP,
    content: @Composable (ContributionUiModel) -> Unit
) {
    MappedPreview(
        domain = domainContribution,
        mapper = { localeProvider, resourceProvider ->
            BalancesUiMapper(localeProvider, resourceProvider)
        },
        transform = { mapper, domain ->
            mapper.mapContributions(listOf(domain), currentUserId = null).first()
        },
        content = content
    )
}

@Composable
fun ContributionListPreviewHelper(
    domainContributions: List<Contribution> = PREVIEW_CONTRIBUTIONS,
    content: @Composable (ImmutableList<ContributionUiModel>) -> Unit
) {
    MappedPreview(
        domain = domainContributions,
        mapper = { localeProvider, resourceProvider ->
            BalancesUiMapper(localeProvider, resourceProvider)
        },
        transform = { mapper, domain ->
            mapper.mapContributions(domain, currentUserId = null)
        },
        content = content
    )
}

@Composable
fun ActivityListPreviewHelper(
    domainContributions: List<Contribution> = PREVIEW_CONTRIBUTIONS,
    domainWithdrawals: List<CashWithdrawal> = listOf(
        PREVIEW_CASH_WITHDRAWAL_GROUP,
        PREVIEW_CASH_WITHDRAWAL_SUBUNIT,
        PREVIEW_CASH_WITHDRAWAL_PERSONAL
    ),
    groupCurrency: String = "EUR",
    content: @Composable (ImmutableList<ActivityItemUiModel>) -> Unit
) {
    MappedPreview(
        domain = domainContributions to domainWithdrawals,
        mapper = { localeProvider, resourceProvider ->
            BalancesUiMapper(localeProvider, resourceProvider)
        },
        transform = { mapper, domain ->
            mapper.mapActivity(
                contributions = domain.first,
                withdrawals = domain.second,
                groupCurrency = groupCurrency,
                currentUserId = null
            )
        },
        content = content
    )
}

@Composable
fun CashWithdrawalItemPreviewHelper(
    domainWithdrawal: CashWithdrawal = PREVIEW_CASH_WITHDRAWAL_GROUP,
    groupCurrency: String = "EUR",
    content: @Composable (CashWithdrawalUiModel) -> Unit
) {
    MappedPreview(
        domain = domainWithdrawal,
        mapper = { localeProvider, resourceProvider ->
            BalancesUiMapper(localeProvider, resourceProvider)
        },
        transform = { mapper, domain ->
            mapper.mapCashWithdrawals(
                withdrawals = listOf(domain),
                groupCurrency = groupCurrency,
                currentUserId = null
            ).first()
        },
        content = content
    )
}

@Composable
fun MemberBalanceItemPreviewHelper(
    domainBalance: MemberBalance = PREVIEW_MEMBER_BALANCE_POSITIVE,
    currency: String = "EUR",
    content: @Composable (MemberBalanceUiModel) -> Unit
) {
    MappedPreview(
        domain = domainBalance,
        mapper = { localeProvider, resourceProvider ->
            BalancesUiMapper(localeProvider, resourceProvider)
        },
        transform = { mapper, domain ->
            mapper.mapMemberBalances(
                balances = listOf(domain),
                currency = currency,
                currentUserId = null
            ).first()
        },
        content = content
    )
}

@Composable
fun MemberBalanceListPreviewHelper(
    domainBalances: List<MemberBalance> = listOf(
        PREVIEW_MEMBER_BALANCE_POSITIVE,
        PREVIEW_MEMBER_BALANCE_NEGATIVE
    ),
    currency: String = "EUR",
    content: @Composable (ImmutableList<MemberBalanceUiModel>) -> Unit
) {
    MappedPreview(
        domain = domainBalances,
        mapper = { localeProvider, resourceProvider ->
            BalancesUiMapper(localeProvider, resourceProvider)
        },
        transform = { mapper, domain ->
            mapper.mapMemberBalances(
                balances = domain,
                currency = currency,
                currentUserId = null
            )
        },
        content = content
    )
}

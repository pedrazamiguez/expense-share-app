package es.pedrazamiguez.expenseshareapp.features.balance.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.MappedPreview
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.BalancesUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ContributionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.GroupPocketBalanceUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
fun BalanceCardPreviewHelper(
    domainBalance: GroupPocketBalance = PREVIEW_POCKET_BALANCE,
    groupName: String = PREVIEW_GROUP_NAME,
    content: @Composable (GroupPocketBalanceUiModel) -> Unit
) {
    MappedPreview(
        domain = domainBalance,
        mapper = { localeProvider, _ ->
            BalancesUiMapper(localeProvider)
        },
        transform = { mapper, domain ->
            mapper.mapBalance(domain, groupName)
        },
        content = content
    )
}

@Composable
fun ContributionItemPreviewHelper(
    domainContribution: Contribution = PREVIEW_CONTRIBUTION_1,
    content: @Composable (ContributionUiModel) -> Unit
) {
    MappedPreview(
        domain = domainContribution,
        mapper = { localeProvider, _ ->
            BalancesUiMapper(localeProvider)
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
        mapper = { localeProvider, _ ->
            BalancesUiMapper(localeProvider)
        },
        transform = { mapper, domain ->
            mapper.mapContributions(domain, currentUserId = null)
        },
        content = content
    )
}

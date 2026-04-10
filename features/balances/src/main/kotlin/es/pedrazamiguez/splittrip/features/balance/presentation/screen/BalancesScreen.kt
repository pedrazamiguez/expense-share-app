package es.pedrazamiguez.splittrip.features.balance.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Wallet
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.DeferredLoadingContainer
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.EmptyStateView
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.splittrip.features.balance.R
import es.pedrazamiguez.splittrip.features.balance.presentation.component.CashWithdrawalHistoryItem
import es.pedrazamiguez.splittrip.features.balance.presentation.component.ContributionHistoryItem
import es.pedrazamiguez.splittrip.features.balance.presentation.component.GroupPocketBalanceCard
import es.pedrazamiguez.splittrip.features.balance.presentation.component.MemberBalanceItem
import es.pedrazamiguez.splittrip.features.balance.presentation.model.ActivityItemUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.MemberBalanceUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.event.BalancesUiEvent
import es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.state.BalancesUiState
import kotlinx.collections.immutable.ImmutableList

@Composable
fun BalancesScreen(
    uiState: BalancesUiState = BalancesUiState(),
    onEvent: (BalancesUiEvent) -> Unit = {},
    onNavigateToContribution: () -> Unit = {},
    onNavigateToWithdrawal: () -> Unit = {}
) {
    val bottomPadding = LocalBottomPadding.current

    BalancesBodyContent(
        uiState = uiState,
        bottomPadding = bottomPadding,
        onEvent = onEvent,
        onNavigateToContribution = onNavigateToContribution,
        onNavigateToWithdrawal = onNavigateToWithdrawal
    )
}

@Composable
private fun BalancesBodyContent(
    uiState: BalancesUiState,
    bottomPadding: Dp,
    onEvent: (BalancesUiEvent) -> Unit,
    onNavigateToContribution: () -> Unit,
    onNavigateToWithdrawal: () -> Unit
) {
    DeferredLoadingContainer(
        isLoading = uiState.isLoading,
        loadingContent = { ShimmerLoadingList() }
    ) {
        when {
            uiState.pocketBalance.formattedBalance.isEmpty() &&
                uiState.activityItems.isEmpty() -> {
                EmptyStateView(
                    title = stringResource(R.string.balances_empty_title),
                    description = stringResource(R.string.balances_empty_description),
                    icon = TablerIcons.Outline.Wallet
                )
            }

            else -> {
                BalancesListContent(
                    uiState = uiState,
                    bottomPadding = bottomPadding,
                    onEvent = onEvent,
                    onNavigateToContribution = onNavigateToContribution,
                    onNavigateToWithdrawal = onNavigateToWithdrawal
                )
            }
        }
    }
}

@Composable
private fun BalancesListContent(
    uiState: BalancesUiState,
    bottomPadding: Dp,
    onEvent: (BalancesUiEvent) -> Unit,
    onNavigateToContribution: () -> Unit,
    onNavigateToWithdrawal: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 16.dp + bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            Column {
                Text(
                    text = stringResource(R.string.balances_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.balances_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            GroupPocketBalanceCard(
                balance = uiState.pocketBalance,
                shouldAnimateBalance = uiState.shouldAnimateBalance,
                previousBalance = uiState.previousBalance,
                balanceRollingUp = uiState.balanceRollingUp,
                onBalanceAnimationComplete = { onEvent(BalancesUiEvent.BalanceAnimationComplete) },
                onAddMoney = onNavigateToContribution,
                onWithdrawCash = onNavigateToWithdrawal
            )
        }
        memberBalancesSection(uiState.memberBalances)
        activitySection(uiState.activityItems)
    }
}

private fun LazyListScope.memberBalancesSection(memberBalances: ImmutableList<MemberBalanceUiModel>) {
    if (memberBalances.isEmpty()) return
    item {
        Text(
            text = stringResource(R.string.balances_member_balances_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
    items(items = memberBalances, key = { "mb-${it.userId}" }) { memberBalance ->
        MemberBalanceItem(memberBalance = memberBalance)
    }
}

private fun LazyListScope.activitySection(activityItems: ImmutableList<ActivityItemUiModel>) {
    if (activityItems.isEmpty()) return
    item {
        Text(
            text = stringResource(R.string.balances_history_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
    items(
        items = activityItems,
        key = { item ->
            when (item) {
                is ActivityItemUiModel.ContributionItem -> "c-${item.contribution.id}"
                is ActivityItemUiModel.CashWithdrawalItem -> "cw-${item.withdrawal.id}"
            }
        }
    ) { item ->
        when (item) {
            is ActivityItemUiModel.ContributionItem -> ContributionHistoryItem(contribution = item.contribution)
            is ActivityItemUiModel.CashWithdrawalItem -> CashWithdrawalHistoryItem(withdrawal = item.withdrawal)
        }
    }
}

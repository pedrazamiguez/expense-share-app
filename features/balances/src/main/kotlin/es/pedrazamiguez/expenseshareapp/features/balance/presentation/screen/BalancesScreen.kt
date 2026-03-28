package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.LocalAtm
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.DeferredLoadingContainer
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.EmptyStateView
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.scaffold.ExpressiveFab
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.rememberConnectedScrollBehavior
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.CashWithdrawalHistoryItem
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.ContributionHistoryItem
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.GroupPocketBalanceCard
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.MemberBalanceItem
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ActivityItemUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.MemberBalanceUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.BalancesUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.BalancesUiState
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalancesScreen(
    uiState: BalancesUiState = BalancesUiState(),
    onEvent: (BalancesUiEvent) -> Unit = {},
    onNavigateToContribution: () -> Unit = {},
    onNavigateToWithdrawal: () -> Unit = {}
) {
    val bottomPadding = LocalBottomPadding.current
    val scrollBehavior = rememberConnectedScrollBehavior()
    Box(modifier = Modifier.fillMaxSize()) {
        DeferredLoadingContainer(
            isLoading = uiState.isLoading,
            loadingContent = { ShimmerLoadingList() }
        ) {
            when {
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.pocketBalance.formattedBalance.isEmpty() &&
                    uiState.activityItems.isEmpty() -> {
                    EmptyStateView(
                        title = stringResource(R.string.balances_empty_title),
                        description = stringResource(R.string.balances_empty_description),
                        icon = Icons.Outlined.AccountBalanceWallet
                    )
                }

                else -> {
                    BalancesListContent(
                        uiState = uiState,
                        scrollBehavior = scrollBehavior,
                        bottomPadding = bottomPadding,
                        onEvent = onEvent
                    )
                }
            }
        }

        val showFabs = !uiState.isLoading && uiState.errorMessage == null
        BalancesFabSection(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .alpha(if (showFabs) 1f else 0f)
                .then(if (!showFabs) Modifier.clearAndSetSemantics { } else Modifier),
            bottomPadding = bottomPadding,
            onNavigateToWithdrawal = if (showFabs) {
                onNavigateToWithdrawal
            } else {
                {}
            },
            onNavigateToContribution = if (showFabs) {
                onNavigateToContribution
            } else {
                {}
            }
        )
    }
}

@Composable
private fun BalancesFabSection(
    modifier: Modifier = Modifier,
    bottomPadding: Dp,
    onNavigateToWithdrawal: () -> Unit,
    onNavigateToContribution: () -> Unit
) {
    Column(
        modifier = modifier.padding(end = 16.dp, bottom = 16.dp + bottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End
    ) {
        ExpressiveFab(
            onClick = onNavigateToWithdrawal,
            icon = Icons.Outlined.LocalAtm,
            contentDescription = stringResource(R.string.balances_withdraw_cash),
            modifier = Modifier,
            sharedTransitionKey = ADD_CASH_WITHDRAWAL_SHARED_ELEMENT_KEY
        )
        ExpressiveFab(
            onClick = onNavigateToContribution,
            icon = Icons.Outlined.Add,
            contentDescription = stringResource(R.string.balances_add_money),
            modifier = Modifier,
            sharedTransitionKey = ADD_CONTRIBUTION_SHARED_ELEMENT_KEY
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BalancesListContent(
    uiState: BalancesUiState,
    scrollBehavior: TopAppBarScrollBehavior,
    bottomPadding: Dp,
    onEvent: (BalancesUiEvent) -> Unit
) {
    val fabExtraPadding = 148.dp
    LazyColumn(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 16.dp + bottomPadding + fabExtraPadding
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GroupPocketBalanceCard(
                balance = uiState.pocketBalance,
                shouldAnimateBalance = uiState.shouldAnimateBalance,
                previousBalance = uiState.previousBalance,
                balanceRollingUp = uiState.balanceRollingUp,
                onBalanceAnimationComplete = { onEvent(BalancesUiEvent.BalanceAnimationComplete) }
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

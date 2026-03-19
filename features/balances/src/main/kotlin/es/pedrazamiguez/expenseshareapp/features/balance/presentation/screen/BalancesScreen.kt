package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.LocalAtm
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.BalancesUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.BalancesUiState

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
                    val fabExtraPadding = 148.dp
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp + bottomPadding + fabExtraPadding
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Balance Header Card
                        item {
                            GroupPocketBalanceCard(
                                balance = uiState.pocketBalance,
                                shouldAnimateBalance = uiState.shouldAnimateBalance,
                                previousBalance = uiState.previousBalance,
                                balanceRollingUp = uiState.balanceRollingUp,
                                onBalanceAnimationComplete = {
                                    onEvent(BalancesUiEvent.BalanceAnimationComplete)
                                }
                            )
                        }

                        // Member Balances Section
                        val hasMemberBalances = uiState.memberBalances.isNotEmpty()

                        if (hasMemberBalances) {
                            item {
                                Text(
                                    text = stringResource(R.string.balances_member_balances_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            items(
                                items = uiState.memberBalances,
                                key = { "mb-${it.userId}" }
                            ) { memberBalance ->
                                MemberBalanceItem(memberBalance = memberBalance)
                            }
                        }

                        // Activity Section Header
                        val hasActivity = uiState.activityItems.isNotEmpty()

                        if (hasActivity) {
                            item {
                                Text(
                                    text = stringResource(R.string.balances_history_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            // Merged activity items sorted by date descending
                            items(
                                items = uiState.activityItems,
                                key = { item ->
                                    when (item) {
                                        is ActivityItemUiModel.ContributionItem -> "c-${item.contribution.id}"
                                        is ActivityItemUiModel.CashWithdrawalItem -> "cw-${item.withdrawal.id}"
                                    }
                                }
                            ) { item ->
                                when (item) {
                                    is ActivityItemUiModel.ContributionItem ->
                                        ContributionHistoryItem(contribution = item.contribution)

                                    is ActivityItemUiModel.CashWithdrawalItem ->
                                        CashWithdrawalHistoryItem(withdrawal = item.withdrawal)
                                }
                            }
                        }
                    }
                }
            }
        }

        // FABs - Add Money & Withdraw Cash
        if (!uiState.isLoading && uiState.errorMessage == null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp + bottomPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Secondary FAB: Withdraw Cash
                ExpressiveFab(
                    onClick = onNavigateToWithdrawal,
                    icon = Icons.Outlined.LocalAtm,
                    contentDescription = stringResource(R.string.balances_withdraw_cash),
                    modifier = Modifier,
                    sharedTransitionKey = ADD_CASH_WITHDRAWAL_SHARED_ELEMENT_KEY
                )

                // Primary FAB: Add Money
                ExpressiveFab(
                    onClick = onNavigateToContribution,
                    icon = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.balances_add_money),
                    modifier = Modifier,
                    sharedTransitionKey = ADD_CONTRIBUTION_SHARED_ELEMENT_KEY
                )
            }
        }
    }
}

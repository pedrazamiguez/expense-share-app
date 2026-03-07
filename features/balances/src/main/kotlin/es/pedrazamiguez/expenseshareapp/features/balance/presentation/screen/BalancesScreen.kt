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
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.EmptyStateView
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.DeferredLoadingContainer
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.scaffold.ExpressiveFab
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.rememberConnectedScrollBehavior
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.AddMoneyDialog
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.CashWithdrawalHistoryItem
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.ContributionHistoryItem
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.GroupPocketBalanceCard
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.BalancesUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.BalancesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalancesScreen(
    uiState: BalancesUiState = BalancesUiState(),
    onEvent: (BalancesUiEvent) -> Unit = {},
    onNavigateToWithdrawal: () -> Unit = {}
) {
    val bottomPadding = LocalBottomPadding.current
    val scrollBehavior = rememberConnectedScrollBehavior()

    // Add Money Dialog
    if (uiState.isAddMoneyDialogVisible) {
        AddMoneyDialog(
            amountInput = uiState.contributionAmountInput,
            amountError = uiState.contributionAmountError,
            onEvent = onEvent
        )
    }

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
                        uiState.contributions.isEmpty() &&
                        uiState.cashWithdrawals.isEmpty() -> {
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
                            GroupPocketBalanceCard(balance = uiState.pocketBalance)
                        }

                        // Activity Section Header
                        val hasActivity = uiState.contributions.isNotEmpty() ||
                                uiState.cashWithdrawals.isNotEmpty()

                        if (hasActivity) {
                            item {
                                Text(
                                    text = stringResource(R.string.balances_history_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            // Cash Withdrawal history items
                            items(
                                items = uiState.cashWithdrawals,
                                key = { "cw-${it.id}" }
                            ) { withdrawal ->
                                CashWithdrawalHistoryItem(withdrawal = withdrawal)
                            }

                            // Contribution history items
                            items(
                                items = uiState.contributions,
                                key = { "c-${it.id}" }
                            ) { contribution ->
                                ContributionHistoryItem(contribution = contribution)
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
                    onClick = { onEvent(BalancesUiEvent.ShowAddMoneyDialog) },
                    icon = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.balances_add_money),
                    modifier = Modifier
                )
            }
        }
    }
}

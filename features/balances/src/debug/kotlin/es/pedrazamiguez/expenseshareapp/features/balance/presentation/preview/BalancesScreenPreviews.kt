package es.pedrazamiguez.expenseshareapp.features.balance.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ContributionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.GroupPocketBalanceUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.BalancesScreen
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.BalancesUiState
import kotlinx.collections.immutable.persistentListOf

@PreviewComplete
@Composable
private fun BalancesScreenLoadingPreview() {
    PreviewThemeWrapper {
        BalancesScreen(
            uiState = BalancesUiState(isLoading = true)
        )
    }
}

@PreviewComplete
@Composable
private fun BalancesScreenEmptyPreview() {
    PreviewThemeWrapper {
        BalancesScreen(
            uiState = BalancesUiState(isLoading = false, groupId = "group-1")
        )
    }
}

@PreviewComplete
@Composable
private fun BalancesScreenWithDataPreview() {
    PreviewThemeWrapper {
        BalancesScreen(
            uiState = BalancesUiState(
                isLoading = false,
                groupId = "group-1",
                pocketBalance = GroupPocketBalanceUiModel(
                    formattedBalance = "€1,034.55",
                    formattedTotalContributed = "€1,200.00",
                    formattedTotalSpent = "€165.45",
                    currency = "EUR"
                ),
                contributions = persistentListOf(
                    ContributionUiModel(
                        id = "c1",
                        userId = "Antonio",
                        formattedAmount = "€300.00",
                        dateText = "Jan 15, 2026"
                    ),
                    ContributionUiModel(
                        id = "c2",
                        userId = "Maria",
                        formattedAmount = "€300.00",
                        dateText = "Jan 15, 2026"
                    ),
                    ContributionUiModel(
                        id = "c3",
                        userId = "Pedro",
                        formattedAmount = "€300.00",
                        dateText = "Jan 14, 2026"
                    ),
                    ContributionUiModel(
                        id = "c4",
                        userId = "Laura",
                        formattedAmount = "€300.00",
                        dateText = "Jan 14, 2026"
                    )
                )
            )
        )
    }
}

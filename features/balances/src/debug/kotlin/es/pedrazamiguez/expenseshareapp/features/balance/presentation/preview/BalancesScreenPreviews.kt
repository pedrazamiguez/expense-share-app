package es.pedrazamiguez.expenseshareapp.features.balance.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.BalancesScreen
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.view.BalanceView
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.BalancesUiState
import kotlinx.collections.immutable.persistentListOf

@PreviewComplete
@Composable
private fun BalanceScreenPreview() {
    PreviewThemeWrapper {
        BalancesScreen(
            uiState = BalancesUiState(
                balances = persistentListOf(
                    BalanceView(
                        userId = "1",
                        balanceId = "b1",
                        amount = "+20.0",
                        currencyCode = "USD"
                    ),
                    BalanceView(
                        userId = "2",
                        balanceId = "b2",
                        amount = "-15.5",
                        currencyCode = "EUR"
                    ),
                    BalanceView(
                        userId = "3",
                        balanceId = "b3",
                        amount = "+5.0",
                        currencyCode = "GBP"
                    )
                )
            )
        )
    }
}

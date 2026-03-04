package es.pedrazamiguez.expenseshareapp.features.balance.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewLocales
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.GroupPocketBalanceCard
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.GroupPocketBalanceUiModel

@PreviewLocales
@Composable
private fun GroupPocketBalanceCardPreview() {
    PreviewThemeWrapper {
        GroupPocketBalanceCard(
            balance = GroupPocketBalanceUiModel(
                formattedBalance = "€1,034.55",
                formattedTotalContributed = "€1,200.00",
                formattedTotalSpent = "€165.45",
                currency = "EUR"
            )
        )
    }
}

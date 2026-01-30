package es.pedrazamiguez.expenseshareapp.features.group.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.CreateGroupScreen
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateGroupUiState
import kotlinx.collections.immutable.persistentListOf

@PreviewComplete
@Composable
private fun CreateGroupScreenPreview() {
    PreviewThemeWrapper {
        CreateGroupScreen(
            uiState = CreateGroupUiState(
                availableCurrencies = persistentListOf(
                    CURRENCY_EUR,
                    CURRENCY_USD,
                    CURRENCY_MXN,
                )
            )
        )
    }
}

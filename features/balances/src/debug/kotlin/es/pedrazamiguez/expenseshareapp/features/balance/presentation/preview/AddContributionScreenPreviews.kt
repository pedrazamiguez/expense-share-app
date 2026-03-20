package es.pedrazamiguez.expenseshareapp.features.balance.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.SubunitOptionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.AddContributionScreen
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddContributionUiState
import kotlinx.collections.immutable.persistentListOf

@PreviewComplete
@Composable
private fun AddContributionScreenEmptyPreview() {
    PreviewThemeWrapper {
        AddContributionScreen(
            groupId = "group-1",
            uiState = AddContributionUiState()
        )
    }
}

@PreviewComplete
@Composable
private fun AddContributionScreenFilledPreview() {
    PreviewThemeWrapper {
        AddContributionScreen(
            groupId = "group-1",
            uiState = AddContributionUiState(
                amountInput = "300",
                contributionScope = PayerType.GROUP
            )
        )
    }
}

@PreviewComplete
@Composable
private fun AddContributionScreenWithSubunitsPreview() {
    PreviewThemeWrapper {
        AddContributionScreen(
            groupId = "group-1",
            uiState = AddContributionUiState(
                amountInput = "150",
                contributionScope = PayerType.SUBUNIT,
                selectedSubunitId = "subunit-1",
                subunitOptions = persistentListOf(
                    SubunitOptionUiModel("subunit-1", "Couple A"),
                    SubunitOptionUiModel("subunit-2", "Couple B")
                )
            )
        )
    }
}

@PreviewComplete
@Composable
private fun AddContributionScreenErrorPreview() {
    PreviewThemeWrapper {
        AddContributionScreen(
            groupId = "group-1",
            uiState = AddContributionUiState(
                amountInput = "",
                amountError = true
            )
        )
    }
}

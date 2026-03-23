package es.pedrazamiguez.expenseshareapp.features.balance.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.SubunitOptionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.AddContributionScreen
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddContributionStep
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddContributionUiState
import kotlinx.collections.immutable.persistentListOf

@PreviewComplete
@Composable
private fun AddContributionAmountStepEmptyPreview() {
    PreviewThemeWrapper {
        AddContributionScreen(
            groupId = "group-1",
            uiState = AddContributionUiState(
                currentStep = AddContributionStep.AMOUNT,
                groupCurrencyCode = "EUR",
                groupCurrencySymbol = "€"
            )
        )
    }
}

@PreviewComplete
@Composable
private fun AddContributionAmountStepFilledPreview() {
    PreviewThemeWrapper {
        AddContributionScreen(
            groupId = "group-1",
            uiState = AddContributionUiState(
                currentStep = AddContributionStep.AMOUNT,
                amountInput = "300",
                groupCurrencyCode = "EUR",
                groupCurrencySymbol = "€"
            )
        )
    }
}

@PreviewComplete
@Composable
private fun AddContributionAmountStepErrorPreview() {
    PreviewThemeWrapper {
        AddContributionScreen(
            groupId = "group-1",
            uiState = AddContributionUiState(
                currentStep = AddContributionStep.AMOUNT,
                amountInput = "",
                amountError = true,
                groupCurrencyCode = "EUR",
                groupCurrencySymbol = "€"
            )
        )
    }
}

@PreviewComplete
@Composable
private fun AddContributionScopeStepGroupPreview() {
    PreviewThemeWrapper {
        AddContributionScreen(
            groupId = "group-1",
            uiState = AddContributionUiState(
                currentStep = AddContributionStep.SCOPE,
                amountInput = "300",
                groupCurrencyCode = "EUR",
                groupCurrencySymbol = "€",
                contributionScope = PayerType.GROUP
            )
        )
    }
}

@PreviewComplete
@Composable
private fun AddContributionScopeStepSubunitPreview() {
    PreviewThemeWrapper {
        AddContributionScreen(
            groupId = "group-1",
            uiState = AddContributionUiState(
                currentStep = AddContributionStep.SCOPE,
                amountInput = "150",
                groupCurrencyCode = "EUR",
                groupCurrencySymbol = "€",
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
private fun AddContributionReviewStepGroupPreview() {
    PreviewThemeWrapper {
        AddContributionScreen(
            groupId = "group-1",
            uiState = AddContributionUiState(
                currentStep = AddContributionStep.REVIEW,
                amountInput = "300",
                groupCurrencyCode = "EUR",
                formattedAmountWithCurrency = "300,00\u00A0€",
                contributionScope = PayerType.GROUP
            )
        )
    }
}

@PreviewComplete
@Composable
private fun AddContributionReviewStepSubunitPreview() {
    PreviewThemeWrapper {
        AddContributionScreen(
            groupId = "group-1",
            uiState = AddContributionUiState(
                currentStep = AddContributionStep.REVIEW,
                amountInput = "150",
                groupCurrencyCode = "EUR",
                formattedAmountWithCurrency = "150,00\u00A0€",
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

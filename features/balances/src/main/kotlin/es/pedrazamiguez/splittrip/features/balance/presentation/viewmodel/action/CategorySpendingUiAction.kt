package es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.action

sealed interface CategorySpendingUiAction {
    data object NavigateBack : CategorySpendingUiAction
}

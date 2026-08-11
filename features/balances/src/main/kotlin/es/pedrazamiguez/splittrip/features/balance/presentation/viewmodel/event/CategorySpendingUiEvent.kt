package es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.event

sealed interface CategorySpendingUiEvent {
    data object OnNavigateBack : CategorySpendingUiEvent
}

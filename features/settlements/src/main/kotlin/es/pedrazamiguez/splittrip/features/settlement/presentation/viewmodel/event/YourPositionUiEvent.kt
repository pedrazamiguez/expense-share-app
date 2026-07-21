package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.event

sealed class YourPositionUiEvent {
    data object Refresh : YourPositionUiEvent()
    data object ShowCashBreakdown : YourPositionUiEvent()
    data object DismissCashBreakdown : YourPositionUiEvent()
}

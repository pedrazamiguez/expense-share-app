package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.event

sealed class MyPositionUiEvent {
    data object Refresh : MyPositionUiEvent()
}

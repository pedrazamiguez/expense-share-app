package es.pedrazamiguez.expenseshareapp.features.profile.presentation.viewmodel.event

sealed interface ProfileUiEvent {
    data object LoadProfile : ProfileUiEvent
}

package es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.event

sealed interface ContributionDetailUiEvent {
    data object DeleteConfirmed : ContributionDetailUiEvent
}

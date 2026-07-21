package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.state

import es.pedrazamiguez.splittrip.features.settlement.presentation.model.PersonalPositionUiModel

data class YourPositionUiState(
    val isLoading: Boolean = true,
    val personalPosition: PersonalPositionUiModel? = null
)

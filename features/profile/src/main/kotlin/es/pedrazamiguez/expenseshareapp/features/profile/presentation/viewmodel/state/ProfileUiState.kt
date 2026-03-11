package es.pedrazamiguez.expenseshareapp.features.profile.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.features.profile.presentation.model.ProfileUiModel

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: ProfileUiModel? = null,
    val errorMessage: String? = null
)


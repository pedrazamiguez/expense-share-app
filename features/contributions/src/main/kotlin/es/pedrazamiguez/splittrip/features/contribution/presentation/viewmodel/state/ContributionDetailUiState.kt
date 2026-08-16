package es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.state

import es.pedrazamiguez.splittrip.features.contribution.presentation.model.ContributionDetailUiModel

data class ContributionDetailUiState(
    val contribution: ContributionDetailUiModel? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val isGroupArchived: Boolean = false
)

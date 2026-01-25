package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.GroupUiModel

data class ListUserGroupsUiState(
    val groups: List<GroupUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val scrollPosition: Int = 0,
    val scrollOffset: Int = 0
)

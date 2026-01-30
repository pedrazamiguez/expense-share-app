package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.GroupUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ListUserGroupsUiState(
    val groups: ImmutableList<GroupUiModel> = persistentListOf(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val scrollPosition: Int = 0,
    val scrollOffset: Int = 0
)

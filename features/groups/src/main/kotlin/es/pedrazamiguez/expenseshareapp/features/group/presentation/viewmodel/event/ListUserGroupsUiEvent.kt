package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event

sealed interface ListUserGroupsUiEvent {
    data object LoadGroups : ListUserGroupsUiEvent
    data class ScrollPositionChanged(val index: Int, val offset: Int) : ListUserGroupsUiEvent
}

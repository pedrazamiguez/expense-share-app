package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event

sealed interface GroupsUiEvent {
    data object LoadGroups : GroupsUiEvent
    data class ScrollPositionChanged(val index: Int, val offset: Int) : GroupsUiEvent
}

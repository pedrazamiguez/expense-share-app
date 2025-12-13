package es.pedrazamiguez.expenseshareapp.features.group.presentation.model

sealed interface CreateGroupUiAction {
    data object None : CreateGroupUiAction
    data class ShowError(val message: String) : CreateGroupUiAction
}

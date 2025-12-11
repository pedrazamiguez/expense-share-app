package es.pedrazamiguez.expenseshareapp.ui.group.presentation.model

sealed interface CreateGroupUiAction {
    data object None : CreateGroupUiAction
    data class ShowError(val message: String) : CreateGroupUiAction
}

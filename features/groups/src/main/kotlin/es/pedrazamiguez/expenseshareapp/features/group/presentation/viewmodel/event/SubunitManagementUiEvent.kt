package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event

sealed interface SubunitManagementUiEvent {
    data object CreateSubunit : SubunitManagementUiEvent
    data class EditSubunit(val subunitId: String) : SubunitManagementUiEvent
    data class ConfirmDeleteSubunit(val subunitId: String) : SubunitManagementUiEvent
}

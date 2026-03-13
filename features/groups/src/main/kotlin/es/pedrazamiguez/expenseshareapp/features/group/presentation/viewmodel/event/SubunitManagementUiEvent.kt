package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event

sealed interface SubunitManagementUiEvent {
    data object ShowCreateDialog : SubunitManagementUiEvent
    data class ShowEditDialog(val subunitId: String) : SubunitManagementUiEvent
    data object DismissDialog : SubunitManagementUiEvent
    data class UpdateName(val name: String) : SubunitManagementUiEvent
    data class ToggleMember(val userId: String) : SubunitManagementUiEvent
    data class UpdateMemberShare(val userId: String, val share: String) : SubunitManagementUiEvent
    data object SaveSubunit : SubunitManagementUiEvent
    data class RequestDeleteSubunit(val subunitId: String) : SubunitManagementUiEvent
    data class ConfirmDeleteSubunit(val subunitId: String) : SubunitManagementUiEvent
}


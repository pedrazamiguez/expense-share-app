package es.pedrazamiguez.expenseshareapp.ui.group.presentation.model

sealed interface CreateGroupUiEvent {
    data class NameChanged(val name: String) : CreateGroupUiEvent
    data class CurrencyChanged(val currency: String) : CreateGroupUiEvent
    data class DescriptionChanged(val description: String) : CreateGroupUiEvent
    data object SubmitCreateGroup : CreateGroupUiEvent
}

package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event

import es.pedrazamiguez.expenseshareapp.domain.model.User

sealed interface CreateGroupUiEvent {
    data object LoadCurrencies : CreateGroupUiEvent
    data class NameChanged(val name: String) : CreateGroupUiEvent
    data class CurrencySelected(val code: String) : CreateGroupUiEvent
    data class ExtraCurrencyToggled(val code: String) : CreateGroupUiEvent
    data class DescriptionChanged(val description: String) : CreateGroupUiEvent
    data class MemberSearchQueryChanged(val query: String) : CreateGroupUiEvent
    data class MemberSelected(val user: User) : CreateGroupUiEvent
    data class MemberRemoved(val user: User) : CreateGroupUiEvent
    data object SubmitCreateGroup : CreateGroupUiEvent
}

package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event

import es.pedrazamiguez.expenseshareapp.domain.model.Currency

sealed interface CreateGroupUiEvent {
    data object LoadCurrencies : CreateGroupUiEvent
    data class NameChanged(val name: String) : CreateGroupUiEvent
    data class CurrencySelected(val currency: Currency) : CreateGroupUiEvent
    data class ExtraCurrencyToggled(val currency: Currency) : CreateGroupUiEvent
    data class DescriptionChanged(val description: String) : CreateGroupUiEvent
    data object SubmitCreateGroup : CreateGroupUiEvent
}
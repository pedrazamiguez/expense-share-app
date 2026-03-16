package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event

sealed interface AddContributionUiEvent {
    data class LoadSubunitOptions(val groupId: String?) : AddContributionUiEvent
    data class UpdateAmount(val amount: String) : AddContributionUiEvent
    data class SelectSubunit(val subunitId: String?) : AddContributionUiEvent
    data class Submit(val groupId: String?) : AddContributionUiEvent
}


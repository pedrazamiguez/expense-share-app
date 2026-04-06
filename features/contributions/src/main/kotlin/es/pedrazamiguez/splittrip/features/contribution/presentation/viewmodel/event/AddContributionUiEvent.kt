package es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.event

import es.pedrazamiguez.splittrip.domain.enums.PayerType

sealed interface AddContributionUiEvent {
    data class LoadGroupConfig(val groupId: String?) : AddContributionUiEvent
    data class UpdateAmount(val amount: String) : AddContributionUiEvent
    data class ContributionScopeSelected(
        val scope: PayerType,
        val subunitId: String? = null
    ) : AddContributionUiEvent
    data class MemberSelected(val userId: String) : AddContributionUiEvent
    data class Submit(val groupId: String?) : AddContributionUiEvent
    data object NextStep : AddContributionUiEvent
    data object PreviousStep : AddContributionUiEvent
}

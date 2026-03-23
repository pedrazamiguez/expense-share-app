package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.SubunitOptionUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class AddContributionUiState(
    val isLoading: Boolean = false,
    val amountInput: String = "",
    val amountError: Boolean = false,
    val subunitOptions: ImmutableList<SubunitOptionUiModel> = persistentListOf(),
    val contributionScope: PayerType = PayerType.USER,
    val selectedSubunitId: String? = null,
    val error: UiText? = null
)

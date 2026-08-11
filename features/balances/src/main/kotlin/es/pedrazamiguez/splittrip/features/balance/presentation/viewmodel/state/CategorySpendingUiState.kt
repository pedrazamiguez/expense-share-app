package es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.state

import es.pedrazamiguez.splittrip.features.balance.presentation.model.CategorySpendingUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class CategorySpendingUiState(
    val isLoading: Boolean = true,
    val items: ImmutableList<CategorySpendingUiModel> = persistentListOf(),
    val totalFormattedAmount: String = ""
)

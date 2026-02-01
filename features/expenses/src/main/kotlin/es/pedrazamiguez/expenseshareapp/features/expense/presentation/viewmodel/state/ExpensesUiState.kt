package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ExpensesUiState(
    val expenses: ImmutableList<ExpenseUiModel> = persistentListOf(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val scrollPosition: Int = 0,
    val scrollOffset: Int = 0,
    val groupId: String? = null
)

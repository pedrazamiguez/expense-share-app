package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseDateGroupUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ExpensesUiState(
    val expenseGroups: ImmutableList<ExpenseDateGroupUiModel> = persistentListOf(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val scrollPosition: Int = 0,
    val scrollOffset: Int = 0,
    val groupId: String? = null
) {
    /** True when there are no expenses across all date groups. */
    val isEmpty: Boolean
        get() = expenseGroups.all { it.expenses.isEmpty() }
}

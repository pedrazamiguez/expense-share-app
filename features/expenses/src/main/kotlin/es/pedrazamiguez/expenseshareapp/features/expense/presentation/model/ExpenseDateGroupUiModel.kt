package es.pedrazamiguez.expenseshareapp.features.expense.presentation.model

import kotlinx.collections.immutable.ImmutableList

/**
 * Represents a group of expenses for a single day.
 * Used to render sticky headers in the expense list.
 */
data class ExpenseDateGroupUiModel(
    val dateText: String,
    val formattedDayTotal: String,
    val expenses: ImmutableList<ExpenseUiModel>
)

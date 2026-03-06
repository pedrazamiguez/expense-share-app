package es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions

import androidx.annotation.StringRes
import es.pedrazamiguez.expenseshareapp.domain.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.features.expense.R

@StringRes
fun ExpenseCategory.toStringRes(): Int = when (this) {
    ExpenseCategory.CONTRIBUTION -> R.string.expense_category_contribution
    ExpenseCategory.REFUND -> R.string.expense_category_refund
    ExpenseCategory.TRANSPORT -> R.string.expense_category_transport
    ExpenseCategory.FOOD -> R.string.expense_category_food
    ExpenseCategory.LODGING -> R.string.expense_category_lodging
    ExpenseCategory.ACTIVITIES -> R.string.expense_category_activities
    ExpenseCategory.INSURANCE -> R.string.expense_category_insurance
    ExpenseCategory.ENTERTAINMENT -> R.string.expense_category_entertainment
    ExpenseCategory.SHOPPING -> R.string.expense_category_shopping
    ExpenseCategory.OTHER -> R.string.expense_category_other
}


package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseUiModel

class ExpenseUiMapper(
    private val localeProvider: LocaleProvider
) {

    fun map(expense: Expense): ExpenseUiModel {
        localeProvider.getCurrentLocale()
        return ExpenseUiModel(
            id = expense.id, title = expense.title, formattedAmount = ""
        )
    }

}

package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatAmount
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatShortDate
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseUiModel

class ExpenseUiMapper(
    private val localeProvider: LocaleProvider, private val resourceProvider: ResourceProvider
) {

    fun map(expense: Expense): ExpenseUiModel {
        val appLocale = localeProvider.getCurrentLocale()
        return ExpenseUiModel(
            id = expense.id,
            title = expense.title,
            formattedAmount = expense.formatAmount(appLocale),
            paidByText = resourceProvider.getString(R.string.paid_by, expense.createdBy),
            dateText = expense.createdAt?.formatShortDate(appLocale) ?: ""
        )
    }

}

package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatAmount
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseUiModel
import java.time.format.DateTimeFormatter

class ExpenseUiMapper(
    private val localeProvider: LocaleProvider
) {

    fun map(expense: Expense): ExpenseUiModel {
        val appLocale = localeProvider.getCurrentLocale()
        return ExpenseUiModel(
            id = expense.id,
            title = expense.title,
            formattedAmount = expense.formatAmount(appLocale),
            paidByText = "Paid by ${expense.createdBy}",
            dateText = expense.createdAt?.format(DateTimeFormatter.ofPattern("dd MMM")) ?: ""
        )
    }

}

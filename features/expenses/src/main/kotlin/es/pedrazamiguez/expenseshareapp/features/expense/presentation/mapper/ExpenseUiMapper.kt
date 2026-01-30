package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatAmount
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatShortDate
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class ExpenseUiMapper(
    private val localeProvider: LocaleProvider, private val resourceProvider: ResourceProvider
) {

    fun map(expense: Expense): ExpenseUiModel {
        val appLocale = localeProvider.getCurrentLocale()
        return with(expense) {
            ExpenseUiModel(
                id = id,
                title = title,
                formattedAmount = formatAmount(appLocale),
                paidByText = resourceProvider.getString(R.string.paid_by, createdBy),
                dateText = createdAt?.formatShortDate(appLocale) ?: ""
            )
        }
    }

    fun mapList(expenses: List<Expense>): ImmutableList<ExpenseUiModel> =
        expenses.map { map(it) }.toImmutableList()

}

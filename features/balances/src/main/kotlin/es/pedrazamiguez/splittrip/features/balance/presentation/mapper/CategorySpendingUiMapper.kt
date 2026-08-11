package es.pedrazamiguez.splittrip.features.balance.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.designsystem.foundation.ChartColors
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatCurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.features.balance.presentation.model.CategorySpendingUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class CategorySpendingUiMapper(
    private val localeProvider: LocaleProvider
) {
    fun mapExpenses(expenses: List<Expense>, groupCurrency: String): ImmutableList<CategorySpendingUiModel> {
        val locale = localeProvider.getCurrentLocale()

        val positiveExpenses = expenses.filter { it.groupAmount > 0 }

        val categoryTotals = positiveExpenses
            .groupBy { it.category }
            .mapValues { (_, categoryExpenses) ->
                categoryExpenses.sumOf { it.groupAmount }
            }

        val globalTotal = categoryTotals.values.sum()

        val sortedCategories = categoryTotals.entries
            .sortedByDescending { it.value }

        return sortedCategories.mapIndexed { index, (category, amount) ->
            val progress = if (globalTotal > 0L) {
                amount.toFloat() / globalTotal.toFloat()
            } else {
                0f
            }

            CategorySpendingUiModel(
                category = category,
                formattedAmount = formatCurrencyAmount(amount, groupCurrency, locale),
                progress = progress,
                color = ChartColors[index % ChartColors.size]
            )
        }.toImmutableList()
    }

    fun formatTotalAmount(totalCents: Long, groupCurrency: String): String {
        if (totalCents <= 0) return ""
        return formatCurrencyAmount(totalCents, groupCurrency, localeProvider.getCurrentLocale())
    }
}

package es.pedrazamiguez.splittrip.features.balance.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.foundation.ChartColors
import es.pedrazamiguez.splittrip.core.designsystem.presentation.extensions.toIconVector
import es.pedrazamiguez.splittrip.core.designsystem.presentation.extensions.toStringRes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatCurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.features.balance.presentation.model.CategorySpendingUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.SubcategorySpendingUiModel
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class CategorySpendingUiMapper(
    private val localeProvider: LocaleProvider,
    private val resourceProvider: ResourceProvider
) {
    fun mapExpenses(expenses: List<Expense>, groupCurrency: String): ImmutableList<CategorySpendingUiModel> {
        val locale = localeProvider.getCurrentLocale()

        val positiveExpenses = expenses.filter { it.groupAmount > 0 }

        val categoryGroups = positiveExpenses.groupBy { it.category }

        val globalTotal = positiveExpenses.sumOf { it.groupAmount }

        val sortedCategories = categoryGroups.entries
            .sortedByDescending { (_, categoryExpenses) -> categoryExpenses.sumOf { it.groupAmount } }

        return sortedCategories.mapIndexed { index, (category, categoryExpenses) ->
            val amount = categoryExpenses.sumOf { it.groupAmount }
            val progress = if (globalTotal > 0L) {
                amount.toFloat() / globalTotal.toFloat()
            } else {
                0f
            }

            val subcategories = categoryExpenses
                .groupBy { it.subcategory }
                .mapValues { (_, subcategoryExpenses) -> subcategoryExpenses.sumOf { it.groupAmount } }
                .entries
                .sortedByDescending { it.value }
                .map { (subcategory, subAmount) ->
                    val percentageOfCategory = if (amount > 0L) {
                        (subAmount.toFloat() / amount.toFloat() * 100f).roundToInt()
                    } else {
                        0
                    }
                    SubcategorySpendingUiModel(
                        subcategoryName = resourceProvider.getString(subcategory.toStringRes()),
                        subcategoryIcon = subcategory.toIconVector(),
                        formattedAmount = formatCurrencyAmount(subAmount, groupCurrency, locale),
                        percentageOfCategory = percentageOfCategory,
                        rawAmountCents = subAmount
                    )
                }
                .toImmutableList()

            CategorySpendingUiModel(
                categoryName = resourceProvider.getString(category.toStringRes()),
                categoryIcon = category.toIconVector(),
                formattedAmount = formatCurrencyAmount(amount, groupCurrency, locale),
                progress = progress,
                color = ChartColors[index % ChartColors.size],
                subcategories = subcategories
            )
        }.toImmutableList()
    }

    fun formatTotalAmount(totalCents: Long, groupCurrency: String): String {
        if (totalCents <= 0) return ""
        return formatCurrencyAmount(totalCents, groupCurrency, localeProvider.getCurrentLocale())
    }
}

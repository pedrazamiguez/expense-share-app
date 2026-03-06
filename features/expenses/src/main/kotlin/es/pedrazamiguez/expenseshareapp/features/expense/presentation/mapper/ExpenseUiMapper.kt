package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatAmount
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatShortDate
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatSourceAmount
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions.toStringRes
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDate

class ExpenseUiMapper(
    private val localeProvider: LocaleProvider, private val resourceProvider: ResourceProvider
) {

    fun map(expense: Expense): ExpenseUiModel {
        val appLocale = localeProvider.getCurrentLocale()
        val (badgeText, isPastDue) = buildScheduledBadge(expense, appLocale)

        return with(expense) {
            ExpenseUiModel(
                id = id,
                title = title,
                formattedAmount = formatAmount(appLocale),
                formattedOriginalAmount = if (sourceCurrency != groupCurrency) {
                    formatSourceAmount(appLocale)
                } else {
                    null
                },
                categoryText = resourceProvider.getString(category.toStringRes()),
                vendorText = vendor,
                paymentMethodText = resourceProvider.getString(paymentMethod.toStringRes()),
                paymentStatusText = resourceProvider.getString(paymentStatus.toStringRes()),
                paidByText = resourceProvider.getString(R.string.paid_by, createdBy),
                dateText = createdAt?.formatShortDate(appLocale) ?: "",
                scheduledBadgeText = badgeText,
                isScheduledPastDue = isPastDue
            )
        }
    }

    fun mapList(expenses: List<Expense>): ImmutableList<ExpenseUiModel> =
        expenses.map { map(it) }.toImmutableList()

    /**
     * Builds the scheduled-payment badge for the expense item.
     *
     * @return Pair of (badgeText, isPastDue).
     *         badgeText is null when the expense is not SCHEDULED.
     */
    private fun buildScheduledBadge(
        expense: Expense,
        locale: java.util.Locale
    ): Pair<String?, Boolean> {
        if (expense.paymentStatus != PaymentStatus.SCHEDULED) return null to false

        val dueDate = expense.dueDate ?: return null to false
        val today = LocalDate.now()
        val dueDateLocal = dueDate.toLocalDate()

        return when {
            dueDateLocal.isEqual(today) -> {
                resourceProvider.getString(R.string.expense_scheduled_due_today) to true
            }
            dueDateLocal.isBefore(today) -> {
                resourceProvider.getString(R.string.expense_scheduled_paid) to true
            }
            else -> {
                val formattedDate = dueDate.formatShortDate(locale)
                resourceProvider.getString(
                    R.string.expense_scheduled_due_on, formattedDate
                ) to false
            }
        }
    }
}

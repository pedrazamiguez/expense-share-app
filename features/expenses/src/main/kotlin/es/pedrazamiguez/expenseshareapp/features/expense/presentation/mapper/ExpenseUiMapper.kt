package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatAmount
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatCurrencyAmount
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatShortDate
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatSourceAmount
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions.toStringRes
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseDateGroupUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseUiModel
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class ExpenseUiMapper(private val localeProvider: LocaleProvider, private val resourceProvider: ResourceProvider) {

    fun map(expense: Expense, memberProfiles: Map<String, User> = emptyMap()): ExpenseUiModel {
        val appLocale = localeProvider.getCurrentLocale()
        val (badgeText, isPastDue) = buildScheduledBadge(expense, appLocale)

        return with(expense) {
            val resolvedName = resolveDisplayName(createdBy, memberProfiles)
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
                paidByText = resourceProvider.getString(R.string.paid_by, resolvedName),
                dateText = createdAt?.formatShortDate(appLocale) ?: "",
                scheduledBadgeText = badgeText,
                isScheduledPastDue = isPastDue,
                hasAddOns = addOns.isNotEmpty()
            )
        }
    }

    fun mapList(
        expenses: List<Expense>,
        memberProfiles: Map<String, User> = emptyMap()
    ): ImmutableList<ExpenseUiModel> = expenses.map { map(it, memberProfiles) }.toImmutableList()

    /**
     * Groups expenses by date (from createdAt) and produces date headers
     * with the formatted daily total in the group's default currency.
     *
     * Expenses are already sorted DESC by createdAt from the DAO.
     * The groupCurrencyCode is taken from the first expense in the list.
     */
    fun mapGroupedByDate(
        expenses: List<Expense>,
        memberProfiles: Map<String, User> = emptyMap()
    ): ImmutableList<ExpenseDateGroupUiModel> {
        if (expenses.isEmpty()) return emptyList<ExpenseDateGroupUiModel>().toImmutableList()

        val appLocale = localeProvider.getCurrentLocale()
        val groupCurrencyCode = expenses.first().groupCurrency

        return expenses
            .groupBy { it.createdAt?.toLocalDate() }
            .map { (date, dayExpenses) ->
                val dateText = date?.let {
                    java.time.LocalDateTime.of(it, java.time.LocalTime.MIDNIGHT)
                        .formatShortDate(appLocale)
                } ?: ""

                val dayTotalCents = dayExpenses.sumOf { it.groupAmount }
                val formattedDayTotal = formatCurrencyAmount(
                    amount = dayTotalCents,
                    currencyCode = groupCurrencyCode,
                    locale = appLocale
                )

                ExpenseDateGroupUiModel(
                    dateText = dateText,
                    formattedDayTotal = formattedDayTotal,
                    expenses = dayExpenses.map { map(it, memberProfiles) }.toImmutableList()
                )
            }
            .toImmutableList()
    }

    /**
     * Builds the scheduled-payment badge for the expense item.
     *
     * @return Pair of (badgeText, isPastDue).
     *         badgeText is null when the expense is not SCHEDULED.
     */
    private fun buildScheduledBadge(expense: Expense, locale: java.util.Locale): Pair<String?, Boolean> {
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
                    R.string.expense_scheduled_due_on,
                    formattedDate
                ) to false
            }
        }
    }

    /**
     * Resolves a userId to a human-readable display name using the
     * fallback hierarchy: displayName → email → raw userId.
     */
    private fun resolveDisplayName(userId: String, memberProfiles: Map<String, User>): String {
        val user = memberProfiles[userId] ?: return userId
        return user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email.takeIf { it.isNotBlank() }
            ?: userId
    }
}

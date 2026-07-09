package es.pedrazamiguez.splittrip.features.expense.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.splittrip.domain.enums.PaymentStatus
import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.features.expense.R
import java.time.LocalDate

data class PaymentBadgeData(
    val text: String,
    val isPassed: Boolean,
    val isToday: Boolean
)

/**
 * Centralises payment badge logic for [PaymentStatus.SCHEDULED] and [PaymentStatus.REFUNDABLE] expenses.
 * It produces identical badge dates for both list and detail views.
 *
 * Returns a [PaymentBadgeData] representing the relative date string and logical states:
 * - null when the expense has no due date or is not in a status that needs a date badge.
 * - [PaymentBadgeData.isPassed] signals that the due date is strictly before today.
 * - [PaymentBadgeData.isToday] signals that the due date is today.
 */
class PaymentStatusBadgeUiMapper(
    private val formattingHelper: FormattingHelper,
    private val resourceProvider: ResourceProvider
) {

    fun buildBadge(expense: Expense): PaymentBadgeData? {
        val dueDate = expense.dueDate
        val status = expense.paymentStatus
        if ((status != PaymentStatus.SCHEDULED && status != PaymentStatus.REFUNDABLE) ||
            dueDate == null
        ) {
            return null
        }

        val today = LocalDate.now()
        val dueDateLocal = dueDate.toLocalDate()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)

        return when {
            dueDateLocal.isBefore(yesterday) ->
                PaymentBadgeData(
                    text = formattingHelper.formatShortDate(dueDate),
                    isPassed = true,
                    isToday = false
                )

            dueDateLocal.isEqual(yesterday) ->
                PaymentBadgeData(
                    text = resourceProvider.getString(R.string.expense_relative_yesterday),
                    isPassed = true,
                    isToday = false
                )

            dueDateLocal.isEqual(today) ->
                PaymentBadgeData(
                    text = resourceProvider.getString(R.string.expense_relative_today),
                    isPassed = false,
                    isToday = true
                )

            dueDateLocal.isEqual(tomorrow) ->
                PaymentBadgeData(
                    text = resourceProvider.getString(R.string.expense_relative_tomorrow),
                    isPassed = false,
                    isToday = false
                )

            else ->
                PaymentBadgeData(
                    text = formattingHelper.formatShortDate(dueDate),
                    isPassed = false,
                    isToday = false
                )
        }
    }
}

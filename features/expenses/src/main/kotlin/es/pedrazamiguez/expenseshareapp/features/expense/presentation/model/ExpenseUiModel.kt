package es.pedrazamiguez.expenseshareapp.features.expense.presentation.model

data class ExpenseUiModel(
    val id: String = "",
    val title: String = "",
    val formattedAmount: String = "",
    val formattedOriginalAmount: String? = null,
    val categoryText: String = "",
    val vendorText: String? = null,
    val paymentMethodText: String = "",
    val paymentStatusText: String = "",
    val paidByText: String = "",
    val dateText: String = "",
    /**
     * Badge text for SCHEDULED expenses. Null when not applicable.
     * - Future: "Due on 15 Mar"
     * - Today:  "Due today"
     * - Past:   "Paid"
     */
    val scheduledBadgeText: String? = null,
    /**
     * True when the scheduled payment's due date has passed or is today,
     * used to pick the check icon (✅) vs clock icon (🕐) in the UI.
     */
    val isScheduledPastDue: Boolean = false,
    /**
     * True when the expense has add-ons (fees, tips, surcharges, discounts).
     * Used to display an indicator badge in the expense list item.
     */
    val hasAddOns: Boolean = false
)

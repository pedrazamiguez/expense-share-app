package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state

/**
 * Defines the wizard steps for the Add Expense flow.
 *
 * The step sequence is:
 *   - amount + currency + title (always)
 *   - exchange rate (conditional: foreign currency selected)
 *   - details: payment method, category, vendor, notes, payment status, due date, receipt
 *   - split among members (conditional: group has >1 member)
 *   - add-ons: fees, tips, discounts, surcharges
 *   - review: read-only summary
 *
 * Conditional steps (EXCHANGE_RATE, SPLIT) are dynamically included/excluded
 * by [applicableSteps] based on the current form state.
 */
enum class AddExpenseStep {
    /** Amount + Currency + Title — always shown. */
    AMOUNT,

    /** Exchange rate + calculated group amount — only when foreign currency selected. */
    EXCHANGE_RATE,

    /** Payment method, category, vendor, notes, payment status, due date, receipt. */
    DETAILS,

    /** Split type + per-member allocation + sub-unit mode — only when >1 member. */
    SPLIT,

    /** Fees, tips, discounts, surcharges. */
    ADD_ONS,

    /** Read-only summary of all entered data — always shown (final confirmation). */
    REVIEW;

    companion object {
        /**
         * Returns the ordered list of steps applicable to the current form state.
         *
         * @param showExchangeRateSection `true` when a foreign currency is selected.
         * @param hasSplit `true` when the group has more than one member.
         */
        fun applicableSteps(
            showExchangeRateSection: Boolean,
            hasSplit: Boolean
        ): List<AddExpenseStep> = buildList {
            add(AMOUNT)
            if (showExchangeRateSection) add(EXCHANGE_RATE)
            add(DETAILS)
            if (hasSplit) add(SPLIT)
            add(ADD_ONS)
            add(REVIEW)
        }
    }
}

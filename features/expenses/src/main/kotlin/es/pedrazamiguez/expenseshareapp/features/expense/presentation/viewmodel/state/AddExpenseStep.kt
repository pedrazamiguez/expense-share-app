package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state

/**
 * Defines the wizard steps for the Add Expense flow.
 *
 * The step sequence is:
 *   1. title (always)
 *   2. payment method (always — determines exchange-rate lock behaviour)
 *   3. funding source (always — "Group Pocket" or "My Money")
 *   4. amount + currency (always)
 *   5. exchange rate (conditional: foreign currency selected)
 *   6. split among members (conditional: group has >1 member)
 *   7. category (always)
 *   8. vendor + notes (always)
 *   9. payment status / scheduling (always)
 *  10. receipt (always)
 *  11. add-ons: fees, tips, discounts, surcharges (always)
 *  12. review: read-only summary (always)
 *
 * Conditional steps (EXCHANGE_RATE, SPLIT) are dynamically included/excluded
 * by [applicableSteps] based on the current form state.
 */
enum class AddExpenseStep {
    /** Expense title — always shown. */
    TITLE,

    /** Payment method selection — always shown (affects exchange-rate lock). */
    PAYMENT_METHOD,

    /** Funding source — always shown ("Group Pocket" or "My Money"). */
    FUNDING_SOURCE,

    /** Amount + Currency — always shown. */
    AMOUNT,

    /** Exchange rate + calculated group amount — only when foreign currency selected. */
    EXCHANGE_RATE,

    /** Split type + per-member allocation + sub-unit mode — only when >1 member. */
    SPLIT,

    /** Category selection. */
    CATEGORY,

    /** Vendor and optional notes. */
    VENDOR_NOTES,

    /** Payment status + conditional due date. */
    PAYMENT_STATUS,

    /** Receipt image attachment. */
    RECEIPT,

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
            add(TITLE)
            add(PAYMENT_METHOD)
            add(FUNDING_SOURCE)
            add(AMOUNT)
            if (showExchangeRateSection) add(EXCHANGE_RATE)
            if (hasSplit) add(SPLIT)
            add(CATEGORY)
            add(VENDOR_NOTES)
            add(PAYMENT_STATUS)
            add(RECEIPT)
            add(ADD_ONS)
            add(REVIEW)
        }
    }
}

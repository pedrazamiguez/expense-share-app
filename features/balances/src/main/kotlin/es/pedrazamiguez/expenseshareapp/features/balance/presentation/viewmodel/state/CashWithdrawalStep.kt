package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state

/**
 * Defines the wizard steps for the cash withdrawal flow.
 *
 * The step sequence is intentionally symmetric:
 *   - withdrawal amount → withdrawal conversion (if foreign)
 *   - scope → details (with fee toggle)
 *   - fee amount → fee conversion (if fee currency is foreign)
 *   - review
 *
 * Conditional steps (EXCHANGE_RATE, ATM_FEE, FEE_EXCHANGE_RATE) are dynamically
 * included/excluded by [applicableSteps] based on the current form state.
 */
enum class CashWithdrawalStep {
    /** Amount input + currency selector — always shown. */
    AMOUNT,

    /** Exchange rate + deducted amount in group currency — only when foreign currency selected. */
    EXCHANGE_RATE,

    /** Who is withdrawing: group / sub-unit / personal — always shown. */
    SCOPE,

    /** Title, notes, and ATM fee opt-in toggle — always shown. */
    DETAILS,

    /** ATM fee amount + fee currency — only when fee is toggled on. */
    ATM_FEE,

    /** ATM fee exchange rate + converted amount — only when fee currency ≠ group currency. */
    FEE_EXCHANGE_RATE,

    /** Read-only summary of all entered data — always shown (final confirmation). */
    REVIEW;

    companion object {
        /**
         * Returns the ordered list of steps applicable to the current form state.
         *
         * @param showExchangeRateSection `true` when a foreign withdrawal currency is selected.
         * @param hasFee                  `true` when the ATM fee toggle is on.
         * @param showFeeExchangeRateSection `true` when fee currency ≠ group currency.
         */
        fun applicableSteps(
            showExchangeRateSection: Boolean,
            hasFee: Boolean,
            showFeeExchangeRateSection: Boolean
        ): List<CashWithdrawalStep> = buildList {
            add(AMOUNT)
            if (showExchangeRateSection) add(EXCHANGE_RATE)
            add(SCOPE)
            add(DETAILS)
            if (hasFee) {
                add(ATM_FEE)
                if (showFeeExchangeRateSection) add(FEE_EXCHANGE_RATE)
            }
            add(REVIEW)
        }
    }
}

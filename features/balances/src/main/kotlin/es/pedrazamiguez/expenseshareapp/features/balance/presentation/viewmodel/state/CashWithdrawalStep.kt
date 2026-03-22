package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state

/**
 * Defines the wizard steps for the cash withdrawal flow.
 *
 * Steps are ordered and may be conditionally applicable based on the current
 * [AddCashWithdrawalUiState]. The wizard dynamically computes the active step
 * list so that conditional steps (exchange rate, ATM fee) only appear when relevant.
 */
enum class CashWithdrawalStep {
    /** Amount input + currency selector — always shown. */
    AMOUNT,

    /** Exchange rate + deducted amount in group currency — only when foreign currency selected. */
    EXCHANGE_RATE,

    /** ATM fee toggle, amount, currency, fee exchange rate — optional, user-activated. */
    ATM_FEE,

    /** Scope selector, title, notes — always shown. */
    DETAILS,

    /** Read-only summary of all entered data — always shown (final confirmation). */
    REVIEW;

    companion object {
        /**
         * Computes the ordered list of applicable steps for the current form state.
         *
         * Exchange Rate step only appears when a foreign currency is selected.
         * ATM Fee step only appears when the user has toggled the fee on.
         */
        fun applicableSteps(
            showExchangeRateSection: Boolean,
            hasFee: Boolean
        ): List<CashWithdrawalStep> = buildList {
            add(AMOUNT)
            if (showExchangeRateSection) add(EXCHANGE_RATE)
            if (hasFee) add(ATM_FEE)
            add(DETAILS)
            add(REVIEW)
        }
    }
}

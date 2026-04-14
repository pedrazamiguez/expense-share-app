package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state

import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardStep

/**
 * Defines the wizard steps for the Add Expense flow.
 *
 * The step sequence is:
 *   1. title (always)
 *   2. payment method (always — determines exchange-rate lock behaviour)
 *   3. funding source (always — "Group Pocket" or "My Money")
 *   4. contribution scope (conditional: funding source = "My Money")
 *   5. amount + currency (always)
 *   6. exchange rate (conditional: foreign currency selected)
 *   7. split among members (conditional: group has >1 member)
 *   8. category (always, optional — may be skipped)
 *   9. vendor + notes (always, optional — may be skipped)
 *  10. payment status / scheduling (always)
 *  11. receipt (always, optional — may be skipped)
 *  12. add-ons: fees, tips, discounts, surcharges (always, optional — may be skipped)
 *  13. review: read-only summary (always)
 *
 * Conditional steps (CONTRIBUTION_SCOPE, EXCHANGE_RATE, SPLIT) are dynamically
 * included/excluded by [applicableSteps] based on the current form state.
 *
 * @property isOptional When `true` the step can be skipped via the "Skip to Review"
 *                      link in [WizardStepIndicator]. Optional steps show a dashed
 *                      border in the indicator until completed.
 * @property isReview When `true` this is the final read-only review/confirmation step.
 */
enum class AddExpenseStep(
    override val isOptional: Boolean = false,
    override val isReview: Boolean = false
) : WizardStep {
    /** Expense title — always shown. */
    TITLE,

    /** Payment method selection — always shown (affects exchange-rate lock). */
    PAYMENT_METHOD,

    /** Funding source — always shown ("Group Pocket" or "My Money"). */
    FUNDING_SOURCE,

    /** Contribution scope — only when funding source is "My Money" (USER). */
    CONTRIBUTION_SCOPE,

    /** Amount + Currency — always shown. */
    AMOUNT,

    /** Exchange rate + calculated group amount — only when foreign currency selected. */
    EXCHANGE_RATE,

    /** Split type + per-member allocation + sub-unit mode — only when >1 member. */
    SPLIT,

    /** Category selection — optional, may be skipped. */
    CATEGORY(isOptional = true),

    /** Vendor and optional notes — optional, may be skipped. */
    VENDOR_NOTES(isOptional = true),

    /** Payment status + conditional due date. */
    PAYMENT_STATUS,

    /** Receipt image attachment — optional, may be skipped. */
    RECEIPT(isOptional = true),

    /** Fees, tips, discounts, surcharges — optional, may be skipped. */
    ADD_ONS(isOptional = true),

    /** Read-only summary of all entered data — always shown (final confirmation). */
    REVIEW(isReview = true);

    companion object {
        /**
         * Returns the ordered list of steps applicable to the current form state.
         *
         * @param showContributionScopeStep `true` when funding source is "My Money" (USER).
         * @param showExchangeRateSection `true` when a foreign currency is selected.
         * @param hasSplit `true` when the group has more than one member.
         */
        fun applicableSteps(
            showContributionScopeStep: Boolean,
            showExchangeRateSection: Boolean,
            hasSplit: Boolean
        ): List<AddExpenseStep> = buildList {
            add(TITLE)
            add(PAYMENT_METHOD)
            add(FUNDING_SOURCE)
            if (showContributionScopeStep) add(CONTRIBUTION_SCOPE)
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

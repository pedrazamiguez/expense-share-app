package es.pedrazamiguez.expenseshareapp.domain.model

/**
 * Result of a cash exchange rate preview computation.
 *
 * Distinguishes between a successfully computed preview, no available
 * withdrawals, and insufficient cash — allowing the UI to show the
 * appropriate feedback for each case.
 */
sealed interface CashRatePreviewResult {

    /**
     * A preview was successfully computed from available ATM withdrawals.
     */
    data class Available(val preview: CashRatePreview) : CashRatePreviewResult

    /**
     * No withdrawals exist for the requested currency in the given group.
     */
    data object NoWithdrawals : CashRatePreviewResult

    /**
     * Withdrawals exist but the total remaining cash is insufficient
     * to cover the requested source amount.
     */
    data object InsufficientCash : CashRatePreviewResult
}

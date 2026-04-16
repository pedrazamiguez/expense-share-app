package es.pedrazamiguez.splittrip.features.expense.presentation.model

/**
 * UI model representing a single ATM withdrawal tranche in the "Funded from" breakdown
 * shown when paying with CASH: on the Exchange Rate step when that step is present,
 * and on the Amount step for same-currency CASH flows where the Exchange Rate step
 * is skipped.
 *
 * @param withdrawalLabel       Display label for the withdrawal (title if available,
 *                              fallback: "ATM — {date}").
 * @param formattedAmountConsumed Amount consumed from this withdrawal, formatted with
 *                              currency symbol (e.g., "฿ 5,000").
 * @param formattedRemainingAfter Remaining balance after this simulated consumption,
 *                              formatted with currency symbol (e.g., "฿ 0 remaining").
 * @param formattedRate         Exchange rate of this withdrawal formatted for display
 *                              (e.g., "37.04").
 */
data class CashTranchePreviewUiModel(
    val withdrawalLabel: String,
    val formattedAmountConsumed: String,
    val formattedRemainingAfter: String,
    val formattedRate: String
)

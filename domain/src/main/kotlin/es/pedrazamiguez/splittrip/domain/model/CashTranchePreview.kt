package es.pedrazamiguez.splittrip.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * A lightweight read model representing a single withdrawal tranche consumed by a simulated
 * FIFO cash expense preview.
 *
 * Populated by [es.pedrazamiguez.splittrip.domain.usecase.expense.PreviewCashExchangeRateUseCase]
 * and surfaced in [CashRatePreview] so the UI can show "Funded from" breakdown.
 *
 * @param withdrawalId          ID of the [CashWithdrawal] that contributes this tranche.
 * @param withdrawalTitle       User-provided label of the withdrawal (may be null/blank).
 * @param withdrawalDate        Timestamp when the withdrawal was recorded (may be null).
 * @param amountConsumedCents   How much of this withdrawal funds the expense (in minor units).
 * @param remainingAfterCents   Remaining balance of the withdrawal after this simulated consumption.
 * @param withdrawalRate        Exchange rate of this specific withdrawal (SourceCurrency/GroupCurrency).
 */
data class CashTranchePreview(
    val withdrawalId: String,
    val withdrawalTitle: String?,
    val withdrawalDate: LocalDateTime?,
    val amountConsumedCents: Long,
    val remainingAfterCents: Long,
    val withdrawalRate: BigDecimal
)

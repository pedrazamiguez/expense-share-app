package es.pedrazamiguez.splittrip.domain.usecase.expense

import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.CashRatePreview
import es.pedrazamiguez.splittrip.domain.model.CashRatePreviewResult
import es.pedrazamiguez.splittrip.domain.model.CashTranchePreview
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.service.ExchangeRateCalculationService
import es.pedrazamiguez.splittrip.domain.service.ExpenseCalculatorService
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Computes a preview of the FIFO-blended exchange rate for a CASH expense.
 *
 * Used by the UI to show the correct ATM-derived exchange rate in the exchange rate
 * section when the payment method is CASH, instead of fetching from the Open Exchange
 * Rates API.
 *
 * The pool queried is determined by [payerType] and [payerId]:
 * - **GROUP (default):** all GROUP-scoped withdrawals.
 * - **USER:** the user's personal (USER-scoped) withdrawals for [payerId] + GROUP fallback.
 * - **SUBUNIT:** the subunit's (SUBUNIT-scoped) withdrawals for [payerId] + GROUP fallback.
 *
 * - When [sourceAmountCents] > 0 and sufficient cash exists: runs a simulated FIFO
 *   to compute the blended display rate and equivalent group amount.
 * - When [sourceAmountCents] <= 0: returns a weighted-average display rate from all
 *   available withdrawals (best-effort preview before the user enters an amount).
 *
 * Returns [CashRatePreviewResult.NoWithdrawals] when no withdrawals exist.
 * Returns [CashRatePreviewResult.InsufficientCash] when the available cash cannot
 * cover [sourceAmountCents].
 *
 * When the FIFO result consumes only a single withdrawal tranche, the withdrawal's
 * stored [exchangeRate][es.pedrazamiguez.splittrip.domain.model.CashWithdrawal.exchangeRate]
 * is used directly as the display rate to avoid integer-cent rounding artefacts.
 */
class PreviewCashExchangeRateUseCase(
    private val cashWithdrawalRepository: CashWithdrawalRepository,
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val exchangeRateCalculationService: ExchangeRateCalculationService
) {
    companion object {
        private const val RATE_PRECISION = 6
    }

    suspend operator fun invoke(
        groupId: String,
        sourceCurrency: String,
        sourceAmountCents: Long,
        payerType: PayerType = PayerType.GROUP,
        payerId: String? = null
    ): CashRatePreviewResult {
        val withdrawals = cashWithdrawalRepository.getAvailableWithdrawals(
            groupId,
            sourceCurrency,
            payerType,
            payerId
        )
        if (withdrawals.isEmpty()) return CashRatePreviewResult.NoWithdrawals
        if (sourceAmountCents <= 0) return previewWithoutAmount(withdrawals)
        return previewWithAmount(sourceAmountCents, withdrawals)
    }

    /**
     * No amount entered yet — return weighted-average display rate from all withdrawals.
     * For a single withdrawal, use its stored exchange rate directly to avoid
     * integer-cent rounding artefacts.
     */
    private fun previewWithoutAmount(
        withdrawals: List<CashWithdrawal>
    ): CashRatePreviewResult {
        if (withdrawals.size == 1) {
            val rate = withdrawals.first().exchangeRate
            if (rate > BigDecimal.ZERO) {
                return CashRatePreviewResult.Available(
                    CashRatePreview(displayRate = rate)
                )
            }
        }

        val totalWithdrawn = withdrawals.sumOf { it.amountWithdrawn }
        val totalDeducted = withdrawals.sumOf { it.deductedBaseAmount }
        if (totalWithdrawn <= 0 || totalDeducted <= 0) {
            return CashRatePreviewResult.NoWithdrawals
        }

        val weightedDisplayRate = BigDecimal(totalWithdrawn)
            .divide(BigDecimal(totalDeducted), RATE_PRECISION, RoundingMode.HALF_UP)
        return CashRatePreviewResult.Available(
            CashRatePreview(displayRate = weightedDisplayRate)
        )
    }

    /**
     * Amount entered — simulate FIFO to get the blended group amount and display rate.
     */
    private fun previewWithAmount(
        sourceAmountCents: Long,
        withdrawals: List<CashWithdrawal>
    ): CashRatePreviewResult {
        if (expenseCalculatorService.hasInsufficientCash(sourceAmountCents, withdrawals)) {
            return CashRatePreviewResult.InsufficientCash
        }

        // Simulate FIFO to get the blended group amount
        val fifoResult = expenseCalculatorService.calculateFifoCashAmount(
            amountToCover = sourceAmountCents,
            availableWithdrawals = withdrawals
        )

        // When the entire expense falls within a single withdrawal tranche,
        // use that withdrawal's stored exchange rate directly. This avoids the
        // display rate fluctuating due to integer-cent rounding in the FIFO
        // group amount calculation.
        val displayRate = if (fifoResult.tranches.size == 1) {
            val tranche = fifoResult.tranches.first()
            val withdrawal = withdrawals.find { it.id == tranche.withdrawalId }
            val storedRate = withdrawal?.exchangeRate ?: BigDecimal.ZERO
            if (storedRate > BigDecimal.ZERO) {
                storedRate
            } else {
                exchangeRateCalculationService.calculateBlendedDisplayRate(
                    sourceAmountCents = sourceAmountCents,
                    groupAmountCents = fifoResult.groupAmountCents
                )
            }
        } else {
            exchangeRateCalculationService.calculateBlendedDisplayRate(
                sourceAmountCents = sourceAmountCents,
                groupAmountCents = fifoResult.groupAmountCents
            )
        }

        val tranchePreviews = fifoResult.tranches.mapNotNull { tranche ->
            val withdrawal = withdrawals.find { it.id == tranche.withdrawalId } ?: return@mapNotNull null
            CashTranchePreview(
                withdrawalId = tranche.withdrawalId,
                withdrawalTitle = withdrawal.title,
                withdrawalDate = withdrawal.createdAt,
                amountConsumedCents = tranche.amountConsumed,
                remainingAfterCents = withdrawal.remainingAmount - tranche.amountConsumed,
                withdrawalRate = withdrawal.exchangeRate
            )
        }

        return CashRatePreviewResult.Available(
            CashRatePreview(
                displayRate = displayRate,
                groupAmountCents = fifoResult.groupAmountCents,
                tranches = tranchePreviews
            )
        )
    }
}

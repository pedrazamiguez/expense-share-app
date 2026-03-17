package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.model.CashRatePreview
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Computes a preview of the FIFO-blended exchange rate for a CASH expense.
 *
 * Used by the UI to show the correct ATM-derived exchange rate in the exchange rate
 * section when the payment method is CASH, instead of fetching from the Open Exchange
 * Rates API.
 *
 * - When [sourceAmountCents] > 0 and sufficient cash exists: runs a simulated FIFO
 *   to compute the blended display rate and equivalent group amount.
 * - When [sourceAmountCents] <= 0: returns a weighted-average display rate from all
 *   available withdrawals (best-effort preview before the user enters an amount).
 *
 * Returns `null` in the following cases:
 * - No withdrawals exist for the requested currency.
 * - The available cash is insufficient to cover [sourceAmountCents].
 * - All available withdrawals have non-positive `amountWithdrawn` or `deductedBaseAmount`
 *   (degenerate data preventing a meaningful rate calculation).
 */
class PreviewCashExchangeRateUseCase(
    private val cashWithdrawalRepository: CashWithdrawalRepository,
    private val expenseCalculatorService: ExpenseCalculatorService
) {
    companion object {
        private const val RATE_PRECISION = 6
    }

    suspend operator fun invoke(
        groupId: String,
        sourceCurrency: String,
        sourceAmountCents: Long
    ): CashRatePreview? {
        val withdrawals = cashWithdrawalRepository.getAvailableWithdrawals(groupId, sourceCurrency)
        if (withdrawals.isEmpty()) return null

        if (sourceAmountCents <= 0) {
            // No amount entered yet — return weighted-average display rate from all withdrawals
            val totalWithdrawn = withdrawals.sumOf { it.amountWithdrawn }
            val totalDeducted = withdrawals.sumOf { it.deductedBaseAmount }
            if (totalWithdrawn <= 0 || totalDeducted <= 0) return null

            val weightedDisplayRate = BigDecimal(totalWithdrawn)
                .divide(BigDecimal(totalDeducted), RATE_PRECISION, RoundingMode.HALF_UP)
            return CashRatePreview(displayRate = weightedDisplayRate)
        }

        // Check if there's enough cash
        if (expenseCalculatorService.hasInsufficientCash(sourceAmountCents, withdrawals)) {
            return null
        }

        // Simulate FIFO to get the blended group amount
        val fifoResult = expenseCalculatorService.calculateFifoCashAmount(
            amountToCover = sourceAmountCents,
            availableWithdrawals = withdrawals
        )

        val blendedDisplayRate = expenseCalculatorService.calculateBlendedDisplayRate(
            sourceAmountCents = sourceAmountCents,
            groupAmountCents = fifoResult.groupAmountCents
        )

        return CashRatePreview(
            displayRate = blendedDisplayRate,
            groupAmountCents = fifoResult.groupAmountCents
        )
    }
}


package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.service.CashWithdrawalValidationService

/**
 * Use case for logging an ATM cash withdrawal.
 *
 * Validates the withdrawal data, saves it via the repository,
 * and optionally logs an ATM fee as a standard expense (future enhancement).
 */
class AddCashWithdrawalUseCase(
    private val cashWithdrawalRepository: CashWithdrawalRepository,
    private val validationService: CashWithdrawalValidationService
) {

    suspend operator fun invoke(
        groupId: String?,
        withdrawal: CashWithdrawal
    ): Result<Unit> = runCatching {

        require(!groupId.isNullOrBlank()) { "Group ID cannot be null or blank" }

        val amountResult = validationService.validateAmountWithdrawn(withdrawal.amountWithdrawn)
        check(amountResult is CashWithdrawalValidationService.ValidationResult.Valid) {
            "Amount withdrawn must be greater than zero"
        }

        val deductedResult = validationService.validateDeductedBaseAmount(withdrawal.deductedBaseAmount)
        check(deductedResult is CashWithdrawalValidationService.ValidationResult.Valid) {
            "Deducted base amount must be greater than zero"
        }

        val currencyResult = validationService.validateCurrency(withdrawal.currency)
        check(currencyResult is CashWithdrawalValidationService.ValidationResult.Valid) {
            "Currency is required"
        }

        val rateResult = validationService.validateExchangeRate(withdrawal.exchangeRate)
        check(rateResult is CashWithdrawalValidationService.ValidationResult.Valid) {
            "Exchange rate must be greater than zero"
        }

        cashWithdrawalRepository.addWithdrawal(groupId, withdrawal)
    }
}


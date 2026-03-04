package es.pedrazamiguez.expenseshareapp.domain.service

/**
 * Domain service responsible for validating cash withdrawal data.
 * Validation logic belongs in services, NOT in UseCases or ViewModels.
 */
class CashWithdrawalValidationService {

    fun validateAmountWithdrawn(amount: Long): ValidationResult {
        return when {
            amount <= 0 -> ValidationResult.Invalid(ValidationError.AMOUNT_MUST_BE_POSITIVE)
            else -> ValidationResult.Valid
        }
    }

    fun validateDeductedBaseAmount(amount: Long): ValidationResult {
        return when {
            amount <= 0 -> ValidationResult.Invalid(ValidationError.DEDUCTED_AMOUNT_MUST_BE_POSITIVE)
            else -> ValidationResult.Valid
        }
    }

    fun validateCurrency(currency: String): ValidationResult {
        return when {
            currency.isBlank() -> ValidationResult.Invalid(ValidationError.CURRENCY_REQUIRED)
            else -> ValidationResult.Valid
        }
    }

    fun validateExchangeRate(rate: Double): ValidationResult {
        return when {
            rate <= 0.0 -> ValidationResult.Invalid(ValidationError.EXCHANGE_RATE_MUST_BE_POSITIVE)
            else -> ValidationResult.Valid
        }
    }

    sealed interface ValidationResult {
        data object Valid : ValidationResult
        data class Invalid(val error: ValidationError) : ValidationResult
    }

    enum class ValidationError {
        AMOUNT_MUST_BE_POSITIVE,
        DEDUCTED_AMOUNT_MUST_BE_POSITIVE,
        CURRENCY_REQUIRED,
        EXCHANGE_RATE_MUST_BE_POSITIVE
    }
}


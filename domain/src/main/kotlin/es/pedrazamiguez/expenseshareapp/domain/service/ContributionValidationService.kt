package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.model.Contribution

/**
 * Domain service responsible for validating contribution data.
 * Validation logic belongs in services, NOT in UseCases or ViewModels.
 */
class ContributionValidationService {

    fun validateAmount(amount: Long): ValidationResult = when {
        amount <= 0 -> ValidationResult.Invalid(ValidationError.AMOUNT_MUST_BE_POSITIVE)
        else -> ValidationResult.Valid
    }

    fun validate(contribution: Contribution): ValidationResult = validateAmount(contribution.amount)

    sealed interface ValidationResult {
        data object Valid : ValidationResult
        data class Invalid(val error: ValidationError) : ValidationResult
    }

    enum class ValidationError {
        AMOUNT_MUST_BE_POSITIVE
    }
}

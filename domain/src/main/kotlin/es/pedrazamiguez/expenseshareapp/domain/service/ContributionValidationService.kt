package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit

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

    /**
     * Validates the sub-unit assignment for a contribution.
     *
     * @param subunitId The sub-unit to validate against.
     * @param userId The user making the contribution.
     * @param groupSubunits All sub-units in the group.
     * @return [ValidationResult.Valid] if no sub-unit is specified or if the sub-unit
     *         exists and the user is a member of it.
     */
    fun validateSubunit(
        subunitId: String?,
        userId: String,
        groupSubunits: List<Subunit>
    ): ValidationResult {
        if (subunitId == null) return ValidationResult.Valid

        val subunit = groupSubunits.find { it.id == subunitId }
            ?: return ValidationResult.Invalid(ValidationError.SUBUNIT_NOT_FOUND)

        if (userId !in subunit.memberIds) {
            return ValidationResult.Invalid(ValidationError.USER_NOT_IN_SUBUNIT)
        }

        return ValidationResult.Valid
    }

    sealed interface ValidationResult {
        data object Valid : ValidationResult
        data class Invalid(val error: ValidationError) : ValidationResult
    }

    enum class ValidationError {
        AMOUNT_MUST_BE_POSITIVE,
        SUBUNIT_NOT_FOUND,
        USER_NOT_IN_SUBUNIT
    }
}

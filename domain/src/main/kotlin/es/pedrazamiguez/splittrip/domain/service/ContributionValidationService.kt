package es.pedrazamiguez.splittrip.domain.service

import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.Subunit

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
     * Validates the contribution scope and subunit assignment.
     *
     * - When [contributionScope] is [PayerType.SUBUNIT], a valid [subunitId] must be provided,
     *   the subunit must exist in the group, and [userId] must be a member of it.
     * - When [contributionScope] is [PayerType.GROUP] or [PayerType.USER], [subunitId] must be null.
     *
     * @param contributionScope The intended scope of the contribution.
     * @param subunitId The subunit ID (only for SUBUNIT scope).
     * @param userId The user making the contribution.
     * @param groupSubunits All subunits in the group.
     */
    fun validateContributionScope(
        contributionScope: PayerType,
        subunitId: String?,
        userId: String,
        groupSubunits: List<Subunit>
    ): ValidationResult = when (contributionScope) {
        PayerType.SUBUNIT -> validateSubunitScope(subunitId, userId, groupSubunits)
        else -> if (subunitId != null) {
            ValidationResult.Invalid(ValidationError.INVALID_SUBUNIT_FOR_SCOPE)
        } else {
            ValidationResult.Valid
        }
    }

    private fun validateSubunitScope(
        subunitId: String?,
        userId: String,
        groupSubunits: List<Subunit>
    ): ValidationResult {
        if (subunitId.isNullOrBlank()) {
            return ValidationResult.Invalid(ValidationError.SUBUNIT_REQUIRED)
        }
        val subunit = groupSubunits.find { it.id == subunitId }
            ?: return ValidationResult.Invalid(ValidationError.SUBUNIT_NOT_FOUND)
        return if (userId !in subunit.memberIds) {
            ValidationResult.Invalid(ValidationError.USER_NOT_IN_SUBUNIT)
        } else {
            ValidationResult.Valid
        }
    }

    /**
     * Validates the subunit assignment for a contribution.
     *
     * @param subunitId The subunit to validate against.
     * @param userId The user making the contribution.
     * @param groupSubunits All subunits in the group.
     * @return [ValidationResult.Valid] if no subunit is specified or if the subunit
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
        SUBUNIT_REQUIRED,
        INVALID_SUBUNIT_FOR_SCOPE,
        USER_NOT_IN_SUBUNIT
    }
}

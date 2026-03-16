package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import java.math.BigDecimal

/**
 * Domain service responsible for validating cash withdrawal data.
 * Validation logic belongs in services, NOT in UseCases or ViewModels.
 */
class CashWithdrawalValidationService {

    fun validateAmountWithdrawn(amount: Long): ValidationResult = when {
        amount <= 0 -> ValidationResult.Invalid(ValidationError.AMOUNT_MUST_BE_POSITIVE)
        else -> ValidationResult.Valid
    }

    fun validateDeductedBaseAmount(amount: Long): ValidationResult = when {
        amount <= 0 -> ValidationResult.Invalid(ValidationError.DEDUCTED_AMOUNT_MUST_BE_POSITIVE)
        else -> ValidationResult.Valid
    }

    fun validateCurrency(currency: String): ValidationResult = when {
        currency.isBlank() -> ValidationResult.Invalid(ValidationError.CURRENCY_REQUIRED)
        else -> ValidationResult.Valid
    }

    fun validateExchangeRate(rate: BigDecimal): ValidationResult = when {
        rate.compareTo(
            BigDecimal.ZERO
        ) <= 0 -> ValidationResult.Invalid(ValidationError.EXCHANGE_RATE_MUST_BE_POSITIVE)
        else -> ValidationResult.Valid
    }

    /**
     * Validates the withdrawal scope and sub-unit assignment.
     *
     * - When [withdrawalScope] is [PayerType.SUBUNIT], a valid [subunitId] must be provided,
     *   the sub-unit must exist in the group, and [userId] must be a member of it.
     * - When [withdrawalScope] is [PayerType.GROUP] or [PayerType.USER], [subunitId] must be null.
     *
     * @param withdrawalScope The intended scope of the withdrawal.
     * @param subunitId The sub-unit ID (only for SUBUNIT scope).
     * @param userId The user performing the withdrawal.
     * @param groupSubunits All sub-units in the group.
     */
    fun validateWithdrawalScope(
        withdrawalScope: PayerType,
        subunitId: String?,
        userId: String,
        groupSubunits: List<Subunit>
    ): ValidationResult {
        return when (withdrawalScope) {
            PayerType.SUBUNIT -> {
                if (subunitId.isNullOrBlank()) {
                    return ValidationResult.Invalid(ValidationError.SUBUNIT_REQUIRED)
                }
                val subunit = groupSubunits.find { it.id == subunitId }
                    ?: return ValidationResult.Invalid(ValidationError.SUBUNIT_NOT_FOUND)
                if (userId !in subunit.memberIds) {
                    return ValidationResult.Invalid(ValidationError.USER_NOT_IN_SUBUNIT)
                }
                ValidationResult.Valid
            }

            else -> {
                if (subunitId != null) {
                    return ValidationResult.Invalid(ValidationError.INVALID_SUBUNIT_FOR_SCOPE)
                }
                ValidationResult.Valid
            }
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
        EXCHANGE_RATE_MUST_BE_POSITIVE,
        SUBUNIT_REQUIRED,
        SUBUNIT_NOT_FOUND,
        USER_NOT_IN_SUBUNIT,
        INVALID_SUBUNIT_FOR_SCOPE
    }
}

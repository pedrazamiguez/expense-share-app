package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.model.ValidationResult

class ExpenseValidationService {

    fun validateTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult.Invalid("Title cannot be empty")
            else -> ValidationResult.Valid
        }
    }

    fun validateAmount(amountString: String): ValidationResult {
        val result = CurrencyConverter.parseToCents(amountString)
        return if (result.isSuccess) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(
                result.exceptionOrNull()?.message ?: "Invalid amount"
            )
        }
    }

    /**
     * Validates that the number of users is positive before performing
     * division-based operations (e.g., equal split).
     *
     * @param count The number of users to split an expense among.
     * @return [ValidationResult.Valid] if count > 0, otherwise [ValidationResult.Invalid].
     */
    fun validateUserCount(count: Int): ValidationResult {
        return when {
            count <= 0 -> ValidationResult.Invalid("User count must be greater than zero")
            else -> ValidationResult.Valid
        }
    }
}


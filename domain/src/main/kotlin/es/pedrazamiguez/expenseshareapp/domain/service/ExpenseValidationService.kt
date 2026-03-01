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
}


package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import es.pedrazamiguez.expenseshareapp.domain.model.ValidationResult
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory

class ExpenseValidationService(
    private val splitCalculatorFactory: ExpenseSplitCalculatorFactory
) {

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

    /**
     * Validates expense splits by delegating to the appropriate strategy's validation.
     *
     * @param splitType       The split strategy being used.
     * @param splits          The user-provided split data.
     * @param totalAmountCents The total expense amount in cents.
     * @param participantIds  The user IDs of all active (non-excluded) participants.
     * @return [ValidationResult.Valid] if the splits are valid, otherwise [ValidationResult.Invalid].
     */
    fun validateSplits(
        splitType: SplitType,
        splits: List<ExpenseSplit>,
        totalAmountCents: Long,
        participantIds: List<String>
    ): ValidationResult {
        return try {
            val calculator = splitCalculatorFactory.create(splitType)
            // calculateShares calls validate() internally via Template Method
            calculator.calculateShares(totalAmountCents, participantIds, splits)
            ValidationResult.Valid
        } catch (e: Exception) {
            ValidationResult.Invalid(e.message ?: "Invalid split configuration")
        }
    }
}


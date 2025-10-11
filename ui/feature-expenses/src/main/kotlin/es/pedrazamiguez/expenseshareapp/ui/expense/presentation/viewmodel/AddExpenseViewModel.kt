package es.pedrazamiguez.expenseshareapp.ui.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.ui.extension.hardcoded
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.ui.expense.presentation.model.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.ui.expense.presentation.model.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.ui.expense.presentation.model.AddExpenseUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale
import kotlin.math.roundToLong

class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<AddExpenseUiAction>()
    val actions: SharedFlow<AddExpenseUiAction> = _actions.asSharedFlow()

    fun onEvent(
        event: AddExpenseUiEvent,
        onAddExpenseSuccess: () -> Unit
    ) {
        when (event) {
            is AddExpenseUiEvent.TitleChanged -> _uiState.value = _uiState.value.copy(expenseTitle = event.title)
            is AddExpenseUiEvent.AmountChanged -> _uiState.value = _uiState.value.copy(expenseAmount = event.amount)
            AddExpenseUiEvent.SubmitAddExpense -> {
                if (_uiState.value.expenseTitle.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isTitleValid = false,
                        error = "Expense title cannot be empty".hardcoded
                    )
                    return
                }

                val amountInCents = convertAmountToCents(_uiState.value.expenseAmount)
                if (amountInCents == null || amountInCents <= 0) {
                    _uiState.value = _uiState.value.copy(
                        isAmountValid = false,
                        error = "Please enter a valid amount greater than zero".hardcoded
                    )
                    return
                }
                addExpense(onAddExpenseSuccess)
            }
        }
    }

    private fun addExpense(onAddExpenseSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            runCatching {
                val amountInCents = convertAmountToCents(_uiState.value.expenseAmount) ?: throw IllegalArgumentException("Invalid amount")

                val expenseToAdd = Expense(
                    title = _uiState.value.expenseTitle,
                    amountCents = amountInCents,
                )

                addExpenseUseCase(
                    "",
                    expenseToAdd
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onAddExpenseSuccess()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
                _actions.emit(AddExpenseUiAction.ShowError(e.message ?: "Expense addition failed".hardcoded))
            }
        }
    }

    /**
     * Converts a string amount to cents as a Long, handling locale-specific decimal separators.
     * Examples:
     * "1" -> 100
     * "12.56" (English) -> 1256
     * "12,56" (Spanish/German/etc.) -> 1256
     * "1427.158" -> 142716 (rounded from 142715.8)
     *
     * @param amountString The string representation of the amount
     * @return The amount in cents as Long, or null if the input is invalid
     */
    private fun convertAmountToCents(amountString: String): Long? {
        if (amountString.isBlank()) return null

        return try {
            // Try to parse using the current locale first
            val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
            val amountDouble = try {
                numberFormat.parse(amountString)?.toDouble()
            } catch (e: ParseException) {
                // If locale parsing fails, try with dot separator as fallback
                amountString.toDoubleOrNull()
            }

            amountDouble?.let { amount ->
                // Convert to cents by multiplying by 100 and rounding to nearest whole number
                (amount * 100).roundToLong()
            }
        } catch (e: Exception) {
            null
        }
    }
}

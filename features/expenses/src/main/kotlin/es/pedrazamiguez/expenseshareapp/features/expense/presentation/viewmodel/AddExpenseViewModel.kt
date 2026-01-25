package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.hardcoded
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<AddExpenseUiAction>()
    val actions: SharedFlow<AddExpenseUiAction> = _actions.asSharedFlow()

    fun onEvent(
        event: AddExpenseUiEvent, onAddExpenseSuccess: () -> Unit
    ) {
        when (event) {
            is AddExpenseUiEvent.TitleChanged -> {
                _uiState.value = _uiState.value.copy(
                    expenseTitle = event.title,
                    isTitleValid = true, // Clear title validation error when user types
                    error = if (_uiState.value.error?.contains("title") == true) null else _uiState.value.error
                )
            }

            is AddExpenseUiEvent.AmountChanged -> {
                _uiState.value = _uiState.value.copy(
                    expenseAmount = event.amount,
                    isAmountValid = true, // Clear amount validation error when user types
                    error = if (_uiState.value.error?.contains("amount") == true || _uiState.value.error?.contains(
                            "valid amount"
                        ) == true
                    ) null else _uiState.value.error
                )
            }

            is AddExpenseUiEvent.SubmitAddExpense -> {
                if (_uiState.value.expenseTitle.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isTitleValid = false,
                        error = "Expense title cannot be empty".hardcoded
                    )
                    return
                }

                val amountInCents = CurrencyConverter
                    .parseToCents(_uiState.value.expenseAmount)
                    .getOrElse {
                        _uiState.value = _uiState.value.copy(
                            isAmountValid = false,
                            error = it.message?.hardcoded
                        )
                        return
                    }

                addExpense(
                    event.groupId,
                    amountInCents,
                    onAddExpenseSuccess
                )
            }
        }
    }

    private fun addExpense(
        groupId: String?, amountInCents: Long, onAddExpenseSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            runCatching {
                val expenseToAdd = Expense(
                    title = _uiState.value.expenseTitle,
                    amountCents = amountInCents,
                )

                addExpenseUseCase(
                    groupId = groupId,
                    expense = expenseToAdd
                )
            }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onAddExpenseSuccess()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message,
                        isLoading = false
                    )
                    _actions.emit(
                        AddExpenseUiAction.ShowError(
                            e.message ?: "Expense addition failed".hardcoded
                        )
                    )
                }
        }
    }

}

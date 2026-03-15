package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.exception.InsufficientCashException
import es.pedrazamiguez.expenseshareapp.domain.model.ValidationResult
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedPaymentMethodUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Handles expense form submission:
 * [SubmitAddExpense].
 *
 * Validates the form, maps to a domain object, and delegates to the use case.
 */
class SubmitEventHandler(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val expenseValidationService: ExpenseValidationService,
    private val setGroupLastUsedCurrencyUseCase: SetGroupLastUsedCurrencyUseCase,
    private val setGroupLastUsedPaymentMethodUseCase: SetGroupLastUsedPaymentMethodUseCase,
    private val setGroupLastUsedCategoryUseCase: SetGroupLastUsedCategoryUseCase,
    private val addExpenseUiMapper: AddExpenseUiMapper
) : AddExpenseEventHandler {

    private lateinit var _uiState: MutableStateFlow<AddExpenseUiState>
    private lateinit var _actions: MutableSharedFlow<AddExpenseUiAction>
    private lateinit var scope: CoroutineScope

    override fun bind(
        stateFlow: MutableStateFlow<AddExpenseUiState>,
        actionsFlow: MutableSharedFlow<AddExpenseUiAction>,
        scope: CoroutineScope
    ) {
        _uiState = stateFlow
        _actions = actionsFlow
        this.scope = scope
    }

    fun submitExpense(groupId: String?, onSuccess: () -> Unit) {
        if (groupId == null) return

        val currentState = _uiState.value

        // Validate title using domain service
        val titleValidation = expenseValidationService.validateTitle(currentState.expenseTitle)
        if (titleValidation is ValidationResult.Invalid) {
            _uiState.update {
                it.copy(
                    isTitleValid = false,
                    error = UiText.StringResource(R.string.expense_error_title_empty)
                )
            }
            return
        }

        // Validate amount using domain service
        val amountValidation = expenseValidationService.validateAmount(currentState.sourceAmount)
        if (amountValidation is ValidationResult.Invalid) {
            _uiState.update {
                it.copy(
                    isAmountValid = false,
                    error = UiText.DynamicString(amountValidation.message)
                )
            }
            return
        }

        // Validate due date when payment status is SCHEDULED
        if (currentState.selectedPaymentStatus?.id == PaymentStatus.SCHEDULED.name &&
            currentState.dueDateMillis == null
        ) {
            _uiState.update {
                it.copy(
                    isDueDateValid = false,
                    error = UiText.StringResource(R.string.expense_error_due_date_required)
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        addExpenseUiMapper.mapToDomain(_uiState.value, groupId).onSuccess { expense ->
            scope.launch {
                addExpenseUseCase(groupId, expense).onSuccess {
                    // Save the user's selections specific to this group
                    _uiState.value.selectedCurrency?.code?.let { code ->
                        runCatching {
                            setGroupLastUsedCurrencyUseCase(groupId, code)
                        }
                    }
                    _uiState.value.selectedPaymentMethod?.id?.let { id ->
                        runCatching {
                            setGroupLastUsedPaymentMethodUseCase(groupId, id)
                        }
                    }
                    _uiState.value.selectedCategory?.id?.let { id ->
                        runCatching {
                            setGroupLastUsedCategoryUseCase(groupId, id)
                        }
                    }
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }.onFailure { e ->
                    // Clear loading and ensure no stale inline error is visible;
                    // the snackbar is the correct surface for submission errors.
                    _uiState.update { it.copy(isLoading = false, error = null) }

                    when (e) {
                        is InsufficientCashException -> {
                            // Use the cash currency (the currency the user actually paid in),
                            // NOT the group currency — the cent values come from the source amount.
                            val cashCurrency = currentState.selectedCurrency
                            if (cashCurrency != null) {
                                val required = addExpenseUiMapper.formatCentsForDisplay(
                                    e.requiredCents,
                                    cashCurrency
                                )
                                val available = addExpenseUiMapper.formatCentsForDisplay(
                                    e.availableCents,
                                    cashCurrency
                                )
                                _actions.emit(
                                    AddExpenseUiAction.ShowError(
                                        UiText.StringResource(
                                            R.string.expense_error_insufficient_cash,
                                            required,
                                            available
                                        )
                                    )
                                )
                            } else {
                                _actions.emit(
                                    AddExpenseUiAction.ShowError(
                                        UiText.StringResource(R.string.expense_error_addition_failed)
                                    )
                                )
                            }
                        }

                        else -> {
                            _actions.emit(
                                AddExpenseUiAction.ShowError(
                                    UiText.StringResource(R.string.expense_error_addition_failed)
                                )
                            )
                        }
                    }
                }
            }
        }.onFailure { e ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = UiText.DynamicString(e.message ?: "Unknown error")
                )
            }
        }
    }
}

package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getGroupExpenseConfigUseCase: GetGroupExpenseConfigUseCase,
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val addExpenseUiMapper: AddExpenseUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<AddExpenseUiAction>()
    val actions: SharedFlow<AddExpenseUiAction> = _actions.asSharedFlow()

    fun onEvent(event: AddExpenseUiEvent, onAddExpenseSuccess: () -> Unit = {}) {
        when (event) {
            is AddExpenseUiEvent.LoadGroupConfig -> loadGroupConfig(event.groupId)

            is AddExpenseUiEvent.RetryLoadConfig -> {
                // Reset error state before retrying
                _uiState.update {
                    it.copy(
                        configLoadFailed = false,
                        errorRes = null,
                        errorMessage = null
                    )
                }
                loadGroupConfig(event.groupId, forceRefresh = true)
            }

            is AddExpenseUiEvent.TitleChanged -> {
                _uiState.update {
                    it.copy(
                        expenseTitle = event.title,
                        isTitleValid = true,
                        errorRes = null,
                        errorMessage = null
                    )
                }
            }

            is AddExpenseUiEvent.SourceAmountChanged -> {
                _uiState.update {
                    it.copy(
                        sourceAmount = event.amount,
                        isAmountValid = true,
                        errorRes = null,
                        errorMessage = null
                    )
                }
                recalculateForward()
            }

            is AddExpenseUiEvent.CurrencySelected -> {
                val isForeign = event.currency.code != _uiState.value.groupCurrency?.code
                _uiState.update {
                    it.copy(selectedCurrency = event.currency, showExchangeRateSection = isForeign)
                }
                // If switching to foreign, try to fetch a rate, otherwise default to 1.0 or existing
                if (isForeign) fetchRate() else _uiState.update { it.copy(exchangeRate = "1.0") }
                recalculateForward()
            }

            is AddExpenseUiEvent.PaymentMethodSelected -> {
                _uiState.update { it.copy(selectedPaymentMethod = event.method) }
            }

            is AddExpenseUiEvent.ExchangeRateChanged -> {
                _uiState.update { it.copy(exchangeRate = event.rate) }
                recalculateForward()
            }

            is AddExpenseUiEvent.GroupAmountChanged -> {
                _uiState.update { it.copy(calculatedGroupAmount = event.amount) }
                recalculateReverse()
            }

            is AddExpenseUiEvent.SubmitAddExpense -> submitExpense(
                event.groupId,
                onAddExpenseSuccess
            )
        }
    }

    private fun loadGroupConfig(groupId: String?, forceRefresh: Boolean = false) {
        if (groupId == null) return

        val currentState = _uiState.value
        val isGroupChanged = currentState.loadedGroupId != groupId

        // Optimization: Don't reload if we already have data for the SAME group (e.g., on screen rotation)
        // unless forceRefresh is explicitly requested (e.g., retry after error)
        // Always reload if the groupId has changed
        if (!forceRefresh && !isGroupChanged && currentState.isConfigLoaded) return

        viewModelScope.launch {
            // Reset form state when loading a different group's config
            if (isGroupChanged) {
                _uiState.update {
                    AddExpenseUiState(isLoading = true, configLoadFailed = false)
                }
            } else {
                _uiState.update { it.copy(isLoading = true, configLoadFailed = false) }
            }

            getGroupExpenseConfigUseCase(groupId, forceRefresh)
                .onSuccess { config ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isConfigLoaded = true,
                            configLoadFailed = false,
                            loadedGroupId = groupId,
                            groupName = config.group.name,
                            groupCurrency = config.groupCurrency,
                            availableCurrencies = config.availableCurrencies.toImmutableList(),
                            selectedCurrency = config.groupCurrency, // Default to group currency
                            errorRes = null,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to load group configuration for groupId: $groupId")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isConfigLoaded = false,
                            configLoadFailed = true,
                            errorRes = R.string.expense_error_load_group_config
                        )
                    }
                }
        }
    }

    /**
     * Calculates the group amount from source amount and exchange rate.
     * All BigDecimal operations are delegated to ExpenseCalculatorService.
     */
    private fun recalculateForward() {
        val state = _uiState.value
        val sourceDecimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
        val targetDecimalPlaces = state.groupCurrency?.decimalDigits ?: 2
        val calculatedAmount = expenseCalculatorService.calculateGroupAmountFromStrings(
            sourceAmountString = state.sourceAmount,
            exchangeRateString = state.exchangeRate,
            sourceDecimalPlaces = sourceDecimalPlaces,
            targetDecimalPlaces = targetDecimalPlaces
        )
        _uiState.update { it.copy(calculatedGroupAmount = calculatedAmount) }
    }

    /**
     * Calculates the implied exchange rate from source and group amounts.
     * All BigDecimal operations are delegated to ExpenseCalculatorService.
     */
    private fun recalculateReverse() {
        val state = _uiState.value
        val sourceDecimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
        val impliedRate = expenseCalculatorService.calculateImpliedRateFromStrings(
            sourceAmountString = state.sourceAmount,
            groupAmountString = state.calculatedGroupAmount,
            sourceDecimalPlaces = sourceDecimalPlaces
        )
        _uiState.update { it.copy(exchangeRate = impliedRate) }
    }

    private fun fetchRate() {
        // Here you would call a use case to get exchange rates
        // For MVP, we can simulate or just leave current rate.
        // If you implement the API call, update _uiState.exchangeRate inside the coroutine.
    }

    private fun submitExpense(groupId: String?, onSuccess: () -> Unit) {
        if (groupId == null) return

        if (_uiState.value.expenseTitle.isBlank()) {
            _uiState.update {
                it.copy(
                    isTitleValid = false,
                    errorRes = R.string.expense_error_title_empty
                )
            }
            return
        }

        val currentState = _uiState.value
        val amountResult = CurrencyConverter.parseToCents(currentState.sourceAmount)
        if (amountResult.isFailure) {
            _uiState.update {
                it.copy(
                    isAmountValid = false,
                    errorMessage = amountResult.exceptionOrNull()?.message ?: "Invalid amount"
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorRes = null, errorMessage = null) }

        addExpenseUiMapper.mapToDomain(_uiState.value, groupId)
            .onSuccess { expense ->
                viewModelScope.launch {
                    addExpenseUseCase(groupId, expense).onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                        onSuccess()
                    }.onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                        _actions.emit(
                            AddExpenseUiAction.ShowError(
                                R.string.expense_error_addition_failed,
                                e.message
                            )
                        )
                    }
                }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
    }

}

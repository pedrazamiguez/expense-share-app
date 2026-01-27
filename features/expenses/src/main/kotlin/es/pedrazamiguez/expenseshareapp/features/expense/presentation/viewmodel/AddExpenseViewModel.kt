package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val groupRepository: GroupRepository,
    private val currencyRepository: CurrencyRepository,
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

            is AddExpenseUiEvent.TitleChanged -> {
                _uiState.update { it.copy(expenseTitle = event.title, isTitleValid = true, errorRes = null, errorMessage = null) }
            }

            is AddExpenseUiEvent.SourceAmountChanged -> {
                _uiState.update { it.copy(sourceAmount = event.amount, isAmountValid = true, errorMessage = null) }
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

            is AddExpenseUiEvent.SubmitAddExpense -> submitExpense(event.groupId, onAddExpenseSuccess)
        }
    }

    private fun loadGroupConfig(groupId: String?) {
        if (groupId == null) return
        viewModelScope.launch {
            val group = withContext(Dispatchers.IO) {
                groupRepository.getGroupById(groupId)
            } ?: return@launch
            val allCurrencies = withContext(Dispatchers.IO) {
                currencyRepository.getCurrencies()
            }

            val groupCurrency = allCurrencies.find { it.code == group.currency }
            // Include group's main currency plus any extra currencies configured for the group
            val allowedCodes = (listOf(group.currency) + group.extraCurrencies).distinct()
            val available = allCurrencies.filter { it.code in allowedCodes }

            _uiState.update {
                it.copy(
                    groupCurrency = groupCurrency,
                    availableCurrencies = available,
                    selectedCurrency = groupCurrency // Default to group currency
                )
            }
        }
    }

    private fun recalculateForward() {
        val state = _uiState.value
        // Parse source amount using the same logic as CurrencyConverter
        val sourceCents = CurrencyConverter.parseToCents(state.sourceAmount).getOrNull()
        val source = sourceCents?.let { BigDecimal(it).divide(BigDecimal(100)) } ?: BigDecimal.ZERO
        val rate = state.exchangeRate.toBigDecimalOrNull() ?: BigDecimal.ONE

        val result = expenseCalculatorService.calculateGroupAmount(source, rate)
        _uiState.update { it.copy(calculatedGroupAmount = result.toPlainString()) }
    }

    private fun recalculateReverse() {
        val state = _uiState.value
        val source = state.sourceAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val target = state.calculatedGroupAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO

        val result = expenseCalculatorService.calculateImpliedRate(source, target)
        _uiState.update { it.copy(exchangeRate = result.stripTrailingZeros().toPlainString()) }
    }

    private fun fetchRate() {
        // Here you would call currencyRepository.getExchangeRate(...)
        // For MVP, we can simulate or just leave current rate.
        // If you implement the API call, update _uiState.exchangeRate inside the coroutine.
    }

    private fun submitExpense(groupId: String?, onSuccess: () -> Unit) {
        if (groupId == null) return

        if (_uiState.value.expenseTitle.isBlank()) {
            _uiState.update { it.copy(isTitleValid = false, errorRes = R.string.expense_error_title_empty) }
            return
        }

        val currentState = _uiState.value
        val amountResult = CurrencyConverter.parseToCents(currentState.sourceAmount)
        if (amountResult.isFailure) {
            _uiState.update {
                it.copy(
                    isAmountValid = false,
                    errorMessage = amountResult.exceptionOrNull()?.message ?: "Amount must be greater than zero"
                )
            }
            return
        } else {
            // Clear any previous amount error
            _uiState.update { it.copy(isAmountValid = true, errorMessage = null) }
        }

        _uiState.update { it.copy(isLoading = true, errorRes = null) }

        addExpenseUiMapper.mapToDomain(_uiState.value, groupId)
            .onSuccess { expense ->
                viewModelScope.launch {
                    addExpenseUseCase(groupId, expense).onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                        onSuccess()
                    }.onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                        _actions.emit(AddExpenseUiAction.ShowError(R.string.expense_error_addition_failed, e.message))
                    }
                }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                viewModelScope.launch {
                    _actions.emit(AddExpenseUiAction.ShowError(R.string.expense_error_addition_failed, e.message))
                }
            }
    }

}

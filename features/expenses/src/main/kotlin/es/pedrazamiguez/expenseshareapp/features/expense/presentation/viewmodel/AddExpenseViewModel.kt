package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.ConfigEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.CurrencyEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.SplitEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.SubmitEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AddExpenseViewModel(
    private val configEventHandler: ConfigEventHandler,
    private val currencyEventHandler: CurrencyEventHandler,
    private val splitEventHandler: SplitEventHandler,
    private val submitEventHandler: SubmitEventHandler,
    private val addExpenseUiMapper: AddExpenseUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<AddExpenseUiAction>()
    val actions: SharedFlow<AddExpenseUiAction> = _actions.asSharedFlow()

    init {
        // Bind all handlers to the shared state and actions flows
        configEventHandler.bind(_uiState, _actions, viewModelScope)
        currencyEventHandler.bind(_uiState, _actions, viewModelScope)
        splitEventHandler.bind(_uiState, _actions, viewModelScope)
        submitEventHandler.bind(_uiState, _actions, viewModelScope)
    }

    fun onEvent(event: AddExpenseUiEvent, onAddExpenseSuccess: () -> Unit = {}) {
        when (event) {
            // ── Config ──────────────────────────────────────────────────
            is AddExpenseUiEvent.LoadGroupConfig ->
                configEventHandler.loadGroupConfig(event.groupId)

            is AddExpenseUiEvent.RetryLoadConfig -> {
                _uiState.update { it.copy(configLoadFailed = false, error = null) }
                configEventHandler.loadGroupConfig(event.groupId, forceRefresh = true)
            }

            // ── Currency & Exchange Rate ────────────────────────────────
            is AddExpenseUiEvent.CurrencySelected ->
                currencyEventHandler.handleCurrencySelected(event.currencyCode) {
                    splitEventHandler.recalculateSplits()
                }

            is AddExpenseUiEvent.ExchangeRateChanged ->
                currencyEventHandler.handleExchangeRateChanged(event.rate)

            is AddExpenseUiEvent.GroupAmountChanged ->
                currencyEventHandler.handleGroupAmountChanged(event.amount)

            // ── Splits ──────────────────────────────────────────────────
            is AddExpenseUiEvent.SplitTypeChanged ->
                splitEventHandler.handleSplitTypeChanged(event.splitTypeId)

            is AddExpenseUiEvent.SplitAmountChanged ->
                splitEventHandler.handleExactAmountChanged(event.userId, event.amount)

            is AddExpenseUiEvent.SplitPercentageChanged ->
                splitEventHandler.handlePercentageChanged(event.userId, event.percentage)

            is AddExpenseUiEvent.SplitExcludedToggled ->
                splitEventHandler.handleSplitExcludedToggled(event.userId)

            // ── Submission ──────────────────────────────────────────────
            is AddExpenseUiEvent.SubmitAddExpense ->
                submitEventHandler.submitExpense(event.groupId, onAddExpenseSuccess)

            // ── Simple form field updates (inline) ──────────────────────
            is AddExpenseUiEvent.TitleChanged -> {
                _uiState.update {
                    it.copy(
                        expenseTitle = event.title,
                        isTitleValid = true,
                        error = null
                    )
                }
            }

            is AddExpenseUiEvent.SourceAmountChanged -> {
                _uiState.update {
                    it.copy(
                        sourceAmount = event.amount,
                        isAmountValid = true,
                        error = null
                    )
                }
                currencyEventHandler.recalculateForward()
                splitEventHandler.recalculateSplits()
            }

            is AddExpenseUiEvent.PaymentMethodSelected -> {
                val selectedMethod = _uiState.value.paymentMethods
                    .find { it.id == event.methodId } ?: return
                _uiState.update { it.copy(selectedPaymentMethod = selectedMethod) }
            }

            is AddExpenseUiEvent.CategorySelected -> {
                val selectedCategory = _uiState.value.availableCategories
                    .find { it.id == event.categoryId } ?: return
                _uiState.update { it.copy(selectedCategory = selectedCategory) }
            }

            is AddExpenseUiEvent.VendorChanged -> {
                _uiState.update { it.copy(vendor = event.vendor) }
            }

            is AddExpenseUiEvent.PaymentStatusSelected -> {
                val selectedStatus = _uiState.value.availablePaymentStatuses
                    .find { it.id == event.statusId } ?: return
                val isScheduled = event.statusId == PaymentStatus.SCHEDULED.name
                _uiState.update {
                    it.copy(
                        selectedPaymentStatus = selectedStatus,
                        showDueDateSection = isScheduled,
                        dueDateMillis = if (isScheduled) it.dueDateMillis else null,
                        formattedDueDate = if (isScheduled) it.formattedDueDate else "",
                        isDueDateValid = true
                    )
                }
            }

            is AddExpenseUiEvent.DueDateSelected -> {
                val formattedDate = addExpenseUiMapper.formatDueDateForDisplay(event.dateMillis)
                _uiState.update {
                    it.copy(
                        dueDateMillis = event.dateMillis,
                        formattedDueDate = formattedDate,
                        isDueDateValid = true
                    )
                }
            }

            is AddExpenseUiEvent.ReceiptImageSelected -> {
                _uiState.update { it.copy(receiptUri = event.uri) }
            }

            is AddExpenseUiEvent.RemoveReceiptImage -> {
                _uiState.update { it.copy(receiptUri = null) }
            }
        }
    }
}

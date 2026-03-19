package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.ConfigEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.CurrencyEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.SplitEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.SubmitEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.SubunitSplitEventHandler
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
    private val subunitSplitEventHandler: SubunitSplitEventHandler,
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
        subunitSplitEventHandler.bind(_uiState, _actions, viewModelScope)
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
                    subunitSplitEventHandler.recalculateEntitySplits()
                }

            is AddExpenseUiEvent.ExchangeRateChanged ->
                currencyEventHandler.handleExchangeRateChanged(event.rate)

            is AddExpenseUiEvent.GroupAmountChanged ->
                currencyEventHandler.handleGroupAmountChanged(event.amount)

            // ── Splits ──────────────────────────────────────────────────
            is AddExpenseUiEvent.SplitTypeChanged -> {
                splitEventHandler.handleSplitTypeChanged(event.splitTypeId)
                // Also recalculate entity splits if in sub-unit mode
                subunitSplitEventHandler.recalculateEntitySplits()
            }

            is AddExpenseUiEvent.SplitAmountChanged ->
                splitEventHandler.handleExactAmountChanged(event.userId, event.amount)

            is AddExpenseUiEvent.SplitPercentageChanged ->
                splitEventHandler.handlePercentageChanged(event.userId, event.percentage)

            is AddExpenseUiEvent.SplitExcludedToggled ->
                splitEventHandler.handleSplitExcludedToggled(event.userId)

            // ── Sub-unit splits ────────────────────────────────────────────
            is AddExpenseUiEvent.SubunitModeToggled ->
                subunitSplitEventHandler.handleSubunitModeToggled()

            is AddExpenseUiEvent.EntityAccordionToggled ->
                subunitSplitEventHandler.handleAccordionToggled(event.entityId)

            is AddExpenseUiEvent.EntitySplitExcludedToggled ->
                subunitSplitEventHandler.handleEntityExcludedToggled(event.entityId)

            is AddExpenseUiEvent.EntitySplitAmountChanged ->
                subunitSplitEventHandler.handleEntityAmountChanged(event.entityId, event.amount)

            is AddExpenseUiEvent.EntitySplitPercentageChanged ->
                subunitSplitEventHandler.handleEntityPercentageChanged(event.entityId, event.percentage)

            is AddExpenseUiEvent.IntraSubunitSplitTypeChanged ->
                subunitSplitEventHandler.handleIntraSubunitSplitTypeChanged(event.subunitId, event.splitTypeId)

            is AddExpenseUiEvent.IntraSubunitAmountChanged ->
                subunitSplitEventHandler.handleIntraSubunitAmountChanged(event.subunitId, event.userId, event.amount)

            is AddExpenseUiEvent.IntraSubunitPercentageChanged ->
                subunitSplitEventHandler.handleIntraSubunitPercentageChanged(
                    event.subunitId,
                    event.userId,
                    event.percentage
                )

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
                // For CASH + foreign currency, recalculate from ATM withdrawals (debounced)
                if (_uiState.value.isExchangeRateLocked) {
                    currencyEventHandler.recalculateCashForward()
                } else {
                    currencyEventHandler.recalculateForward()
                }
                splitEventHandler.recalculateSplits()
                subunitSplitEventHandler.recalculateEntitySplits()
            }

            is AddExpenseUiEvent.PaymentMethodSelected -> {
                val selectedMethod = _uiState.value.paymentMethods
                    .find { it.id == event.methodId } ?: return
                _uiState.update { it.copy(selectedPaymentMethod = selectedMethod) }

                // React to payment method change for exchange rate behavior
                val isCash = try {
                    PaymentMethod.fromString(selectedMethod.id) == PaymentMethod.CASH
                } catch (_: IllegalArgumentException) {
                    false
                }
                currencyEventHandler.handlePaymentMethodChanged(isCash)
            }

            is AddExpenseUiEvent.CategorySelected -> {
                val selectedCategory = _uiState.value.availableCategories
                    .find { it.id == event.categoryId } ?: return
                _uiState.update { it.copy(selectedCategory = selectedCategory) }
            }

            is AddExpenseUiEvent.VendorChanged -> {
                _uiState.update { it.copy(vendor = event.vendor) }
            }

            is AddExpenseUiEvent.NotesChanged -> {
                _uiState.update { it.copy(notes = event.notes) }
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

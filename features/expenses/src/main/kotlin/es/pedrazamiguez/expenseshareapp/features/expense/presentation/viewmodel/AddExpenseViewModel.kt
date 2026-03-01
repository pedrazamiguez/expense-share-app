package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.model.ValidationResult
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getGroupExpenseConfigUseCase: GetGroupExpenseConfigUseCase,
    private val getExchangeRateUseCase: GetExchangeRateUseCase,
    private val getGroupLastUsedCurrencyUseCase: GetGroupLastUsedCurrencyUseCase,
    private val setGroupLastUsedCurrencyUseCase: SetGroupLastUsedCurrencyUseCase,
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val expenseValidationService: ExpenseValidationService,
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
                        configLoadFailed = false, error = null
                    )
                }
                loadGroupConfig(event.groupId, forceRefresh = true)
            }

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
                recalculateForward()
            }

            is AddExpenseUiEvent.CurrencySelected -> {
                val currentState = _uiState.value
                val selectedUiModel = currentState.availableCurrencies
                    .find { it.code == event.currencyCode } ?: return
                val isForeign = selectedUiModel.code != currentState.groupCurrency?.code

                val exchangeRateLabel = if (isForeign && currentState.groupCurrency != null) {
                    addExpenseUiMapper.buildExchangeRateLabel(currentState.groupCurrency, selectedUiModel)
                } else ""

                _uiState.update {
                    it.copy(
                        selectedCurrency = selectedUiModel,
                        showExchangeRateSection = isForeign,
                        exchangeRateLabel = exchangeRateLabel
                    )
                }
                // If switching to foreign, try to fetch a rate, otherwise default to 1.0
                if (isForeign) fetchRate() else _uiState.update { it.copy(displayExchangeRate = "1.0") }
                recalculateForward()
            }

            is AddExpenseUiEvent.PaymentMethodSelected -> {
                val selectedMethod = _uiState.value.paymentMethods
                    .find { it.id == event.methodId } ?: return
                _uiState.update { it.copy(selectedPaymentMethod = selectedMethod) }
            }

            is AddExpenseUiEvent.ExchangeRateChanged -> {
                _uiState.update { it.copy(displayExchangeRate = event.rate) }
                recalculateForward()
            }

            is AddExpenseUiEvent.GroupAmountChanged -> {
                _uiState.update { it.copy(calculatedGroupAmount = event.amount) }
                recalculateReverse()
            }

            is AddExpenseUiEvent.SubmitAddExpense -> submitExpense(
                event.groupId, onAddExpenseSuccess
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

            getGroupExpenseConfigUseCase(groupId, forceRefresh).onSuccess { config ->
                    // Grab the last used currency for this specific group
                    val lastUsedCode = getGroupLastUsedCurrencyUseCase(groupId).firstOrNull()

                    // Map domain models to UI models
                    val mappedCurrencies = addExpenseUiMapper.mapCurrencies(config.availableCurrencies)
                    val mappedGroupCurrency = addExpenseUiMapper.mapCurrency(config.groupCurrency)
                    val mappedPaymentMethods = addExpenseUiMapper.mapPaymentMethods(
                        es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod.entries
                    )
                    val defaultPaymentMethod = mappedPaymentMethods.firstOrNull()

                    // Match against allowed currencies, fallback to default if not found or null
                    val initialCurrencyDomain =
                        config.availableCurrencies.find { it.code == lastUsedCode }
                            ?: config.groupCurrency
                    val initialCurrency = addExpenseUiMapper.mapCurrency(initialCurrencyDomain)

                    val isForeign = initialCurrency.code != mappedGroupCurrency.code
                    val exchangeRateLabel = if (isForeign) {
                        addExpenseUiMapper.buildExchangeRateLabel(mappedGroupCurrency, initialCurrency)
                    } else ""
                    val groupAmountLabel = addExpenseUiMapper.buildGroupAmountLabel(mappedGroupCurrency)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isConfigLoaded = true,
                            configLoadFailed = false,
                            loadedGroupId = groupId,
                            groupName = config.group.name,
                            groupCurrency = mappedGroupCurrency,
                            availableCurrencies = mappedCurrencies,
                            paymentMethods = mappedPaymentMethods,
                            selectedCurrency = initialCurrency,
                            selectedPaymentMethod = defaultPaymentMethod,
                            showExchangeRateSection = isForeign,
                            exchangeRateLabel = exchangeRateLabel,
                            groupAmountLabel = groupAmountLabel,
                            error = null
                        )
                    }

                    if (isForeign) {
                        fetchRate()
                    }
                }.onFailure { e ->
                    Timber.e(e, "Failed to load group configuration for groupId: $groupId")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isConfigLoaded = false,
                            configLoadFailed = true,
                            error = UiText.StringResource(R.string.expense_error_load_group_config)
                        )
                    }
                }
        }
    }

    /**
     * Calculates the group amount from source amount and display exchange rate.
     * Uses the user-friendly display rate (1 GroupCurrency = X SourceCurrency).
     * All BigDecimal operations are delegated to ExpenseCalculatorService.
     */
    private fun recalculateForward() {
        val state = _uiState.value
        val sourceDecimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
        val targetDecimalPlaces = state.groupCurrency?.decimalDigits ?: 2
        val calculatedAmount = expenseCalculatorService.calculateGroupAmountFromDisplayRate(
            sourceAmountString = state.sourceAmount,
            displayRateString = state.displayExchangeRate,
            sourceDecimalPlaces = sourceDecimalPlaces,
            targetDecimalPlaces = targetDecimalPlaces
        )
        // Format the amount for display using locale-aware formatting
        // Use currency's decimal digits as minimum to ensure proper display (e.g., "1,10" for EUR instead of "1,1")
        val formattedAmount = addExpenseUiMapper.formatForDisplay(
            internalValue = calculatedAmount,
            maxDecimalPlaces = targetDecimalPlaces,
            minDecimalPlaces = targetDecimalPlaces
        )
        _uiState.update { it.copy(calculatedGroupAmount = formattedAmount) }
    }

    /**
     * Calculates the implied display exchange rate from source and group amounts.
     * Returns the rate in user-friendly format (1 GroupCurrency = X SourceCurrency).
     * All BigDecimal operations are delegated to ExpenseCalculatorService.
     */
    private fun recalculateReverse() {
        val state = _uiState.value
        val sourceDecimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
        val impliedDisplayRate = expenseCalculatorService.calculateImpliedDisplayRateFromStrings(
            sourceAmountString = state.sourceAmount,
            groupAmountString = state.calculatedGroupAmount,
            sourceDecimalPlaces = sourceDecimalPlaces
        )
        // Format the rate for display using locale-aware formatting
        val formattedRate = addExpenseUiMapper.formatRateForDisplay(impliedDisplayRate)
        _uiState.update { it.copy(displayExchangeRate = formattedRate) }
    }

    private fun fetchRate() {
        val state = _uiState.value
        val groupCurrency = state.groupCurrency
        val selectedCurrency = state.selectedCurrency

        if (groupCurrency == null || selectedCurrency == null || groupCurrency.code == selectedCurrency.code) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRate = true) }

            try {
                val rate = getExchangeRateUseCase(
                    baseCurrencyCode = groupCurrency.code,
                    targetCurrencyCode = selectedCurrency.code
                )

                _uiState.update {
                    it.copy(
                        isLoadingRate = false,
                        // If rate found, update display; otherwise keep existing/default
                        displayExchangeRate = rate?.let { exchangeRate ->
                            addExpenseUiMapper.formatRateForDisplay(exchangeRate.toPlainString())
                        } ?: it.displayExchangeRate)
                }

                if (rate != null) {
                    recalculateForward()
                }
            } catch (e: Exception) {
                Timber.e(
                    e,
                    "Failed to fetch exchange rate for ${groupCurrency.code} -> ${selectedCurrency.code}"
                )
                _uiState.update { it.copy(isLoadingRate = false) }
            }
        }
    }

    private fun submitExpense(groupId: String?, onSuccess: () -> Unit) {
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

        _uiState.update { it.copy(isLoading = true, error = null) }

        addExpenseUiMapper.mapToDomain(_uiState.value, groupId).onSuccess { expense ->
                viewModelScope.launch {
                    addExpenseUseCase(groupId, expense).onSuccess {
                        // Save the currency code specific to this group
                        _uiState.value.selectedCurrency?.code?.let { code ->
                            setGroupLastUsedCurrencyUseCase(groupId, code)
                        }
                        _uiState.update { it.copy(isLoading = false) }
                        onSuccess()
                    }.onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = UiText.DynamicString(e.message ?: "Unknown error")
                            )
                        }
                        _actions.emit(
                            AddExpenseUiAction.ShowError(
                                UiText.StringResource(R.string.expense_error_addition_failed)
                            )
                        )
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

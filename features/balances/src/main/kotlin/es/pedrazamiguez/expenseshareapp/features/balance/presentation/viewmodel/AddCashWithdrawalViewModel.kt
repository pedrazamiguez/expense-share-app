package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.AddOn
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.CashWithdrawalValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddCashWithdrawalUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.AddCashWithdrawalUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.SubunitOptionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.AddCashWithdrawalUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState
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

class AddCashWithdrawalViewModel(
    private val addCashWithdrawalUseCase: AddCashWithdrawalUseCase,
    private val getGroupExpenseConfigUseCase: GetGroupExpenseConfigUseCase,
    private val getExchangeRateUseCase: GetExchangeRateUseCase,
    private val getGroupSubunitsUseCase: GetGroupSubunitsUseCase,
    private val authenticationService: AuthenticationService,
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val cashWithdrawalValidationService: CashWithdrawalValidationService,
    private val mapper: AddCashWithdrawalUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCashWithdrawalUiState())
    val uiState: StateFlow<AddCashWithdrawalUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<AddCashWithdrawalUiAction>()
    val actions: SharedFlow<AddCashWithdrawalUiAction> = _actions.asSharedFlow()

    fun onEvent(event: AddCashWithdrawalUiEvent, onSuccess: () -> Unit = {}) {
        when (event) {
            is AddCashWithdrawalUiEvent.LoadGroupConfig -> loadGroupConfig(event.groupId)

            is AddCashWithdrawalUiEvent.RetryLoadConfig -> {
                _uiState.update { it.copy(configLoadFailed = false, error = null) }
                loadGroupConfig(event.groupId, forceRefresh = true)
            }

            is AddCashWithdrawalUiEvent.CurrencySelected -> handleCurrencySelected(event.currencyCode)

            is AddCashWithdrawalUiEvent.WithdrawalAmountChanged -> {
                _uiState.update {
                    it.copy(withdrawalAmount = event.amount, isAmountValid = true, error = null)
                }
                recalculateDeducted()
            }

            is AddCashWithdrawalUiEvent.DeductedAmountChanged -> {
                _uiState.update { it.copy(deductedAmount = event.amount) }
                recalculateRateFromDeducted()
            }

            is AddCashWithdrawalUiEvent.ExchangeRateChanged -> {
                _uiState.update { it.copy(displayExchangeRate = event.rate) }
                recalculateDeducted()
            }

            is AddCashWithdrawalUiEvent.WithdrawalScopeSelected -> handleWithdrawalScopeSelected(
                event.scope,
                event.subunitId
            )

            is AddCashWithdrawalUiEvent.SubmitWithdrawal -> submitWithdrawal(
                event.groupId,
                onSuccess
            )

            // ATM Fee events
            is AddCashWithdrawalUiEvent.FeeToggled -> handleFeeToggled(event.hasFee)
            is AddCashWithdrawalUiEvent.FeeAmountChanged -> handleFeeAmountChanged(event.amount)
            is AddCashWithdrawalUiEvent.FeeCurrencySelected -> handleFeeCurrencySelected(event.currencyCode)
            is AddCashWithdrawalUiEvent.FeeExchangeRateChanged -> handleFeeExchangeRateChanged(event.rate)
            is AddCashWithdrawalUiEvent.FeeConvertedAmountChanged -> handleFeeConvertedAmountChanged(event.amount)
        }
    }

    private fun loadGroupConfig(groupId: String?, forceRefresh: Boolean = false) {
        if (groupId == null) return

        val currentState = _uiState.value
        val isGroupChanged = currentState.loadedGroupId != groupId

        if (!forceRefresh && !isGroupChanged && currentState.isConfigLoaded) return

        viewModelScope.launch {
            if (isGroupChanged) {
                _uiState.update {
                    AddCashWithdrawalUiState(isLoading = true, configLoadFailed = false)
                }
            } else {
                _uiState.update { it.copy(isLoading = true, configLoadFailed = false) }
            }

            getGroupExpenseConfigUseCase(groupId, forceRefresh)
                .onSuccess { config ->
                    val mappedCurrencies = mapper.mapCurrencies(config.availableCurrencies)
                    val mappedGroupCurrency = mapper.mapCurrency(config.groupCurrency)
                    val deductedAmountLabel = mapper.buildDeductedAmountLabel(mappedGroupCurrency)

                    // Load subunit options for scope selector
                    val subunitOptions = loadSubunitOptions(groupId)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isConfigLoaded = true,
                            configLoadFailed = false,
                            loadedGroupId = groupId,
                            groupName = config.group.name,
                            groupCurrency = mappedGroupCurrency,
                            availableCurrencies = mappedCurrencies,
                            selectedCurrency = mappedGroupCurrency,
                            showExchangeRateSection = false,
                            deductedAmountLabel = deductedAmountLabel,
                            subunitOptions = subunitOptions.toImmutableList(),
                            withdrawalScope = PayerType.GROUP,
                            selectedSubunitId = null,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to load group config for withdrawal")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isConfigLoaded = false,
                            configLoadFailed = true,
                            error = UiText.StringResource(R.string.withdrawal_error_load_config)
                        )
                    }
                }
        }
    }

    /**
     * Loads sub-unit options for the current user in the given group.
     * Only returns sub-units the current user belongs to.
     */
    private suspend fun loadSubunitOptions(groupId: String): List<SubunitOptionUiModel> {
        return try {
            val currentUserId = authenticationService.currentUserId()
            val subunits = getGroupSubunitsUseCase(groupId)
            subunits
                .filter { currentUserId != null && currentUserId in it.memberIds }
                .map { SubunitOptionUiModel(id = it.id, name = it.name) }
        } catch (e: Exception) {
            Timber.w(e, "Failed to load subunit options for withdrawal")
            emptyList()
        }
    }

    private fun handleWithdrawalScopeSelected(scope: PayerType, subunitId: String?) {
        _uiState.update {
            it.copy(
                withdrawalScope = scope,
                selectedSubunitId = if (scope == PayerType.SUBUNIT) subunitId else null
            )
        }
    }

    private fun handleCurrencySelected(currencyCode: String) {
        val currentState = _uiState.value
        val selectedUiModel = currentState.availableCurrencies
            .find { it.code == currencyCode } ?: return
        val isForeign = selectedUiModel.code != currentState.groupCurrency?.code

        val exchangeRateLabel = if (isForeign && currentState.groupCurrency != null) {
            mapper.buildExchangeRateLabel(currentState.groupCurrency, selectedUiModel)
        } else {
            ""
        }

        _uiState.update {
            it.copy(
                selectedCurrency = selectedUiModel,
                showExchangeRateSection = isForeign,
                exchangeRateLabel = exchangeRateLabel
            )
        }

        if (isForeign) {
            fetchRate()
        } else {
            _uiState.update { it.copy(displayExchangeRate = "1.0", deductedAmount = "") }
        }
        recalculateDeducted()
    }

    // ── ATM Fee Handlers ───────────────────────────────────────────────────

    private fun handleFeeToggled(hasFee: Boolean) {
        val state = _uiState.value
        val groupCurrency = state.groupCurrency ?: return

        if (hasFee) {
            // Initialize fee with group currency (no conversion needed by default)
            val feeConvertedLabel = mapper.buildFeeConvertedLabel(groupCurrency)
            _uiState.update {
                it.copy(
                    hasFee = true,
                    feeAmount = "",
                    feeCurrency = groupCurrency,
                    feeExchangeRate = "1.0",
                    feeConvertedAmount = "",
                    feeConvertedLabel = feeConvertedLabel,
                    showFeeExchangeRateSection = false,
                    isFeeAmountValid = true
                )
            }
        } else {
            // Clear all fee fields
            _uiState.update {
                it.copy(
                    hasFee = false,
                    feeAmount = "",
                    feeCurrency = null,
                    feeExchangeRate = "1.0",
                    feeConvertedAmount = "",
                    feeExchangeRateLabel = "",
                    feeConvertedLabel = "",
                    showFeeExchangeRateSection = false,
                    isFeeAmountValid = true
                )
            }
        }
    }

    private fun handleFeeAmountChanged(amount: String) {
        _uiState.update {
            it.copy(feeAmount = amount, isFeeAmountValid = true)
        }
        recalculateFeeConverted()
    }

    private fun handleFeeCurrencySelected(currencyCode: String) {
        val state = _uiState.value
        val feeCurrencyModel = state.availableCurrencies
            .find { it.code == currencyCode } ?: return
        val groupCurrency = state.groupCurrency ?: return
        val isForeign = feeCurrencyModel.code != groupCurrency.code

        val feeExchangeRateLabel = if (isForeign) {
            mapper.buildExchangeRateLabel(groupCurrency, feeCurrencyModel)
        } else {
            ""
        }

        _uiState.update {
            it.copy(
                feeCurrency = feeCurrencyModel,
                showFeeExchangeRateSection = isForeign,
                feeExchangeRateLabel = feeExchangeRateLabel,
                feeExchangeRate = if (isForeign) it.feeExchangeRate else "1.0"
            )
        }

        if (isForeign) {
            fetchFeeRate(groupCurrency.code, feeCurrencyModel.code)
        }
        recalculateFeeConverted()
    }

    private fun handleFeeExchangeRateChanged(rate: String) {
        _uiState.update { it.copy(feeExchangeRate = rate) }
        recalculateFeeConverted()
    }

    private fun handleFeeConvertedAmountChanged(amount: String) {
        _uiState.update { it.copy(feeConvertedAmount = amount) }
        recalculateFeeRateFromConverted()
    }

    private fun recalculateFeeConverted() {
        val state = _uiState.value
        if (!state.showFeeExchangeRateSection) {
            // Same currency - converted equals fee amount
            _uiState.update { it.copy(feeConvertedAmount = state.feeAmount) }
            return
        }

        val sourceDecimalPlaces = state.feeCurrency?.decimalDigits ?: 2
        val targetDecimalPlaces = state.groupCurrency?.decimalDigits ?: 2
        val calculatedConverted = expenseCalculatorService.calculateGroupAmountFromDisplayRate(
            sourceAmountString = state.feeAmount,
            displayRateString = state.feeExchangeRate,
            sourceDecimalPlaces = sourceDecimalPlaces,
            targetDecimalPlaces = targetDecimalPlaces
        )
        val formatted = mapper.formatForDisplay(
            internalValue = calculatedConverted,
            maxDecimalPlaces = targetDecimalPlaces,
            minDecimalPlaces = targetDecimalPlaces
        )
        _uiState.update { it.copy(feeConvertedAmount = formatted) }
    }

    private fun recalculateFeeRateFromConverted() {
        val state = _uiState.value
        if (!state.showFeeExchangeRateSection) return

        val sourceDecimalPlaces = state.feeCurrency?.decimalDigits ?: 2
        val impliedRate = expenseCalculatorService.calculateImpliedDisplayRateFromStrings(
            sourceAmountString = state.feeAmount,
            groupAmountString = state.feeConvertedAmount,
            sourceDecimalPlaces = sourceDecimalPlaces
        )
        val formatted = mapper.formatRateForDisplay(impliedRate)
        _uiState.update { it.copy(feeExchangeRate = formatted) }
    }

    private fun fetchFeeRate(groupCurrencyCode: String, feeCurrencyCode: String) {
        viewModelScope.launch {
            try {
                val rate = getExchangeRateUseCase(
                    baseCurrencyCode = groupCurrencyCode,
                    targetCurrencyCode = feeCurrencyCode
                )
                if (rate != null) {
                    _uiState.update {
                        it.copy(feeExchangeRate = mapper.formatRateForDisplay(rate.toPlainString()))
                    }
                    recalculateFeeConverted()
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to fetch fee exchange rate")
            }
        }
    }

    /**
     * Forward calculation: from withdrawal amount + exchange rate → deducted amount.
     * Uses: sourceAmount / displayRate = groupAmount (deducted from pocket).
     */
    private fun recalculateDeducted() {
        val state = _uiState.value
        if (!state.showExchangeRateSection) return

        val sourceDecimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
        val targetDecimalPlaces = state.groupCurrency?.decimalDigits ?: 2
        val calculatedDeducted = expenseCalculatorService.calculateGroupAmountFromDisplayRate(
            sourceAmountString = state.withdrawalAmount,
            displayRateString = state.displayExchangeRate,
            sourceDecimalPlaces = sourceDecimalPlaces,
            targetDecimalPlaces = targetDecimalPlaces
        )
        val formatted = mapper.formatForDisplay(
            internalValue = calculatedDeducted,
            maxDecimalPlaces = targetDecimalPlaces,
            minDecimalPlaces = targetDecimalPlaces
        )
        _uiState.update { it.copy(deductedAmount = formatted) }
    }

    /**
     * Reverse calculation: from withdrawal amount + deducted amount → implied exchange rate.
     */
    private fun recalculateRateFromDeducted() {
        val state = _uiState.value
        if (!state.showExchangeRateSection) return

        val sourceDecimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
        val impliedRate = expenseCalculatorService.calculateImpliedDisplayRateFromStrings(
            sourceAmountString = state.withdrawalAmount,
            groupAmountString = state.deductedAmount,
            sourceDecimalPlaces = sourceDecimalPlaces
        )
        val formatted = mapper.formatRateForDisplay(impliedRate)
        _uiState.update { it.copy(displayExchangeRate = formatted) }
    }

    private fun fetchRate() {
        val state = _uiState.value
        val groupCurrency = state.groupCurrency
        val selectedCurrency = state.selectedCurrency

        if (groupCurrency == null ||
            selectedCurrency == null ||
            groupCurrency.code == selectedCurrency.code
        ) {
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
                        displayExchangeRate = rate?.let { r ->
                            mapper.formatRateForDisplay(r.toPlainString())
                        } ?: it.displayExchangeRate
                    )
                }
                if (rate != null) recalculateDeducted()
            } catch (e: Exception) {
                Timber.w(e, "Failed to fetch exchange rate")
                _uiState.update { it.copy(isLoadingRate = false) }
            }
        }
    }

    private fun submitWithdrawal(groupId: String?, onSuccess: () -> Unit) {
        if (groupId == null) return
        val state = _uiState.value
        val selectedCurrency = state.selectedCurrency ?: return
        val groupCurrency = state.groupCurrency ?: return

        val amountWithdrawn = mapper.parseAmountToSmallestUnit(
            state.withdrawalAmount,
            selectedCurrency.code
        )

        // Validate
        val amountValidation = cashWithdrawalValidationService.validateAmountWithdrawn(amountWithdrawn)
        if (amountValidation is CashWithdrawalValidationService.ValidationResult.Invalid) {
            _uiState.update { it.copy(isAmountValid = false) }
            return
        }

        // Validate fee if enabled
        if (state.hasFee && state.feeAmount.isNotBlank()) {
            val feeCurrency = state.feeCurrency ?: groupCurrency
            val feeAmountCents = mapper.parseAmountToSmallestUnit(state.feeAmount, feeCurrency.code)
            if (feeAmountCents <= 0) {
                _uiState.update { it.copy(isFeeAmountValid = false) }
                return
            }
        }

        // For same currency, deducted = withdrawn. For foreign, parse user input.
        val deductedBaseAmount = if (state.showExchangeRateSection) {
            mapper.parseAmountToSmallestUnit(state.deductedAmount, groupCurrency.code)
        } else {
            amountWithdrawn
        }

        val exchangeRate = if (state.showExchangeRateSection) {
            expenseCalculatorService.calculateExchangeRate(
                amountWithdrawn = amountWithdrawn,
                deductedBaseAmount = deductedBaseAmount
            )
        } else {
            java.math.BigDecimal.ONE
        }

        // Build add-ons list (ATM fee if present)
        val addOns = buildFeeAddOn(state, groupCurrency)

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val withdrawal = CashWithdrawal(
                    groupId = groupId,
                    withdrawalScope = state.withdrawalScope,
                    subunitId = if (state.withdrawalScope == PayerType.SUBUNIT) state.selectedSubunitId else null,
                    amountWithdrawn = amountWithdrawn,
                    remainingAmount = amountWithdrawn,
                    currency = selectedCurrency.code,
                    deductedBaseAmount = deductedBaseAmount,
                    exchangeRate = exchangeRate,
                    addOns = addOns
                )
                val result = addCashWithdrawalUseCase(groupId, withdrawal)
                result.getOrThrow()
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Failed to add cash withdrawal")
                _uiState.update { it.copy(isLoading = false) }
                _actions.emit(
                    AddCashWithdrawalUiAction.ShowError(
                        UiText.StringResource(R.string.balances_withdraw_cash_error)
                    )
                )
            }
        }
    }

    /**
     * Builds the ATM fee add-on if user has enabled it and entered a valid amount.
     */
    private fun buildFeeAddOn(
        state: AddCashWithdrawalUiState,
        groupCurrency: CurrencyUiModel
    ): List<AddOn> {
        if (!state.hasFee || state.feeAmount.isBlank()) return emptyList()

        val feeCurrency = state.feeCurrency ?: groupCurrency
        val feeAmountCents = mapper.parseAmountToSmallestUnit(state.feeAmount, feeCurrency.code)
        if (feeAmountCents <= 0) return emptyList()

        // Calculate group amount in cents
        val groupAmountCents = if (state.showFeeExchangeRateSection && state.feeConvertedAmount.isNotBlank()) {
            mapper.parseAmountToSmallestUnit(state.feeConvertedAmount, groupCurrency.code)
        } else {
            feeAmountCents
        }

        // Calculate exchange rate (fee currency → group currency)
        val feeExchangeRate = if (state.showFeeExchangeRateSection && feeAmountCents > 0 && groupAmountCents > 0) {
            expenseCalculatorService.calculateExchangeRate(
                amountWithdrawn = feeAmountCents,
                deductedBaseAmount = groupAmountCents
            )
        } else {
            java.math.BigDecimal.ONE
        }

        return listOf(
            AddOn(
                id = java.util.UUID.randomUUID().toString(),
                type = AddOnType.FEE,
                mode = AddOnMode.ON_TOP,
                valueType = AddOnValueType.EXACT,
                amountCents = feeAmountCents,
                currency = feeCurrency.code,
                exchangeRate = feeExchangeRate,
                groupAmountCents = groupAmountCents,
                paymentMethod = PaymentMethod.OTHER,
                description = null
            )
        )
    }
}

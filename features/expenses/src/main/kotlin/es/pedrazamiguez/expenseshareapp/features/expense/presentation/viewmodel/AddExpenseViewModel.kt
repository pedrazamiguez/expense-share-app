package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.exception.InsufficientCashException
import es.pedrazamiguez.expenseshareapp.domain.model.ValidationResult
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory
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
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode

class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getGroupExpenseConfigUseCase: GetGroupExpenseConfigUseCase,
    private val getExchangeRateUseCase: GetExchangeRateUseCase,
    private val getGroupLastUsedCurrencyUseCase: GetGroupLastUsedCurrencyUseCase,
    private val setGroupLastUsedCurrencyUseCase: SetGroupLastUsedCurrencyUseCase,
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val expenseValidationService: ExpenseValidationService,
    private val splitCalculatorFactory: ExpenseSplitCalculatorFactory,
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
                recalculateSplits()
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
                recalculateSplits()
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
                        // Clear due date when switching away from SCHEDULED
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

            is AddExpenseUiEvent.SubmitAddExpense -> submitExpense(
                event.groupId, onAddExpenseSuccess
            )

            is AddExpenseUiEvent.SplitTypeChanged -> {
                val selectedSplitType = _uiState.value.availableSplitTypes
                    .find { it.id == event.splitTypeId } ?: return
                _uiState.update {
                    it.copy(
                        selectedSplitType = selectedSplitType,
                        splitError = null
                    )
                }
                recalculateSplits()
            }

            is AddExpenseUiEvent.SplitAmountChanged -> {
                handleExactAmountChanged(event.userId, event.amount)
            }

            is AddExpenseUiEvent.SplitPercentageChanged -> {
                handlePercentageChanged(event.userId, event.percentage)
            }

            is AddExpenseUiEvent.SplitExcludedToggled -> {
                val updatedSplits = _uiState.value.splits.map { split ->
                    if (split.userId == event.userId) {
                        split.copy(isExcluded = !split.isExcluded)
                    } else split
                }.toImmutableList()
                _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
                recalculateSplits()
            }
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
                        PaymentMethod.entries
                    )
                    val defaultPaymentMethod = mappedPaymentMethods.firstOrNull()

                    val mappedCategories = addExpenseUiMapper.mapCategories(
                        ExpenseCategory.entries
                    )
                    val defaultCategory = mappedCategories.find { it.id == ExpenseCategory.OTHER.name }
                        ?: mappedCategories.lastOrNull()

                    val mappedPaymentStatuses = addExpenseUiMapper.mapPaymentStatuses(
                        PaymentStatus.entries
                    )
                    val defaultPaymentStatus = mappedPaymentStatuses.find { it.id == PaymentStatus.FINISHED.name }
                        ?: mappedPaymentStatuses.firstOrNull()

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

                    // Map split types
                    val mappedSplitTypes = addExpenseUiMapper.mapSplitTypes(SplitType.entries)
                    val defaultSplitType = mappedSplitTypes.find { it.id == SplitType.EQUAL.name }
                        ?: mappedSplitTypes.firstOrNull()

                    // Initialize member splits
                    val memberIds = config.group.members
                    val initialSplits = addExpenseUiMapper.buildInitialSplits(
                        memberIds = memberIds,
                        shares = emptyList()
                    )

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
                            availableCategories = mappedCategories,
                            availablePaymentStatuses = mappedPaymentStatuses,
                            selectedCurrency = initialCurrency,
                            selectedPaymentMethod = defaultPaymentMethod,
                            selectedCategory = defaultCategory,
                            selectedPaymentStatus = defaultPaymentStatus,
                            showExchangeRateSection = isForeign,
                            exchangeRateLabel = exchangeRateLabel,
                            groupAmountLabel = groupAmountLabel,
                            availableSplitTypes = mappedSplitTypes,
                            selectedSplitType = defaultSplitType,
                            splits = initialSplits,
                            memberIds = memberIds.toImmutableList(),
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

    /**
     * Recalculates the per-user splits based on the current split type,
     * source amount, and active participants.
     *
     * - EQUAL: auto-calculates shares with currency display (e.g., "€16.67").
     * - EXACT: auto-distributes remainder evenly among users who haven't been edited.
     * - PERCENT: auto-distributes remaining percentage evenly among unedited users.
     */
    private fun recalculateSplits() {
        val state = _uiState.value
        val splitType = state.selectedSplitType?.let { SplitType.fromString(it.id) } ?: return
        val currencyCode = state.selectedCurrency?.code ?: "EUR"

        val activeParticipantIds = state.splits
            .filter { !it.isExcluded }
            .map { it.userId }
        if (activeParticipantIds.isEmpty()) return

        val sourceAmountCents = parseSourceAmountToCents()

        when (splitType) {
            SplitType.EQUAL -> recalculateEqualSplits(sourceAmountCents, activeParticipantIds, currencyCode)
            SplitType.EXACT -> recalculateExactSplits(sourceAmountCents, activeParticipantIds, currencyCode)
            SplitType.PERCENT -> recalculatePercentSplits(sourceAmountCents, activeParticipantIds, currencyCode)
        }
    }

    /**
     * EQUAL: Splits the total evenly, showing amounts with currency symbol (read-only).
     */
    private fun recalculateEqualSplits(
        sourceAmountCents: Long,
        activeParticipantIds: List<String>,
        currencyCode: String
    ) {
        if (sourceAmountCents <= 0) return

        try {
            val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
            val shares = calculator.calculateShares(sourceAmountCents, activeParticipantIds)

            val state = _uiState.value
            val updatedSplits = state.splits.map { uiModel ->
                val share = shares.find { it.userId == uiModel.userId }
                if (share != null && !uiModel.isExcluded) {
                    uiModel.copy(
                        amountCents = share.amountCents,
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(
                            share.amountCents, currencyCode
                        )
                    )
                } else if (uiModel.isExcluded) {
                    uiModel.copy(amountCents = 0L, formattedAmount = "")
                } else {
                    uiModel
                }
            }.toImmutableList()

            _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
        } catch (e: Exception) {
            Timber.w(e, "Failed to calculate equal splits")
        }
    }

    /**
     * EXACT: Auto-distributes the remaining amount evenly among the
     * other active members. The user who was just edited keeps their typed value.
     * Called from [recalculateSplits] when switching types (resets all to even).
     */
    private fun recalculateExactSplits(
        sourceAmountCents: Long,
        activeParticipantIds: List<String>,
        currencyCode: String
    ) {
        if (sourceAmountCents <= 0 || activeParticipantIds.isEmpty()) return

        // When switching to EXACT mode, start with an even split
        try {
            val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
            val shares = calculator.calculateShares(sourceAmountCents, activeParticipantIds)

            val state = _uiState.value
            val updatedSplits = state.splits.map { uiModel ->
                val share = shares.find { it.userId == uiModel.userId }
                if (share != null && !uiModel.isExcluded) {
                    uiModel.copy(
                        amountCents = share.amountCents,
                        amountInput = addExpenseUiMapper.formatCentsValue(share.amountCents),
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(
                            share.amountCents, currencyCode
                        )
                    )
                } else if (uiModel.isExcluded) {
                    uiModel.copy(amountCents = 0L, amountInput = "", formattedAmount = "")
                } else {
                    uiModel
                }
            }.toImmutableList()

            _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
        } catch (e: Exception) {
            Timber.w(e, "Failed to calculate initial exact splits")
        }
    }

    /**
     * PERCENT: Auto-distributes 100% evenly among active members.
     * Called from [recalculateSplits] when switching types (resets all to even).
     */
    private fun recalculatePercentSplits(
        sourceAmountCents: Long,
        activeParticipantIds: List<String>,
        currencyCode: String
    ) {
        if (activeParticipantIds.isEmpty()) return

        val totalPercent = BigDecimal("100")
        val count = activeParticipantIds.size
        val basePercent = totalPercent.divide(
            BigDecimal(count), 2, RoundingMode.DOWN
        )
        val allocatedPercent = basePercent.multiply(BigDecimal(count))
        var remainderCents = totalPercent.subtract(allocatedPercent)
            .movePointRight(2)
            .setScale(0, RoundingMode.DOWN)
            .toInt()
        val smallestUnit = BigDecimal("0.01")

        val state = _uiState.value
        val updatedSplits = state.splits.map { uiModel ->
            if (!uiModel.isExcluded && uiModel.userId in activeParticipantIds) {
                val pct = if (remainderCents > 0) {
                    remainderCents--
                    basePercent.add(smallestUnit)
                } else {
                    basePercent
                }
                // Calculate cents from percentage
                val amountCents = sourceAmountCents.toBigDecimal()
                    .multiply(pct)
                    .divide(totalPercent, 0, RoundingMode.DOWN)
                    .toLong()

                uiModel.copy(
                    percentageInput = addExpenseUiMapper.formatPercentageForDisplay(pct),
                    amountCents = amountCents,
                    formattedAmount = if (sourceAmountCents > 0) {
                        addExpenseUiMapper.formatCentsWithCurrency(amountCents, currencyCode)
                    } else ""
                )
            } else if (uiModel.isExcluded) {
                uiModel.copy(percentageInput = "", amountCents = 0L, formattedAmount = "")
            } else {
                uiModel
            }
        }.toImmutableList()

        _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
    }

    /**
     * Handles EXACT mode: user typed an amount for one member.
     * Auto-distributes the remaining amount evenly among the other active members.
     */
    private fun handleExactAmountChanged(editedUserId: String, typedAmount: String) {
        val state = _uiState.value
        val currencyCode = state.selectedCurrency?.code ?: "EUR"
        val sourceAmountCents = parseSourceAmountToCents()
        if (sourceAmountCents <= 0) {
            // Just store the typed value, nothing to distribute
            val updatedSplits = state.splits.map { split ->
                if (split.userId == editedUserId) split.copy(amountInput = typedAmount) else split
            }.toImmutableList()
            _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
            return
        }

        // Parse the typed amount to cents
        val typedCents = parseInputToCents(typedAmount)
        val remainingCents = (sourceAmountCents - typedCents).coerceAtLeast(0)

        // Other active (non-excluded) members
        val otherActiveIds = state.splits
            .filter { !it.isExcluded && it.userId != editedUserId }
            .map { it.userId }

        // Distribute remainder evenly among the others
        val otherShares = if (otherActiveIds.isNotEmpty() && remainingCents > 0) {
            try {
                val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
                calculator.calculateShares(remainingCents, otherActiveIds)
            } catch (_: Exception) { emptyList() }
        } else emptyList()

        val updatedSplits = state.splits.map { uiModel ->
            when {
                uiModel.userId == editedUserId && !uiModel.isExcluded -> {
                    uiModel.copy(
                        amountInput = typedAmount,
                        amountCents = typedCents,
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(typedCents, currencyCode)
                    )
                }
                !uiModel.isExcluded -> {
                    val share = otherShares.find { it.userId == uiModel.userId }
                    val cents = share?.amountCents ?: 0L
                    uiModel.copy(
                        amountCents = cents,
                        amountInput = addExpenseUiMapper.formatCentsValue(cents),
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(cents, currencyCode)
                    )
                }
                else -> uiModel // excluded — keep as-is
            }
        }.toImmutableList()

        _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
    }

    /**
     * Handles PERCENT mode: user typed a percentage for one member.
     * Auto-distributes the remaining percentage evenly among the other active members.
     */
    private fun handlePercentageChanged(editedUserId: String, typedPercentage: String) {
        val state = _uiState.value
        val currencyCode = state.selectedCurrency?.code ?: "EUR"
        val sourceAmountCents = parseSourceAmountToCents()

        val typedPct = parseInputToDecimal(typedPercentage)
        val hundred = BigDecimal("100")
        val remainingPct = hundred.subtract(typedPct).coerceAtLeast(BigDecimal.ZERO)

        val otherActiveIds = state.splits
            .filter { !it.isExcluded && it.userId != editedUserId }
            .map { it.userId }

        // Distribute remaining percentage evenly
        val otherCount = otherActiveIds.size
        val otherBasePct = if (otherCount > 0) {
            remainingPct.divide(BigDecimal(otherCount), 2, RoundingMode.DOWN)
        } else BigDecimal.ZERO

        // Remainder distribution for rounding
        val allocatedOtherPct = otherBasePct.multiply(BigDecimal(otherCount))
        var pctRemainder = remainingPct.subtract(allocatedOtherPct)
            .movePointRight(2)
            .setScale(0, RoundingMode.DOWN)
            .toInt()
        val smallestUnit = BigDecimal("0.01")

        val updatedSplits = state.splits.map { uiModel ->
            when {
                uiModel.userId == editedUserId && !uiModel.isExcluded -> {
                    val amountCents = if (sourceAmountCents > 0) {
                        sourceAmountCents.toBigDecimal()
                            .multiply(typedPct)
                            .divide(hundred, 0, RoundingMode.DOWN)
                            .toLong()
                    } else 0L
                    uiModel.copy(
                        percentageInput = typedPercentage,
                        amountCents = amountCents,
                        formattedAmount = if (sourceAmountCents > 0) {
                            addExpenseUiMapper.formatCentsWithCurrency(amountCents, currencyCode)
                        } else ""
                    )
                }
                !uiModel.isExcluded && uiModel.userId in otherActiveIds -> {
                    val pct = if (pctRemainder > 0) {
                        pctRemainder--
                        otherBasePct.add(smallestUnit)
                    } else {
                        otherBasePct
                    }
                    val amountCents = if (sourceAmountCents > 0) {
                        sourceAmountCents.toBigDecimal()
                            .multiply(pct)
                            .divide(hundred, 0, RoundingMode.DOWN)
                            .toLong()
                    } else 0L
                    uiModel.copy(
                        percentageInput = addExpenseUiMapper.formatPercentageForDisplay(pct),
                        amountCents = amountCents,
                        formattedAmount = if (sourceAmountCents > 0) {
                            addExpenseUiMapper.formatCentsWithCurrency(amountCents, currencyCode)
                        } else ""
                    )
                }
                else -> uiModel // excluded — keep as-is
            }
        }.toImmutableList()

        _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    /**
     * Parses the current source amount from UiState to cents.
     */
    private fun parseSourceAmountToCents(): Long {
        val state = _uiState.value
        return try {
            val normalized = CurrencyConverter.normalizeAmountString(state.sourceAmount.trim())
            val decimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
            val amount = normalized.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val multiplier = BigDecimal.TEN.pow(decimalPlaces)
            amount.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).toLong()
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Parses a locale-aware amount input string to cents (Long).
     * Uses the selected currency's decimal digits for correct conversion
     * (e.g., 0 for JPY, 2 for EUR, 3 for TND).
     */
    private fun parseInputToCents(input: String): Long {
        return try {
            val normalized = CurrencyConverter.normalizeAmountString(input.trim())
            val decimalPlaces = _uiState.value.selectedCurrency?.decimalDigits ?: 2
            val amount = normalized.toBigDecimalOrNull() ?: BigDecimal.ZERO
            amount.movePointRight(decimalPlaces).setScale(0, RoundingMode.HALF_UP).toLong()
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Parses a locale-aware decimal input string to BigDecimal.
     */
    private fun parseInputToDecimal(input: String): BigDecimal {
        return try {
            val normalized = CurrencyConverter.normalizeAmountString(input.trim())
            normalized.toBigDecimalOrNull() ?: BigDecimal.ZERO
        } catch (_: Exception) {
            BigDecimal.ZERO
        }
    }

    private fun BigDecimal.coerceAtLeast(min: BigDecimal): BigDecimal =
        if (this < min) min else this

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

        // Validate due date when payment status is SCHEDULED
        if (currentState.selectedPaymentStatus?.id == PaymentStatus.SCHEDULED.name && currentState.dueDateMillis == null) {
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
                viewModelScope.launch {
                    addExpenseUseCase(groupId, expense).onSuccess {
                        // Save the currency code specific to this group
                        _uiState.value.selectedCurrency?.code?.let { code ->
                            setGroupLastUsedCurrencyUseCase(groupId, code)
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
                                        e.requiredCents, cashCurrency
                                    )
                                    val available = addExpenseUiMapper.formatCentsForDisplay(
                                        e.availableCents, cashCurrency
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

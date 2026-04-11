package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.handler.AddOnEventHandler
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.handler.ConfigEventHandler
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.handler.CurrencyEventHandler
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.handler.FormEventHandler
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.handler.FormPostAction
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.handler.PostConfigAction
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.handler.SplitEventHandler
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.handler.SubmitEventHandler
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.handler.SubunitSplitEventHandler
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state.AddExpenseStep
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val configEventHandler: ConfigEventHandler,
    private val currencyEventHandler: CurrencyEventHandler,
    private val splitEventHandler: SplitEventHandler,
    private val subunitSplitEventHandler: SubunitSplitEventHandler,
    private val addOnEventHandler: AddOnEventHandler,
    private val submitEventHandler: SubmitEventHandler,
    private val formEventHandler: FormEventHandler
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
        addOnEventHandler.bind(_uiState, _actions, viewModelScope)
        submitEventHandler.bind(_uiState, _actions, viewModelScope)
        formEventHandler.bind(_uiState, _actions, viewModelScope)

        // Wire post-config callback: ViewModel routes cross-handler actions
        configEventHandler.setPostConfigCallback { action ->
            when (action) {
                is PostConfigAction.FetchRate ->
                    currencyEventHandler.fetchRate()

                is PostConfigAction.FetchCashRate ->
                    currencyEventHandler.fetchCashRate()

                is PostConfigAction.InitEntitySplits ->
                    subunitSplitEventHandler.initEntitySplits(
                        action.memberIds,
                        action.subunits,
                        action.memberProfiles
                    )

                is PostConfigAction.ClearEntitySplits ->
                    subunitSplitEventHandler.clearEntitySplits()
            }
        }

        // Wire post-form callback: ViewModel routes cross-handler actions
        formEventHandler.setFormPostCallback { action ->
            when (action) {
                is FormPostAction.RecalculateAfterAmount ->
                    recalculateAfterAmountChange(action.isExchangeRateLocked)

                is FormPostAction.PaymentMethodChanged ->
                    currencyEventHandler.handlePaymentMethodChanged(
                        action.isCash,
                        action.isGroupPocket
                    )

                is FormPostAction.FundingSourceChanged ->
                    currencyEventHandler.handleFundingSourceChanged(action.isGroupPocket)
            }
        }
    }

    // Thin router — every branch is a single delegation;
    // complexity is proportional to event count, not logic
    @Suppress("LongMethod", "CyclomaticComplexMethod")
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
                    addOnEventHandler.recalculateEffectiveTotal()
                }

            is AddExpenseUiEvent.ExchangeRateChanged ->
                currencyEventHandler.handleExchangeRateChanged(event.rate)

            is AddExpenseUiEvent.GroupAmountChanged ->
                currencyEventHandler.handleGroupAmountChanged(event.amount)

            // ── Splits ──────────────────────────────────────────────────
            is AddExpenseUiEvent.SplitTypeChanged -> {
                splitEventHandler.handleSplitTypeChanged(event.splitTypeId)
                subunitSplitEventHandler.recalculateEntitySplits()
            }

            is AddExpenseUiEvent.SplitAmountChanged ->
                splitEventHandler.handleExactAmountChanged(event.userId, event.amount)

            is AddExpenseUiEvent.SplitPercentageChanged ->
                splitEventHandler.handlePercentageChanged(event.userId, event.percentage)

            is AddExpenseUiEvent.SplitExcludedToggled ->
                splitEventHandler.handleSplitExcludedToggled(event.userId)

            is AddExpenseUiEvent.SplitShareLockToggled ->
                splitEventHandler.handleShareLockToggled(event.userId)

            // ── Subunit splits ────────────────────────────────────────────
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

            is AddExpenseUiEvent.EntityShareLockToggled ->
                subunitSplitEventHandler.handleEntityShareLockToggled(event.entityId)

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

            is AddExpenseUiEvent.IntraSubunitShareLockToggled ->
                subunitSplitEventHandler.handleIntraSubunitShareLockToggled(event.subunitId, event.userId)

            // ── Submission ──────────────────────────────────────────────
            is AddExpenseUiEvent.SubmitAddExpense ->
                submitEventHandler.submitExpense(event.groupId, onAddExpenseSuccess)

            // ── Simple form field updates (delegated to FormEventHandler) ──
            is AddExpenseUiEvent.TitleChanged ->
                formEventHandler.handleTitleChanged(event.title)

            is AddExpenseUiEvent.SourceAmountChanged ->
                formEventHandler.handleSourceAmountChanged(event.amount)

            is AddExpenseUiEvent.PaymentMethodSelected ->
                formEventHandler.handlePaymentMethodSelected(event.methodId)

            is AddExpenseUiEvent.FundingSourceSelected ->
                formEventHandler.handleFundingSourceSelected(event.fundingSourceId)

            is AddExpenseUiEvent.ContributionScopeSelected ->
                formEventHandler.handleContributionScopeSelected(event.scope, event.subunitId)

            is AddExpenseUiEvent.CategorySelected ->
                formEventHandler.handleCategorySelected(event.categoryId)

            is AddExpenseUiEvent.VendorChanged ->
                formEventHandler.handleVendorChanged(event.vendor)

            is AddExpenseUiEvent.NotesChanged ->
                formEventHandler.handleNotesChanged(event.notes)

            is AddExpenseUiEvent.PaymentStatusSelected ->
                formEventHandler.handlePaymentStatusSelected(event.statusId)

            is AddExpenseUiEvent.DueDateSelected ->
                formEventHandler.handleDueDateSelected(event.dateMillis)

            is AddExpenseUiEvent.ReceiptImageSelected ->
                formEventHandler.handleReceiptImageSelected(event.uri)

            is AddExpenseUiEvent.RemoveReceiptImage ->
                formEventHandler.handleRemoveReceiptImage()

            // ── Add-Ons ─────────────────────────────────────────────────
            is AddExpenseUiEvent.AddOnAdded ->
                addOnEventHandler.handleAddOnAdded(event.type)

            is AddExpenseUiEvent.AddOnRemoved ->
                addOnEventHandler.handleAddOnRemoved(event.addOnId)

            is AddExpenseUiEvent.AddOnTypeChanged ->
                addOnEventHandler.handleTypeChanged(event.addOnId, event.type)

            is AddExpenseUiEvent.AddOnModeChanged ->
                addOnEventHandler.handleModeChanged(event.addOnId, event.mode)

            is AddExpenseUiEvent.AddOnValueTypeChanged ->
                addOnEventHandler.handleValueTypeChanged(
                    event.addOnId,
                    event.valueType
                )

            is AddExpenseUiEvent.AddOnAmountChanged ->
                addOnEventHandler.handleAmountChanged(event.addOnId, event.amount)

            is AddExpenseUiEvent.AddOnCurrencySelected ->
                addOnEventHandler.handleCurrencySelected(
                    event.addOnId,
                    event.currencyCode
                )

            is AddExpenseUiEvent.AddOnPaymentMethodSelected ->
                addOnEventHandler.handlePaymentMethodSelected(
                    event.addOnId,
                    event.methodId
                )

            is AddExpenseUiEvent.AddOnDescriptionChanged ->
                addOnEventHandler.handleDescriptionChanged(
                    event.addOnId,
                    event.description
                )

            is AddExpenseUiEvent.AddOnExchangeRateChanged ->
                addOnEventHandler.handleExchangeRateChanged(
                    event.addOnId,
                    event.rate
                )

            is AddExpenseUiEvent.AddOnGroupAmountChanged ->
                addOnEventHandler.handleGroupAmountChanged(
                    event.addOnId,
                    event.amount
                )

            is AddExpenseUiEvent.AddOnsSectionToggled ->
                addOnEventHandler.handleSectionToggled()

            // ── Wizard Navigation ────────────────────────────────────────
            AddExpenseUiEvent.NextStep -> navigateNext()
            AddExpenseUiEvent.PreviousStep -> navigatePrevious()
            AddExpenseUiEvent.JumpToReview -> navigateToReview()
        }
    }

    /**
     * Orchestrates cross-handler recalculations after a source amount change.
     * Called via [FormPostAction.RecalculateAfterAmount].
     */
    private fun recalculateAfterAmountChange(isExchangeRateLocked: Boolean) {
        if (isExchangeRateLocked) {
            currencyEventHandler.recalculateCashForward()
        } else {
            currencyEventHandler.recalculateForward()
        }
        splitEventHandler.recalculateSplits()
        subunitSplitEventHandler.recalculateEntitySplits()
        addOnEventHandler.recalculateEffectiveTotal()
    }

    private fun navigateNext() {
        val state = _uiState.value
        val steps = state.applicableSteps
        val currentIndex = state.currentStepIndex
        if (currentIndex < steps.lastIndex) {
            _uiState.update { it.copy(currentStep = steps[currentIndex + 1]) }
        }
    }

    /**
     * Jumps directly from the current optional step to the REVIEW step.
     * Records the departure step so [navigatePrevious] can return to it.
     */
    private fun navigateToReview() {
        val state = _uiState.value
        if (!state.currentStep.isOptional) return
        _uiState.update {
            it.copy(
                currentStep = AddExpenseStep.REVIEW,
                jumpedFromStep = state.currentStep
            )
        }
    }

    private fun navigatePrevious() {
        val state = _uiState.value
        val steps = state.applicableSteps
        val currentIndex = state.currentStepIndex

        // If the user jumped to REVIEW, go back to the step they jumped from
        if (state.jumpedFromStep != null && state.isOnReviewStep) {
            _uiState.update {
                it.copy(
                    currentStep = state.jumpedFromStep,
                    jumpedFromStep = null
                )
            }
            return
        }

        if (currentIndex > 0) {
            _uiState.update { it.copy(currentStep = steps[currentIndex - 1], jumpedFromStep = null) }
        } else {
            // On first step — signal the Feature to pop the back stack
            viewModelScope.launch {
                _actions.emit(AddExpenseUiAction.NavigateBack)
            }
        }
    }
}

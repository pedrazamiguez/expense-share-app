package es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardNavigator
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.parseAmountToSmallestUnit
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.service.ContributionValidationService
import es.pedrazamiguez.splittrip.features.contribution.presentation.mapper.AddContributionUiMapper
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.action.AddContributionUiAction
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.event.AddContributionUiEvent
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.handler.ContributionConfigHandler
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.handler.ContributionSubmitHandler
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.state.AddContributionStep
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.state.AddContributionUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Thin router ViewModel for the Add Contribution wizard.
 *
 * All business logic is delegated to co-created handler classes:
 * - [ContributionConfigHandler] — group config loading, member selection
 * - [ContributionSubmitHandler] — validation & submission
 *
 * The ViewModel only manages wizard step navigation and lightweight
 * inline state updates.
 */
class AddContributionViewModel(
    private val configHandler: ContributionConfigHandler,
    private val submitHandler: ContributionSubmitHandler,
    private val contributionValidationService: ContributionValidationService,
    private val addContributionUiMapper: AddContributionUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddContributionUiState())
    val uiState: StateFlow<AddContributionUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<AddContributionUiAction>()
    val actions: SharedFlow<AddContributionUiAction> = _actions.asSharedFlow()

    private val wizardNavigator = WizardNavigator()

    /** The group ID set from the Feature layer. */
    private var currentGroupId: String? = null

    init {
        configHandler.bind(_uiState, _actions, viewModelScope)
        submitHandler.bind(_uiState, _actions, viewModelScope)
    }

    /**
     * Sets the group context from the Feature layer.
     *
     * Currency is applied **synchronously** so the symbol is visible on frame 1.
     * Group config (members, subunits) is loaded asynchronously.
     */
    fun setGroupContext(groupId: String?, currency: String?) {
        configHandler.setGroupCurrency(currency)
        if (groupId != currentGroupId) {
            currentGroupId = groupId
            configHandler.loadGroupConfig(groupId)
        }
    }

    fun onEvent(event: AddContributionUiEvent, onSuccess: () -> Unit = {}) {
        when (event) {
            is AddContributionUiEvent.UpdateAmount -> handleAmountChanged(event.amount)
            is AddContributionUiEvent.ContributionScopeSelected ->
                handleContributionScopeSelected(event.scope, event.subunitId)

            is AddContributionUiEvent.MemberSelected ->
                configHandler.handleMemberSelected(event.userId)

            is AddContributionUiEvent.Submit ->
                submitHandler.handleSubmit(currentGroupId, onSuccess)

            AddContributionUiEvent.NextStep -> handleNextStep()
            AddContributionUiEvent.PreviousStep -> handlePreviousStep()
        }
    }

    private fun handleAmountChanged(amount: String) {
        _uiState.update { it.copy(amountInput = amount, amountError = false) }
    }

    private fun handleNextStep() {
        val state = _uiState.value
        val steps = AddContributionStep.entries

        if (state.currentStep == AddContributionStep.AMOUNT) {
            val amountInSmallestUnit = parseAmountToSmallestUnit(
                state.amountInput,
                configHandler.groupCurrency
            )
            val validation = contributionValidationService.validateAmount(amountInSmallestUnit)
            if (validation is ContributionValidationService.ValidationResult.Invalid) {
                _uiState.update { it.copy(amountError = true) }
                return
            }
        }

        val nextStep = wizardNavigator.navigateNext(state.currentStep, steps) ?: return
        _uiState.update {
            it.copy(
                currentStep = nextStep,
                formattedAmountWithCurrency = if (nextStep == AddContributionStep.REVIEW) {
                    addContributionUiMapper.formatInputAmountWithCurrency(
                        it.amountInput,
                        configHandler.groupCurrency
                    )
                } else {
                    it.formattedAmountWithCurrency
                }
            )
        }
    }

    private fun handlePreviousStep() {
        val state = _uiState.value
        val steps = AddContributionStep.entries
        when (val result = wizardNavigator.navigatePrevious(state.currentStep, null, steps)) {
            is WizardNavigator.NavigationResult.Step ->
                _uiState.update { it.copy(currentStep = result.step) }

            is WizardNavigator.NavigationResult.JumpBack ->
                _uiState.update { it.copy(currentStep = result.step) }

            WizardNavigator.NavigationResult.ExitWizard ->
                viewModelScope.launch { _actions.emit(AddContributionUiAction.NavigateBack) }
        }
    }

    private fun handleContributionScopeSelected(scope: PayerType, subunitId: String?) {
        _uiState.update {
            it.copy(
                contributionScope = scope,
                selectedSubunitId = if (scope == PayerType.SUBUNIT) subunitId else null
            )
        }
    }
}

package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.ContributionValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddContributionUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.BalancesUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.SubunitOptionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.AddContributionUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddContributionUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddContributionUiState
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

class AddContributionViewModel(
    private val addContributionUseCase: AddContributionUseCase,
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val getGroupSubunitsUseCase: GetGroupSubunitsUseCase,
    private val authenticationService: AuthenticationService,
    private val contributionValidationService: ContributionValidationService,
    private val balancesUiMapper: BalancesUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddContributionUiState())
    val uiState: StateFlow<AddContributionUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<AddContributionUiAction>()
    val actions: SharedFlow<AddContributionUiAction> = _actions.asSharedFlow()

    private var groupCurrency: String = AppConstants.DEFAULT_CURRENCY_CODE

    fun onEvent(event: AddContributionUiEvent, onSuccess: () -> Unit = {}) {
        when (event) {
            is AddContributionUiEvent.LoadSubunitOptions -> loadSubunitOptions(event.groupId)
            is AddContributionUiEvent.UpdateAmount -> handleAmountChanged(event.amount)
            is AddContributionUiEvent.ContributionScopeSelected -> handleContributionScopeSelected(
                event.scope,
                event.subunitId
            )
            is AddContributionUiEvent.Submit -> handleSubmit(event.groupId, onSuccess)
        }
    }

    private fun loadSubunitOptions(groupId: String?) {
        if (groupId == null) return

        viewModelScope.launch {
            try {
                val group = getGroupByIdUseCase(groupId)
                groupCurrency = group?.currency ?: AppConstants.DEFAULT_CURRENCY_CODE

                val currentUserId = authenticationService.currentUserId()
                val subunits = getGroupSubunitsUseCase(groupId)

                val options = subunits
                    .filter { currentUserId != null && currentUserId in it.memberIds }
                    .map { SubunitOptionUiModel(id = it.id, name = it.name) }
                    .toImmutableList()

                // Reset form state when (re)loading options for a new group
                _uiState.update {
                    it.copy(
                        subunitOptions = options,
                        contributionScope = PayerType.USER,
                        selectedSubunitId = null,
                        amountInput = "",
                        amountError = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load subunit options for group $groupId")
                _uiState.update {
                    it.copy(
                        subunitOptions = it.subunitOptions,
                        contributionScope = PayerType.USER,
                        selectedSubunitId = null
                    )
                }
                _actions.emit(
                    AddContributionUiAction.ShowError(
                        UiText.StringResource(R.string.balances_add_money_error)
                    )
                )
            }
        }
    }

    private fun handleAmountChanged(amount: String) {
        _uiState.update { it.copy(amountInput = amount, amountError = false, error = null) }
    }

    private fun handleContributionScopeSelected(scope: PayerType, subunitId: String?) {
        _uiState.update {
            it.copy(
                contributionScope = scope,
                selectedSubunitId = if (scope == PayerType.SUBUNIT) subunitId else null,
                error = null
            )
        }
    }

    private fun handleSubmit(groupId: String?, onSuccess: () -> Unit) {
        if (groupId == null) return

        val state = _uiState.value
        val amountText = state.amountInput
        val amountInSmallestUnit = balancesUiMapper.parseAmountToSmallestUnit(amountText, groupCurrency)

        // Validate amount
        val amountValidation = contributionValidationService.validateAmount(amountInSmallestUnit)
        if (amountValidation is ContributionValidationService.ValidationResult.Invalid) {
            _uiState.update { it.copy(amountError = true) }
            return
        }

        // Validate subunit selection against loaded options (only for SUBUNIT scope)
        val selectedSubunitId = state.selectedSubunitId
        if (state.contributionScope == PayerType.SUBUNIT && selectedSubunitId != null) {
            val validSubunitIds = state.subunitOptions.map { it.id }.toSet()
            if (selectedSubunitId !in validSubunitIds) {
                viewModelScope.launch {
                    _actions.emit(
                        AddContributionUiAction.ShowError(
                            UiText.StringResource(R.string.balances_add_money_error_subunit)
                        )
                    )
                }
                return
            }
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val contribution = Contribution(
                    groupId = groupId,
                    contributionScope = state.contributionScope,
                    subunitId = selectedSubunitId,
                    amount = amountInSmallestUnit,
                    currency = groupCurrency
                )
                addContributionUseCase(groupId, contribution)
                _uiState.update { it.copy(isLoading = false) }
                _actions.emit(
                    AddContributionUiAction.ShowSuccess(
                        UiText.StringResource(R.string.balances_add_money_success)
                    )
                )
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Failed to add contribution")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = UiText.StringResource(R.string.balances_add_money_error)
                    )
                }
            }
        }
    }
}

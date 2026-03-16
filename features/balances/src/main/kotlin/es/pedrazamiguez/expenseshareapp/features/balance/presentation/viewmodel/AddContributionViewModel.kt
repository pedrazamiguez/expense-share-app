package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
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

    private var groupCurrency: String = "EUR"

    fun onEvent(event: AddContributionUiEvent, onSuccess: () -> Unit = {}) {
        when (event) {
            is AddContributionUiEvent.LoadSubunitOptions -> loadSubunitOptions(event.groupId)
            is AddContributionUiEvent.UpdateAmount -> handleAmountChanged(event.amount)
            is AddContributionUiEvent.SelectSubunit -> handleSubunitSelected(event.subunitId)
            is AddContributionUiEvent.Submit -> handleSubmit(event.groupId, onSuccess)
        }
    }

    private fun loadSubunitOptions(groupId: String?) {
        if (groupId == null) return

        viewModelScope.launch {
            val group = getGroupByIdUseCase(groupId)
            groupCurrency = group?.currency ?: "EUR"

            val currentUserId = authenticationService.currentUserId()
            val subunits = getGroupSubunitsUseCase(groupId)

            val options = subunits
                .filter { currentUserId != null && currentUserId in it.memberIds }
                .map { SubunitOptionUiModel(id = it.id, name = it.name) }
                .toImmutableList()

            _uiState.update { it.copy(subunitOptions = options) }
        }
    }

    private fun handleAmountChanged(amount: String) {
        _uiState.update { it.copy(amountInput = amount, amountError = false) }
    }

    private fun handleSubunitSelected(subunitId: String?) {
        _uiState.update { it.copy(selectedSubunitId = subunitId) }
    }

    private fun handleSubmit(groupId: String?, onSuccess: () -> Unit) {
        if (groupId == null) return

        val amountText = _uiState.value.amountInput
        val amountInSmallestUnit = balancesUiMapper.parseAmountToSmallestUnit(amountText, groupCurrency)

        val validationResult = contributionValidationService.validateAmount(amountInSmallestUnit)
        if (validationResult is ContributionValidationService.ValidationResult.Invalid) {
            _uiState.update { it.copy(amountError = true) }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val contribution = Contribution(
                    groupId = groupId,
                    subunitId = _uiState.value.selectedSubunitId,
                    amount = amountInSmallestUnit,
                    currency = groupCurrency
                )
                addContributionUseCase(groupId, contribution)
                _actions.emit(
                    AddContributionUiAction.ShowSuccess(
                        UiText.StringResource(R.string.balances_add_money_success)
                    )
                )
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Failed to add contribution")
                _uiState.update { it.copy(isLoading = false) }
                _actions.emit(
                    AddContributionUiAction.ShowError(
                        UiText.StringResource(R.string.balances_add_money_error)
                    )
                )
            }
        }
    }
}


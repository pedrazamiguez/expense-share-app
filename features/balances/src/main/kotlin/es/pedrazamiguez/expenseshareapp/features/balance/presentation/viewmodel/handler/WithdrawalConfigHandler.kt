package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.AddCashWithdrawalUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.SubunitOptionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.AddCashWithdrawalUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Handles group configuration loading events:
 * [LoadGroupConfig], [RetryLoadConfig].
 */
class WithdrawalConfigHandler(
    private val getGroupExpenseConfigUseCase: GetGroupExpenseConfigUseCase,
    private val getGroupSubunitsUseCase: GetGroupSubunitsUseCase,
    private val authenticationService: AuthenticationService,
    private val mapper: AddCashWithdrawalUiMapper
) : AddCashWithdrawalEventHandler {

    private lateinit var _uiState: MutableStateFlow<AddCashWithdrawalUiState>
    private lateinit var _actions: MutableSharedFlow<AddCashWithdrawalUiAction>
    private lateinit var scope: CoroutineScope

    override fun bind(
        stateFlow: MutableStateFlow<AddCashWithdrawalUiState>,
        actionsFlow: MutableSharedFlow<AddCashWithdrawalUiAction>,
        scope: CoroutineScope
    ) {
        _uiState = stateFlow
        _actions = actionsFlow
        this.scope = scope
    }

    fun loadGroupConfig(groupId: String?, forceRefresh: Boolean = false) {
        if (groupId == null) return

        val currentState = _uiState.value
        val isGroupChanged = currentState.loadedGroupId != groupId

        if (!forceRefresh && !isGroupChanged && currentState.isConfigLoaded) return

        scope.launch {
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
}

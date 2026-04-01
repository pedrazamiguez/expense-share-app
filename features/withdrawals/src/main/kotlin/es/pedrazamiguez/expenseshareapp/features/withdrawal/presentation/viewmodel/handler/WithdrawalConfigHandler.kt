package es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.MemberOptionUiModel
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.SubunitOptionUiModel
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.model.GroupExpenseConfig
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.withdrawal.R
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.mapper.AddCashWithdrawalUiMapper
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.action.AddCashWithdrawalUiAction
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.state.AddCashWithdrawalUiState
import kotlinx.collections.immutable.ImmutableList
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
    private val getMemberProfilesUseCase: GetMemberProfilesUseCase,
    private val authenticationService: AuthenticationService,
    private val addCashWithdrawalUiMapper: AddCashWithdrawalUiMapper
) : AddCashWithdrawalEventHandler {

    private lateinit var _uiState: MutableStateFlow<AddCashWithdrawalUiState>
    private lateinit var _actions: MutableSharedFlow<AddCashWithdrawalUiAction>
    private lateinit var scope: CoroutineScope

    /** Cached subunits for re-filtering on member change without re-fetching. */
    private var allSubunits: List<Subunit> = emptyList()

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
                .onSuccess { config -> applyLoadedConfig(groupId, config) }
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

    private suspend fun applyLoadedConfig(groupId: String, config: GroupExpenseConfig) {
        val mappedCurrencies = addCashWithdrawalUiMapper.mapCurrencies(config.availableCurrencies)
        val mappedGroupCurrency = addCashWithdrawalUiMapper.mapCurrency(config.groupCurrency)
        val deductedAmountLabel = addCashWithdrawalUiMapper.buildDeductedAmountLabel(mappedGroupCurrency)

        val (currentUserId, subunitOptions, memberOptions, selectedMemberId) =
            loadMemberAndSubunitData(groupId, config)

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
                subunitOptions = subunitOptions,
                withdrawalScope = PayerType.GROUP,
                selectedSubunitId = null,
                groupMembers = memberOptions,
                selectedMemberId = selectedMemberId,
                selectedMemberDisplayName = addCashWithdrawalUiMapper.resolveDisplayName(
                    selectedMemberId,
                    memberOptions
                ),
                error = null
            )
        }
    }

    private suspend fun loadMemberAndSubunitData(
        groupId: String,
        config: GroupExpenseConfig
    ): MemberSubunitResult {
        val currentUserId = authenticationService.currentUserId()

        allSubunits = runCatching {
            getGroupSubunitsUseCase(groupId)
        }.onFailure { e ->
            Timber.e(e, "Failed to load subunits for withdrawal config")
        }.getOrElse { emptyList() }

        val selectedMemberId = currentUserId
        val subunitOptions = filterSubunitsForMember(selectedMemberId)

        val memberProfiles: Map<String, User> = runCatching {
            getMemberProfilesUseCase(config.group.members)
        }.onFailure { e ->
            Timber.e(e, "Failed to load member profiles for withdrawal config")
        }.getOrElse { emptyMap() }

        val memberOptions = addCashWithdrawalUiMapper.toMemberOptions(
            memberIds = config.group.members,
            memberProfiles = memberProfiles,
            currentUserId = currentUserId
        )

        return MemberSubunitResult(currentUserId, subunitOptions, memberOptions, selectedMemberId)
    }

    /**
     * Filters cached subunits for the given member, returning only subunits the member belongs to.
     *
     * Public so the ViewModel can call this when a different member is selected (re-filter
     * without re-fetching from the use case).
     */
    fun filterSubunitsForMember(memberId: String?): ImmutableList<SubunitOptionUiModel> =
        allSubunits
            .filter { memberId != null && memberId in it.memberIds }
            .map { SubunitOptionUiModel(id = it.id, name = it.name) }
            .toImmutableList()
}

/**
 * Structured result from [WithdrawalConfigHandler.loadMemberAndSubunitData]
 * enabling destructuring at the call site.
 */
private data class MemberSubunitResult(
    val currentUserId: String?,
    val subunitOptions: ImmutableList<SubunitOptionUiModel>,
    val memberOptions: ImmutableList<MemberOptionUiModel>,
    val selectedMemberId: String?
)

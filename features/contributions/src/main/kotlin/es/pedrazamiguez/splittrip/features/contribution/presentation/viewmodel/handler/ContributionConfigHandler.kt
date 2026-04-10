package es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.core.common.constant.AppConstants
import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.SubunitOptionUiModel
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.splittrip.domain.usecase.subunit.GetGroupSubunitsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.splittrip.features.contribution.R
import es.pedrazamiguez.splittrip.features.contribution.presentation.mapper.AddContributionUiMapper
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.action.AddContributionUiAction
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.state.AddContributionUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Handles group configuration loading and member selection events.
 *
 * Owns [loadGroupConfig], [handleMemberSelected], and the cached
 * [groupCurrency] / [allSubunits] fields that other handlers may need.
 */
class ContributionConfigHandler(
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val getGroupSubunitsUseCase: GetGroupSubunitsUseCase,
    private val getMemberProfilesUseCase: GetMemberProfilesUseCase,
    private val authenticationService: AuthenticationService,
    private val addContributionUiMapper: AddContributionUiMapper
) : AddContributionEventHandler {

    private lateinit var _uiState: MutableStateFlow<AddContributionUiState>
    private lateinit var _actions: MutableSharedFlow<AddContributionUiAction>
    private lateinit var scope: CoroutineScope

    /** Cached subunits for re-filtering on member change without re-fetching. */
    private var allSubunits: List<Subunit> = emptyList()

    /** Current group currency code — set synchronously from SharedViewModel. */
    var groupCurrency: String = AppConstants.DEFAULT_CURRENCY_CODE
        private set

    /** The currently loaded group ID — used to avoid redundant reloads. */
    private var loadedGroupId: String? = null

    override fun bind(
        stateFlow: MutableStateFlow<AddContributionUiState>,
        actionsFlow: MutableSharedFlow<AddContributionUiAction>,
        scope: CoroutineScope
    ) {
        _uiState = stateFlow
        _actions = actionsFlow
        this.scope = scope
    }

    /**
     * Sets the group currency **synchronously** from the SharedViewModel-provided value.
     *
     * This is called from the Feature's `LaunchedEffect` immediately, before
     * [loadGroupConfig] completes, ensuring the currency symbol is visible on frame 1.
     */
    fun setGroupCurrency(currency: String?) {
        val resolvedCurrency = currency ?: AppConstants.DEFAULT_CURRENCY_CODE
        groupCurrency = resolvedCurrency
        val symbol = addContributionUiMapper.resolveCurrencySymbol(resolvedCurrency)
        Timber.d(
            "setGroupCurrency: input=%s, resolved=%s, symbol='%s'",
            currency,
            resolvedCurrency,
            symbol
        )
        _uiState.update {
            it.copy(
                groupCurrencyCode = resolvedCurrency,
                groupCurrencySymbol = symbol
            )
        }
    }

    /**
     * Loads group configuration (members, subunits) asynchronously.
     *
     * The currency symbol is already set via [setGroupCurrency] before this is called,
     * so only member/subunit data is loaded here.
     */
    fun loadGroupConfig(groupId: String?) {
        if (groupId == null) {
            Timber.d("loadGroupConfig: groupId is null, skipping")
            return
        }
        if (groupId == loadedGroupId) {
            Timber.d("loadGroupConfig: groupId=%s already loaded, skipping", groupId)
            return
        }
        Timber.d("loadGroupConfig: loading config for groupId=%s", groupId)

        scope.launch {
            try {
                val group = getGroupByIdUseCase(groupId)
                val currency = group?.currency ?: AppConstants.DEFAULT_CURRENCY_CODE
                Timber.d(
                    "loadGroupConfig: group=%s, loadedCurrency=%s, symbol='%s'",
                    group?.name,
                    currency,
                    addContributionUiMapper.resolveCurrencySymbol(currency)
                )
                groupCurrency = currency

                val currentUserId = authenticationService.currentUserId()
                allSubunits = getGroupSubunitsUseCase(groupId)

                val memberProfiles = getMemberProfilesUseCase(group?.members ?: emptyList())
                val memberOptions = addContributionUiMapper.toMemberOptions(
                    memberIds = group?.members ?: emptyList(),
                    memberProfiles = memberProfiles,
                    currentUserId = currentUserId
                )

                val selectedMemberId = currentUserId
                val subunitOptions = filterSubunitsForMember(selectedMemberId)

                loadedGroupId = groupId

                _uiState.update {
                    it.copy(
                        groupMembers = memberOptions,
                        selectedMemberId = selectedMemberId,
                        selectedMemberDisplayName = addContributionUiMapper.resolveDisplayName(
                            selectedMemberId,
                            memberOptions
                        ),
                        subunitOptions = subunitOptions,
                        contributionScope = PayerType.USER,
                        selectedSubunitId = null,
                        amountInput = "",
                        amountError = false,
                        groupCurrencyCode = currency,
                        groupCurrencySymbol = addContributionUiMapper.resolveCurrencySymbol(
                            currency
                        )
                    )
                }
                Timber.d(
                    "loadGroupConfig: DONE groupId=%s, final state code=%s symbol='%s'",
                    groupId,
                    _uiState.value.groupCurrencyCode,
                    _uiState.value.groupCurrencySymbol
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to load group config for group $groupId")
                _uiState.update {
                    it.copy(
                        contributionScope = PayerType.USER,
                        selectedSubunitId = null
                    )
                }
                _actions.emit(
                    AddContributionUiAction.ShowError(
                        UiText.StringResource(R.string.contribution_add_money_error)
                    )
                )
            }
        }
    }

    fun handleMemberSelected(userId: String) {
        val subunitOptions = filterSubunitsForMember(userId)

        _uiState.update {
            it.copy(
                selectedMemberId = userId,
                selectedMemberDisplayName = addContributionUiMapper.resolveDisplayName(
                    userId,
                    it.groupMembers
                ),
                subunitOptions = subunitOptions,
                contributionScope = PayerType.USER,
                selectedSubunitId = null
            )
        }
    }

    /**
     * Filters cached subunits for the given member, returning only subunits
     * the member belongs to.
     *
     * Public so the ViewModel can call this when a different member is selected
     * (re-filter without re-fetching from the use case).
     */
    fun filterSubunitsForMember(
        memberId: String?
    ): ImmutableList<SubunitOptionUiModel> =
        allSubunits
            .filter { memberId != null && memberId in it.memberIds }
            .map { SubunitOptionUiModel(id = it.id, name = it.name) }
            .toImmutableList()
}

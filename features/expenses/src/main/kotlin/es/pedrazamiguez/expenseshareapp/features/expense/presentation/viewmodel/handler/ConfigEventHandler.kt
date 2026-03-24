package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedPaymentMethodUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseOptionsMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseSplitMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Handles group configuration loading events:
 * [LoadGroupConfig], [RetryLoadConfig].
 *
 * Post-config actions (exchange rate fetching, entity split initialization)
 * are emitted via [postConfigCallback] and routed by the ViewModel to the
 * appropriate handler — avoiding handler-to-handler constructor coupling.
 */
class ConfigEventHandler(
    private val getGroupExpenseConfigUseCase: GetGroupExpenseConfigUseCase,
    private val getGroupLastUsedCurrencyUseCase: GetGroupLastUsedCurrencyUseCase,
    private val getGroupLastUsedPaymentMethodUseCase: GetGroupLastUsedPaymentMethodUseCase,
    private val getGroupLastUsedCategoryUseCase: GetGroupLastUsedCategoryUseCase,
    private val getMemberProfilesUseCase: GetMemberProfilesUseCase,
    private val addExpenseOptionsMapper: AddExpenseOptionsMapper,
    private val addExpenseSplitMapper: AddExpenseSplitMapper
) : AddExpenseEventHandler {

    private lateinit var _uiState: MutableStateFlow<AddExpenseUiState>
    private lateinit var _actions: MutableSharedFlow<AddExpenseUiAction>
    private lateinit var scope: CoroutineScope

    /**
     * Callback for post-config actions that require cross-handler communication.
     * Set by the ViewModel during initialization via [setPostConfigCallback].
     */
    private var postConfigCallback: ((PostConfigAction) -> Unit)? = null

    override fun bind(
        stateFlow: MutableStateFlow<AddExpenseUiState>,
        actionsFlow: MutableSharedFlow<AddExpenseUiAction>,
        scope: CoroutineScope
    ) {
        _uiState = stateFlow
        _actions = actionsFlow
        this.scope = scope
    }

    /**
     * Registers the callback the ViewModel uses to route post-config actions
     * to the appropriate sibling handlers.
     */
    fun setPostConfigCallback(callback: (PostConfigAction) -> Unit) {
        postConfigCallback = callback
    }

    fun loadGroupConfig(groupId: String?, forceRefresh: Boolean = false) {
        if (groupId == null) return

        val currentState = _uiState.value
        val isGroupChanged = currentState.loadedGroupId != groupId

        // Optimization: Don't reload if we already have data for the SAME group (e.g., on screen rotation)
        // unless forceRefresh is explicitly requested (e.g., retry after error)
        // Always reload if the groupId has changed
        if (!forceRefresh && !isGroupChanged && currentState.isConfigLoaded) return

        scope.launch {
            // Reset form state when loading a different group's config
            if (isGroupChanged) {
                _uiState.update {
                    AddExpenseUiState(isLoading = true, configLoadFailed = false)
                }
            } else {
                _uiState.update { it.copy(isLoading = true, configLoadFailed = false) }
            }

            getGroupExpenseConfigUseCase(groupId, forceRefresh).onSuccess { config ->
                // Grab the last used preferences for this specific group
                val lastUsedCode = getGroupLastUsedCurrencyUseCase(groupId).firstOrNull()
                val recentPaymentMethodIds =
                    getGroupLastUsedPaymentMethodUseCase(groupId).firstOrNull()
                        ?: emptyList()
                val recentCategoryIds =
                    getGroupLastUsedCategoryUseCase(groupId).firstOrNull()
                        ?: emptyList()

                // Map domain models to UI models
                val mappedCurrencies = addExpenseOptionsMapper.mapCurrencies(config.availableCurrencies)
                val mappedGroupCurrency = addExpenseOptionsMapper.mapCurrency(config.groupCurrency)
                val mappedPaymentMethods = addExpenseOptionsMapper.mapPaymentMethods(
                    PaymentMethod.entries
                )

                // Reorder payment methods: recent items first (in MRU order), then remaining
                val reorderedPaymentMethods =
                    reorderByRecent(mappedPaymentMethods, recentPaymentMethodIds) { it.id }

                // Auto-select the most recently used payment method, fallback to first
                val defaultPaymentMethod =
                    recentPaymentMethodIds.firstOrNull()?.let { lastId ->
                        reorderedPaymentMethods.find { it.id == lastId }
                    } ?: reorderedPaymentMethods.firstOrNull()

                val mappedCategories = addExpenseOptionsMapper.mapCategories(
                    ExpenseCategory.entries
                )

                // Reorder categories: recent items first (in MRU order), then remaining
                val reorderedCategories =
                    reorderByRecent(mappedCategories, recentCategoryIds) { it.id }

                // Auto-select the most recently used category, fallback to OTHER
                val defaultCategory =
                    recentCategoryIds.firstOrNull()?.let { lastId ->
                        reorderedCategories.find { it.id == lastId }
                    } ?: reorderedCategories.find { it.id == ExpenseCategory.OTHER.name }
                        ?: reorderedCategories.lastOrNull()

                val mappedPaymentStatuses = addExpenseOptionsMapper.mapPaymentStatuses(
                    PaymentStatus.entries
                )
                val defaultPaymentStatus =
                    mappedPaymentStatuses.find { it.id == PaymentStatus.FINISHED.name }
                        ?: mappedPaymentStatuses.firstOrNull()

                // Match against allowed currencies, fallback to default if not found or null
                val initialCurrencyDomain =
                    config.availableCurrencies.find { it.code == lastUsedCode }
                        ?: config.groupCurrency
                val initialCurrency = addExpenseOptionsMapper.mapCurrency(initialCurrencyDomain)

                val isForeign = initialCurrency.code != mappedGroupCurrency.code
                val exchangeRateLabel = if (isForeign) {
                    addExpenseOptionsMapper.buildExchangeRateLabel(
                        mappedGroupCurrency,
                        initialCurrency
                    )
                } else {
                    ""
                }
                val groupAmountLabel =
                    addExpenseOptionsMapper.buildGroupAmountLabel(mappedGroupCurrency)

                // Map split types
                val mappedSplitTypes = addExpenseOptionsMapper.mapSplitTypes(SplitType.entries)
                val defaultSplitType =
                    mappedSplitTypes.find { it.id == SplitType.EQUAL.name }
                        ?: mappedSplitTypes.firstOrNull()

                // Initialize member splits
                val memberIds = config.group.members
                val memberProfiles = getMemberProfilesUseCase(memberIds)
                val initialSplits = addExpenseSplitMapper.buildInitialSplits(
                    memberIds = memberIds,
                    shares = emptyList(),
                    memberProfiles = memberProfiles
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
                        paymentMethods = reorderedPaymentMethods,
                        availableCategories = reorderedCategories,
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

                // Emit post-config actions via callback — ViewModel routes to handlers
                if (isForeign) {
                    val isCash = defaultPaymentMethod?.let {
                        try {
                            PaymentMethod.fromString(it.id) == PaymentMethod.CASH
                        } catch (_: IllegalArgumentException) {
                            false
                        }
                    } ?: false

                    if (isCash) {
                        postConfigCallback?.invoke(PostConfigAction.FetchCashRate)
                    } else {
                        postConfigCallback?.invoke(PostConfigAction.FetchRate)
                    }
                }

                if (config.subunits.isNotEmpty()) {
                    postConfigCallback?.invoke(
                        PostConfigAction.InitEntitySplits(
                            memberIds,
                            config.subunits,
                            memberProfiles
                        )
                    )
                } else {
                    postConfigCallback?.invoke(PostConfigAction.ClearEntitySplits)
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
     * Reorders a list so that items matching [recentIds] appear first (in MRU order),
     * followed by the remaining items in their original order.
     */
    private fun <T> reorderByRecent(
        items: ImmutableList<T>,
        recentIds: List<String>,
        idSelector: (T) -> String
    ): ImmutableList<T> {
        if (recentIds.isEmpty()) return items
        val recentIdSet = recentIds.toSet()
        val recent = recentIds.mapNotNull { id -> items.find { idSelector(it) == id } }
        val rest = items.filter { idSelector(it) !in recentIdSet }
        return (recent + rest).toImmutableList()
    }
}

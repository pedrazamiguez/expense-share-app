package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.domain.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.GroupExpenseConfig
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedPaymentMethodUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseOptionsUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseSplitUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CategoryUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentMethodUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentStatusUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitTypeUiModel
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
    private val addExpenseOptionsMapper: AddExpenseOptionsUiMapper,
    private val addExpenseSplitMapper: AddExpenseSplitUiMapper
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

            val config = getGroupExpenseConfigUseCase(groupId, forceRefresh).getOrElse { e ->
                Timber.e(e, "Failed to load group configuration for groupId: $groupId")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isConfigLoaded = false,
                        configLoadFailed = true,
                        error = UiText.StringResource(R.string.expense_error_load_group_config)
                    )
                }
                return@launch
            }
            applyConfig(groupId, config)
        }
    }

    /**
     * Maps the loaded [GroupExpenseConfig] into UI state, resolves user preferences
     * (last-used currency, payment method, category), and emits post-config actions.
     */
    internal suspend fun applyConfig(
        groupId: String,
        config: GroupExpenseConfig
    ) {
        // Grab the last used preferences for this specific group
        val lastUsedCode = getGroupLastUsedCurrencyUseCase(groupId).firstOrNull()
        val recentPaymentMethodIds =
            getGroupLastUsedPaymentMethodUseCase(groupId).firstOrNull()
                ?: emptyList()
        val recentCategoryIds =
            getGroupLastUsedCategoryUseCase(groupId).firstOrNull()
                ?: emptyList()

        // Map and resolve all option defaults
        val defaults = resolveDefaultSelections(
            config,
            lastUsedCode,
            recentPaymentMethodIds,
            recentCategoryIds
        )

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
                groupCurrency = defaults.mappedGroupCurrency,
                availableCurrencies = defaults.mappedCurrencies,
                paymentMethods = defaults.reorderedPaymentMethods,
                availableCategories = defaults.reorderedCategories,
                availablePaymentStatuses = defaults.mappedPaymentStatuses,
                selectedCurrency = defaults.initialCurrency,
                selectedPaymentMethod = defaults.defaultPaymentMethod,
                selectedCategory = defaults.defaultCategory,
                selectedPaymentStatus = defaults.defaultPaymentStatus,
                showExchangeRateSection = defaults.isForeign,
                exchangeRateLabel = defaults.exchangeRateLabel,
                groupAmountLabel = defaults.groupAmountLabel,
                availableSplitTypes = defaults.mappedSplitTypes,
                selectedSplitType = defaults.defaultSplitType,
                splits = initialSplits,
                memberIds = memberIds.toImmutableList(),
                error = null
            ).withStepClamped()
        }

        emitPostConfigActions(
            defaults.isForeign,
            defaults.defaultPaymentMethod,
            config,
            memberIds,
            memberProfiles
        )
    }

    /**
     * Maps domain config into UI option lists and resolves default selections
     * based on last-used preferences (MRU reordering).
     */
    internal fun resolveDefaultSelections(
        config: GroupExpenseConfig,
        lastUsedCode: String?,
        recentPaymentMethodIds: List<String>,
        recentCategoryIds: List<String>
    ): ConfigDefaults {
        val mappedCurrencies = addExpenseOptionsMapper.mapCurrencies(config.availableCurrencies)
        val mappedGroupCurrency = addExpenseOptionsMapper.mapCurrency(config.groupCurrency)
        val mappedPaymentMethods = addExpenseOptionsMapper.mapPaymentMethods(
            PaymentMethod.entries
        )

        val reorderedPaymentMethods =
            reorderByRecent(mappedPaymentMethods, recentPaymentMethodIds) { it.id }
        val defaultPaymentMethod =
            recentPaymentMethodIds.firstOrNull()?.let { lastId ->
                reorderedPaymentMethods.find { it.id == lastId }
            } ?: reorderedPaymentMethods.firstOrNull()

        val mappedCategories = addExpenseOptionsMapper.mapCategories(ExpenseCategory.entries)
        val reorderedCategories =
            reorderByRecent(mappedCategories, recentCategoryIds) { it.id }
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

        val initialCurrencyDomain =
            config.availableCurrencies.find { it.code == lastUsedCode }
                ?: config.groupCurrency
        val initialCurrency = addExpenseOptionsMapper.mapCurrency(initialCurrencyDomain)

        val isForeign = initialCurrency.code != mappedGroupCurrency.code
        val exchangeRateLabel = if (isForeign) {
            addExpenseOptionsMapper.buildExchangeRateLabel(mappedGroupCurrency, initialCurrency)
        } else {
            ""
        }
        val groupAmountLabel = addExpenseOptionsMapper.buildGroupAmountLabel(mappedGroupCurrency)

        val mappedSplitTypes = addExpenseOptionsMapper.mapSplitTypes(SplitType.entries)
        val defaultSplitType =
            mappedSplitTypes.find { it.id == SplitType.EQUAL.name }
                ?: mappedSplitTypes.firstOrNull()

        return ConfigDefaults(
            mappedCurrencies = mappedCurrencies,
            mappedGroupCurrency = mappedGroupCurrency,
            reorderedPaymentMethods = reorderedPaymentMethods,
            defaultPaymentMethod = defaultPaymentMethod,
            reorderedCategories = reorderedCategories,
            defaultCategory = defaultCategory,
            mappedPaymentStatuses = mappedPaymentStatuses,
            defaultPaymentStatus = defaultPaymentStatus,
            initialCurrency = initialCurrency,
            isForeign = isForeign,
            exchangeRateLabel = exchangeRateLabel,
            groupAmountLabel = groupAmountLabel,
            mappedSplitTypes = mappedSplitTypes,
            defaultSplitType = defaultSplitType
        )
    }

    /**
     * Emits post-config actions via [postConfigCallback] — exchange rate fetching
     * and entity split initialization.
     */
    internal fun emitPostConfigActions(
        isForeign: Boolean,
        defaultPaymentMethod: PaymentMethodUiModel?,
        config: GroupExpenseConfig,
        memberIds: List<String>,
        memberProfiles: Map<String, User>
    ) {
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

    /**
     * Holds all mapped option lists and default selections resolved from
     * [GroupExpenseConfig] and user preferences.
     */
    internal data class ConfigDefaults(
        val mappedCurrencies: ImmutableList<CurrencyUiModel>,
        val mappedGroupCurrency: CurrencyUiModel,
        val reorderedPaymentMethods: ImmutableList<PaymentMethodUiModel>,
        val defaultPaymentMethod: PaymentMethodUiModel?,
        val reorderedCategories: ImmutableList<CategoryUiModel>,
        val defaultCategory: CategoryUiModel?,
        val mappedPaymentStatuses: ImmutableList<PaymentStatusUiModel>,
        val defaultPaymentStatus: PaymentStatusUiModel?,
        val initialCurrency: CurrencyUiModel,
        val isForeign: Boolean,
        val exchangeRateLabel: String,
        val groupAmountLabel: String,
        val mappedSplitTypes: ImmutableList<SplitTypeUiModel>,
        val defaultSplitType: SplitTypeUiModel?
    )
}

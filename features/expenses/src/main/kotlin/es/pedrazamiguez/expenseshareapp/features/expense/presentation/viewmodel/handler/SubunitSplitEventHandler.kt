package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory
import es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseSplitUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import java.math.BigDecimal
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Handles subunit-aware expense split events (Level 1 entity splits + Level 2 intra-subunit).
 *
 * Operates on [AddExpenseUiState.entitySplits] when [AddExpenseUiState.isSubunitMode] is true.
 * Each entity row is a [SplitUiModel] where:
 * - Solo users: [SplitUiModel.isEntityRow] = true, [SplitUiModel.entityMembers] is empty.
 * - Subunits: [SplitUiModel.isEntityRow] = true, [SplitUiModel.entityMembers] holds member rows.
 *
 * Level 2 (intra-subunit) recalculation is delegated to [IntraSubunitSplitDelegate].
 */
// 13 public event methods (one per UI event) + 6 private helpers — function count
// is proportional to the two-level split event surface
@Suppress("TooManyFunctions")
class SubunitSplitEventHandler(
    private val splitCalculatorFactory: ExpenseSplitCalculatorFactory,
    private val splitPreviewService: SplitPreviewService,
    private val addExpenseSplitMapper: AddExpenseSplitUiMapper,
    private val formattingHelper: FormattingHelper,
    private val intraSubunitSplitDelegate: IntraSubunitSplitDelegate
) : AddExpenseEventHandler {

    private lateinit var _uiState: MutableStateFlow<AddExpenseUiState>
    private lateinit var _actions: MutableSharedFlow<AddExpenseUiAction>
    private lateinit var scope: CoroutineScope

    /** Cached subunits for the current group — used by [IntraSubunitSplitDelegate]. */
    private var groupSubunits: List<Subunit> = emptyList()

    override fun bind(
        stateFlow: MutableStateFlow<AddExpenseUiState>,
        actionsFlow: MutableSharedFlow<AddExpenseUiAction>,
        scope: CoroutineScope
    ) {
        _uiState = stateFlow
        _actions = actionsFlow
        this.scope = scope
    }

    // ── Initialization ──────────────────────────────────────────────────

    /**
     * Builds the initial entity-level split rows from group members and subunits.
     * Called from [ConfigEventHandler] after loading the group config.
     */
    fun initEntitySplits(
        memberIds: List<String>,
        subunits: List<Subunit>,
        memberProfiles: Map<String, User> = emptyMap()
    ) {
        groupSubunits = subunits
        if (subunits.isEmpty()) return

        val subunitMemberIds = subunits.flatMap { it.memberIds }.toSet()
        val soloMemberIds = memberIds.filter { it !in subunitMemberIds }

        val defaultSplitType = _uiState.value.availableSplitTypes
            .find { it.id == SplitType.EQUAL.name }

        val entityRows = mutableListOf<SplitUiModel>()

        // Solo members — entity rows without nested members
        for (userId in soloMemberIds) {
            entityRows.add(
                SplitUiModel(
                    userId = userId,
                    displayName = addExpenseSplitMapper.resolveDisplayName(userId, memberProfiles),
                    isEntityRow = true
                )
            )
        }

        // Subunit entity rows with nested member rows
        for (subunit in subunits) {
            val memberRows = subunit.memberIds.map { memberId ->
                SplitUiModel(
                    userId = memberId,
                    displayName = addExpenseSplitMapper.resolveDisplayName(memberId, memberProfiles),
                    subunitId = subunit.id
                )
            }.sortedWith(
                compareBy(String.CASE_INSENSITIVE_ORDER) { it.displayName }
            ).toImmutableList()

            entityRows.add(
                SplitUiModel(
                    userId = subunit.id,
                    displayName = subunit.name,
                    isEntityRow = true,
                    entityMembers = memberRows,
                    entitySplitType = defaultSplitType
                )
            )
        }

        // Sort entity rows: solo members first, then subunits, alphabetically within each group
        val sortedEntityRows = entityRows
            .sortedWith(
                compareBy<SplitUiModel> { it.entityMembers.isNotEmpty() }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.displayName }
            )
            .toImmutableList()

        _uiState.update {
            it.copy(
                hasSubunits = true,
                entitySplits = sortedEntityRows
            )
        }
    }

    /**
     * Resets subunit state when the loaded group has no subunits.
     */
    fun clearEntitySplits() {
        groupSubunits = emptyList()
        _uiState.update {
            it.copy(
                hasSubunits = false,
                isSubunitMode = false,
                entitySplits = persistentListOf()
            )
        }
    }

    // ── Mode Toggle ─────────────────────────────────────────────────────

    fun handleSubunitModeToggled() {
        val newMode = !_uiState.value.isSubunitMode
        _uiState.update { it.copy(isSubunitMode = newMode, splitError = null) }
        if (newMode) {
            recalculateEntitySplits()
        }
    }

    // ── Accordion Toggle ────────────────────────────────────────────────

    fun handleAccordionToggled(entityId: String) {
        val updatedSplits = _uiState.value.entitySplits.map { entity ->
            if (entity.userId == entityId && entity.entityMembers.isNotEmpty()) {
                entity.copy(isExpanded = !entity.isExpanded)
            } else {
                entity
            }
        }.toImmutableList()
        _uiState.update { it.copy(entitySplits = updatedSplits) }
    }

    // ── Level 1: Entity-Level Events ────────────────────────────────────

    fun handleEntityExcludedToggled(entityId: String) {
        // Clear all entity locks on exclude toggle — redistribution resets
        // Also clear nested member locks to keep behavior consistent across levels
        val updatedSplits = _uiState.value.entitySplits.map { entity ->
            val clearedMembers = entity.entityMembers.map { it.copy(isShareLocked = false) }.toImmutableList()
            if (entity.userId == entityId) {
                entity.copy(
                    isExcluded = !entity.isExcluded,
                    isShareLocked = false,
                    entityMembers = clearedMembers
                )
            } else {
                entity.copy(isShareLocked = false, entityMembers = clearedMembers)
            }
        }.toImmutableList()
        _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
        recalculateEntitySplits()
    }

    fun handleEntityShareLockToggled(entityId: String) {
        val updatedSplits = _uiState.value.entitySplits.map { entity ->
            if (entity.userId == entityId) {
                entity.copy(isShareLocked = !entity.isShareLocked)
            } else {
                entity
            }
        }.toImmutableList()
        _uiState.update { it.copy(entitySplits = updatedSplits) }
    }

    // Entity-level exact amount edit + locked redistribution
    @Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod", "LongMethod")
    fun handleEntityAmountChanged(entityId: String, typedAmount: String) {
        val state = _uiState.value
        val currencyCode = state.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE
        val decimalDigits = state.selectedCurrency?.decimalDigits ?: 2
        val sourceAmountCents = parseSourceAmountToCents()

        if (sourceAmountCents <= 0) {
            val updatedSplits = state.entitySplits.map { entity ->
                if (entity.userId == entityId) {
                    entity.copy(amountInput = typedAmount, isShareLocked = true)
                } else {
                    entity
                }
            }.toImmutableList()
            _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
            return
        }

        val typedCents = splitPreviewService.parseAmountToCents(typedAmount, decimalDigits)

        val lockedCents = state.entitySplits
            .filter { it.isShareLocked && !it.isExcluded && it.userId != entityId }
            .sumOf { it.amountCents }

        val remainingCents = (sourceAmountCents - typedCents - lockedCents).coerceAtLeast(0)

        val otherActiveIds = state.entitySplits
            .filter { !it.isExcluded && it.userId != entityId && !it.isShareLocked }
            .map { it.userId }

        val otherSharesByEntityId = if (otherActiveIds.isNotEmpty() && remainingCents > 0) {
            try {
                val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
                calculator.calculateShares(remainingCents, otherActiveIds)
                    .associateBy { it.userId }
            } catch (_: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }

        val updatedSplits = state.entitySplits.map { entity ->
            when {
                entity.userId == entityId && !entity.isExcluded -> {
                    val updated = entity.copy(
                        amountInput = typedAmount,
                        amountCents = typedCents,
                        isShareLocked = true,
                        formattedAmount = formattingHelper.formatCentsWithCurrency(typedCents, currencyCode)
                    )
                    delegateIntraRecalculation(updated, currencyCode)
                }
                entity.isShareLocked && !entity.isExcluded -> {
                    entity
                }
                !entity.isExcluded -> {
                    val share = otherSharesByEntityId[entity.userId]
                    val cents = share?.amountCents ?: 0L
                    val updated = entity.copy(
                        amountCents = cents,
                        amountInput = formattingHelper.formatCentsValue(cents, decimalDigits),
                        formattedAmount = formattingHelper.formatCentsWithCurrency(cents, currencyCode)
                    )
                    delegateIntraRecalculation(updated, currencyCode)
                }
                else -> entity
            }
        }.toImmutableList()

        _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
    }

    // Entity-level percentage edit + locked redistribution
    @Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod")
    fun handleEntityPercentageChanged(entityId: String, typedPercentage: String) {
        val state = _uiState.value
        val currencyCode = state.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE
        val sourceAmountCents = parseSourceAmountToCents()

        val typedPct = splitPreviewService.parseToDecimal(typedPercentage)

        val otherActiveIds = state.entitySplits
            .filter { !it.isExcluded && it.userId != entityId }
            .map { it.userId }

        val lockedPercentages = state.entitySplits
            .filter { it.isShareLocked && !it.isExcluded && it.userId != entityId && it.userId in otherActiveIds }
            .associate { it.userId to splitPreviewService.parseToDecimal(it.percentageInput) }

        val editedAmountCents = splitPreviewService.calculateAmountFromPercentage(typedPct, sourceAmountCents)
        val otherSharesByEntityId = splitPreviewService.redistributeRemainingPercentage(
            typedPct,
            sourceAmountCents,
            otherActiveIds,
            lockedPercentages
        ).associateBy { it.userId }

        val updatedSplits = state.entitySplits.map { entity ->
            when {
                entity.userId == entityId && !entity.isExcluded -> {
                    val updated = entity.copy(
                        percentageInput = typedPercentage,
                        amountCents = editedAmountCents,
                        isShareLocked = true,
                        formattedAmount = if (sourceAmountCents > 0) {
                            formattingHelper.formatCentsWithCurrency(editedAmountCents, currencyCode)
                        } else {
                            ""
                        }
                    )
                    delegateIntraRecalculation(updated, currencyCode)
                }
                entity.isShareLocked && !entity.isExcluded && entity.userId in otherActiveIds.toSet() -> {
                    entity
                }
                !entity.isExcluded && entity.userId in otherActiveIds.toSet() -> {
                    val share = otherSharesByEntityId[entity.userId]
                    val pct = share?.percentage ?: BigDecimal.ZERO
                    val amountCents = share?.amountCents ?: 0L
                    val updated = entity.copy(
                        percentageInput = formattingHelper.formatPercentageForDisplay(pct),
                        amountCents = amountCents,
                        formattedAmount = if (sourceAmountCents > 0) {
                            formattingHelper.formatCentsWithCurrency(amountCents, currencyCode)
                        } else {
                            ""
                        }
                    )
                    delegateIntraRecalculation(updated, currencyCode)
                }
                else -> entity
            }
        }.toImmutableList()

        _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
    }

    // ── Level 2: Intra-Subunit Events ──────────────────────────────────

    fun handleIntraSubunitSplitTypeChanged(subunitId: String, splitTypeId: String) {
        val selectedType = _uiState.value.availableSplitTypes.find { it.id == splitTypeId } ?: return
        val currencyCode = _uiState.value.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE

        val updatedSplits = _uiState.value.entitySplits.map { entity ->
            if (entity.userId == subunitId) {
                val clearedMembers = entity.entityMembers.map { it.copy(isShareLocked = false) }.toImmutableList()
                val updated = entity.copy(entitySplitType = selectedType, entityMembers = clearedMembers)
                delegateIntraRecalculation(updated, currencyCode)
            } else {
                entity
            }
        }.toImmutableList()

        _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
    }

    // Intra-subunit exact amount edit + locked redistribution
    @Suppress("CognitiveComplexMethod")
    fun handleIntraSubunitAmountChanged(subunitId: String, userId: String, typedAmount: String) {
        val state = _uiState.value
        val currencyCode = state.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE
        val decimalDigits = state.selectedCurrency?.decimalDigits ?: 2

        val updatedSplits = state.entitySplits.map { entity ->
            if (entity.userId == subunitId) {
                val subunitTotalCents = entity.amountCents
                val typedCents = splitPreviewService.parseAmountToCents(typedAmount, decimalDigits)

                val lockedCents = entity.entityMembers
                    .filter { it.isShareLocked && it.userId != userId }
                    .sumOf { it.amountCents }

                val remainingCents = (subunitTotalCents - typedCents - lockedCents).coerceAtLeast(0)

                val otherMemberIds = entity.entityMembers
                    .filter { it.userId != userId && !it.isShareLocked }
                    .map { it.userId }

                val otherSharesMap = if (otherMemberIds.isNotEmpty() && remainingCents > 0) {
                    try {
                        val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
                        calculator.calculateShares(remainingCents, otherMemberIds)
                            .associateBy { it.userId }
                    } catch (_: Exception) {
                        emptyMap()
                    }
                } else {
                    emptyMap()
                }

                val updatedMembers = entity.entityMembers.map { member ->
                    when {
                        member.userId == userId -> member.copy(
                            amountInput = typedAmount,
                            amountCents = typedCents,
                            isShareLocked = true,
                            formattedAmount = formattingHelper.formatCentsWithCurrency(typedCents, currencyCode)
                        )
                        member.isShareLocked -> member
                        else -> {
                            val share = otherSharesMap[member.userId]
                            val cents = share?.amountCents ?: 0L
                            member.copy(
                                amountCents = cents,
                                amountInput = formattingHelper.formatCentsValue(cents, decimalDigits),
                                formattedAmount = formattingHelper.formatCentsWithCurrency(cents, currencyCode)
                            )
                        }
                    }
                }.toImmutableList()

                entity.copy(entityMembers = updatedMembers)
            } else {
                entity
            }
        }.toImmutableList()

        _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
    }

    // Intra-subunit percentage edit + locked redistribution
    @Suppress("CognitiveComplexMethod")
    fun handleIntraSubunitPercentageChanged(subunitId: String, userId: String, typedPercentage: String) {
        val state = _uiState.value
        val currencyCode = state.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE

        val updatedSplits = state.entitySplits.map { entity ->
            if (entity.userId == subunitId) {
                val subunitTotalCents = entity.amountCents
                val typedPct = splitPreviewService.parseToDecimal(typedPercentage)

                val otherMemberIds = entity.entityMembers
                    .filter { it.userId != userId }
                    .map { it.userId }

                val lockedPercentages = entity.entityMembers
                    .filter { it.isShareLocked && it.userId != userId && it.userId in otherMemberIds }
                    .associate { it.userId to splitPreviewService.parseToDecimal(it.percentageInput) }

                val editedAmountCents = splitPreviewService.calculateAmountFromPercentage(typedPct, subunitTotalCents)
                val otherSharesMap = splitPreviewService.redistributeRemainingPercentage(
                    typedPct,
                    subunitTotalCents,
                    otherMemberIds,
                    lockedPercentages
                ).associateBy { it.userId }

                val updatedMembers = entity.entityMembers.map { member ->
                    when {
                        member.userId == userId -> member.copy(
                            percentageInput = typedPercentage,
                            amountCents = editedAmountCents,
                            isShareLocked = true,
                            formattedAmount = if (subunitTotalCents > 0) {
                                formattingHelper.formatCentsWithCurrency(editedAmountCents, currencyCode)
                            } else {
                                ""
                            }
                        )
                        member.isShareLocked && member.userId in otherMemberIds.toSet() -> {
                            member
                        }
                        member.userId in otherMemberIds.toSet() -> {
                            val share = otherSharesMap[member.userId]
                            val pct = share?.percentage ?: BigDecimal.ZERO
                            val amountCents = share?.amountCents ?: 0L
                            member.copy(
                                percentageInput = formattingHelper.formatPercentageForDisplay(pct),
                                amountCents = amountCents,
                                formattedAmount = if (subunitTotalCents > 0) {
                                    formattingHelper.formatCentsWithCurrency(amountCents, currencyCode)
                                } else {
                                    ""
                                }
                            )
                        }
                        else -> member
                    }
                }.toImmutableList()

                entity.copy(entityMembers = updatedMembers)
            } else {
                entity
            }
        }.toImmutableList()

        _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
    }

    fun handleIntraSubunitShareLockToggled(subunitId: String, userId: String) {
        val updatedSplits = _uiState.value.entitySplits.map { entity ->
            if (entity.userId == subunitId) {
                val updatedMembers = entity.entityMembers.map { member ->
                    if (member.userId == userId) {
                        member.copy(isShareLocked = !member.isShareLocked)
                    } else {
                        member
                    }
                }.toImmutableList()
                entity.copy(entityMembers = updatedMembers)
            } else {
                entity
            }
        }.toImmutableList()
        _uiState.update { it.copy(entitySplits = updatedSplits) }
    }

    // ── Recalculation (cross-handler) ───────────────────────────────────

    /**
     * Recalculates entity-level splits based on the current split type and source amount.
     * Called when source amount changes, split type changes, or subunit mode is toggled.
     */
    fun recalculateEntitySplits() {
        val state = _uiState.value
        if (!state.isSubunitMode) return
        if (state.entitySplits.isEmpty()) return

        val splitType = state.selectedSplitType?.let { SplitType.fromString(it.id) } ?: return
        val currencyCode = state.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE
        val sourceAmountCents = parseSourceAmountToCents()

        val activeEntityIds = state.entitySplits
            .filter { !it.isExcluded }
            .map { it.userId }
        if (activeEntityIds.isEmpty()) return

        when (splitType) {
            SplitType.EQUAL -> recalculateEqualEntitySplits(sourceAmountCents, activeEntityIds, currencyCode)
            SplitType.EXACT -> recalculateExactEntitySplits(sourceAmountCents, activeEntityIds, currencyCode)
            SplitType.PERCENT -> recalculatePercentEntitySplits(sourceAmountCents, activeEntityIds, currencyCode)
        }
    }

    // ── Private: Entity-level recalculation ─────────────────────────────

    private fun recalculateEqualEntitySplits(
        sourceAmountCents: Long,
        activeEntityIds: List<String>,
        currencyCode: String
    ) {
        if (sourceAmountCents <= 0) return

        try {
            val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
            val sharesByEntityId = calculator.calculateShares(sourceAmountCents, activeEntityIds)
                .associateBy { it.userId }

            val updatedSplits = _uiState.value.entitySplits.map { entity ->
                val share = sharesByEntityId[entity.userId]
                if (share != null && !entity.isExcluded) {
                    val updated = entity.copy(
                        amountCents = share.amountCents,
                        formattedAmount = formattingHelper.formatCentsWithCurrency(share.amountCents, currencyCode)
                    )
                    delegateIntraRecalculation(updated, currencyCode)
                } else if (entity.isExcluded) {
                    entity.copy(amountCents = 0L, formattedAmount = "")
                } else {
                    entity
                }
            }.toImmutableList()

            _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
        } catch (e: Exception) {
            Timber.w(e, "Failed to calculate equal entity splits")
        }
    }

    private fun recalculateExactEntitySplits(
        sourceAmountCents: Long,
        activeEntityIds: List<String>,
        currencyCode: String
    ) {
        if (sourceAmountCents <= 0 || activeEntityIds.isEmpty()) return

        try {
            val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
            val sharesByEntityId = calculator.calculateShares(sourceAmountCents, activeEntityIds)
                .associateBy { it.userId }

            val decimalDigits = _uiState.value.selectedCurrency?.decimalDigits ?: 2
            val updatedSplits = _uiState.value.entitySplits.map { entity ->
                val share = sharesByEntityId[entity.userId]
                if (share != null && !entity.isExcluded) {
                    val updated = entity.copy(
                        amountCents = share.amountCents,
                        amountInput = formattingHelper.formatCentsValue(share.amountCents, decimalDigits),
                        formattedAmount = formattingHelper.formatCentsWithCurrency(share.amountCents, currencyCode)
                    )
                    delegateIntraRecalculation(updated, currencyCode)
                } else if (entity.isExcluded) {
                    entity.copy(amountCents = 0L, amountInput = "", formattedAmount = "")
                } else {
                    entity
                }
            }.toImmutableList()

            _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
        } catch (e: Exception) {
            Timber.w(e, "Failed to calculate exact entity splits")
        }
    }

    private fun recalculatePercentEntitySplits(
        sourceAmountCents: Long,
        activeEntityIds: List<String>,
        currencyCode: String
    ) {
        if (activeEntityIds.isEmpty()) return

        val sharesByEntityId = splitPreviewService.distributePercentagesEvenly(
            sourceAmountCents,
            activeEntityIds
        ).associateBy { it.userId }

        val updatedSplits = _uiState.value.entitySplits.map { entity ->
            val share = sharesByEntityId[entity.userId]
            if (!entity.isExcluded && share != null) {
                val pct = share.percentage ?: BigDecimal.ZERO
                val updated = entity.copy(
                    percentageInput = formattingHelper.formatPercentageForDisplay(pct),
                    amountCents = share.amountCents,
                    formattedAmount = if (sourceAmountCents > 0) {
                        formattingHelper.formatCentsWithCurrency(share.amountCents, currencyCode)
                    } else {
                        ""
                    }
                )
                delegateIntraRecalculation(updated, currencyCode)
            } else if (entity.isExcluded) {
                entity.copy(percentageInput = "", amountCents = 0L, formattedAmount = "")
            } else {
                entity
            }
        }.toImmutableList()

        _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
    }

    // ── Private helpers ─────────────────────────────────────────────────

    /**
     * Delegates intra-subunit recalculation to [IntraSubunitSplitDelegate].
     * Passes cached [groupSubunits] and the current currency's decimal digits.
     */
    private fun delegateIntraRecalculation(entity: SplitUiModel, currencyCode: String): SplitUiModel {
        val decimalDigits = _uiState.value.selectedCurrency?.decimalDigits ?: 2
        return intraSubunitSplitDelegate.recalculate(entity, currencyCode, groupSubunits, decimalDigits)
    }

    private fun parseSourceAmountToCents(): Long {
        val state = _uiState.value
        val decimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
        return intraSubunitSplitDelegate.parseSourceAmountToCents(state.sourceAmount, decimalPlaces)
    }
}

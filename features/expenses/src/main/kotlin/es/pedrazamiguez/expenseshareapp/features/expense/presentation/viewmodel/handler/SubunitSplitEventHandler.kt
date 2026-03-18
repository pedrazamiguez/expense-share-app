package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory
import es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService
import es.pedrazamiguez.expenseshareapp.domain.service.split.SubunitAwareSplitService
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import java.math.BigDecimal
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Handles sub-unit-aware expense split events (Level 1 entity splits + Level 2 intra-sub-unit).
 *
 * Operates on [AddExpenseUiState.entitySplits] when [AddExpenseUiState.isSubunitMode] is true.
 * Each entity row is a [SplitUiModel] where:
 * - Solo users: [SplitUiModel.isEntityRow] = true, [SplitUiModel.entityMembers] is empty.
 * - Sub-units: [SplitUiModel.isEntityRow] = true, [SplitUiModel.entityMembers] holds member rows.
 *
 * **Delegation strategy:**
 * - [SplitPreviewService]: percentage distribution, amount parsing, and percent→cents conversion.
 * - [SubunitAwareSplitService]: proportional member-share distribution (DOWN rounding + remainder).
 * - [ExpenseSplitCalculatorFactory]: equal/exact split calculation at both entity and member level.
 * - [AddExpenseUiMapper]: all locale-aware formatting (amounts, percentages, currency symbols).
 */
class SubunitSplitEventHandler(
    private val splitCalculatorFactory: ExpenseSplitCalculatorFactory,
    private val splitPreviewService: SplitPreviewService,
    private val subunitAwareSplitService: SubunitAwareSplitService,
    private val addExpenseUiMapper: AddExpenseUiMapper
) : AddExpenseEventHandler {

    private lateinit var _uiState: MutableStateFlow<AddExpenseUiState>
    private lateinit var _actions: MutableSharedFlow<AddExpenseUiAction>
    private lateinit var scope: CoroutineScope

    /** Cached sub-units for the current group — used to look up [Subunit.memberShares]. */
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
     * Builds the initial entity-level split rows from group members and sub-units.
     * Called from [ConfigEventHandler] after loading the group config.
     */
    fun initEntitySplits(memberIds: List<String>, subunits: List<Subunit>) {
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
                    displayName = userId,
                    isEntityRow = true
                )
            )
        }

        // Sub-unit entity rows with nested member rows
        for (subunit in subunits) {
            val memberRows = subunit.memberIds.map { memberId ->
                SplitUiModel(
                    userId = memberId,
                    displayName = memberId,
                    subunitId = subunit.id
                )
            }.toImmutableList()

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

        _uiState.update {
            it.copy(
                hasSubunits = true,
                entitySplits = entityRows.toImmutableList()
            )
        }
    }

    /**
     * Resets sub-unit state when the loaded group has no sub-units.
     * Prevents stale [AddExpenseUiState.hasSubunits] / [AddExpenseUiState.isSubunitMode] /
     * [AddExpenseUiState.entitySplits] from a previous group load.
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
        val updatedSplits = _uiState.value.entitySplits.map { entity ->
            if (entity.userId == entityId) {
                entity.copy(isExcluded = !entity.isExcluded)
            } else {
                entity
            }
        }.toImmutableList()
        _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
        recalculateEntitySplits()
    }

    fun handleEntityAmountChanged(entityId: String, typedAmount: String) {
        val state = _uiState.value
        val currencyCode = state.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE
        val decimalDigits = state.selectedCurrency?.decimalDigits ?: 2
        val sourceAmountCents = parseSourceAmountToCents()

        if (sourceAmountCents <= 0) {
            val updatedSplits = state.entitySplits.map { entity ->
                if (entity.userId == entityId) entity.copy(amountInput = typedAmount) else entity
            }.toImmutableList()
            _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
            return
        }

        val typedCents = splitPreviewService.parseAmountToCents(typedAmount, decimalDigits)
        val remainingCents = (sourceAmountCents - typedCents).coerceAtLeast(0)

        val otherActiveIds = state.entitySplits
            .filter { !it.isExcluded && it.userId != entityId }
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
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(typedCents, currencyCode)
                    )
                    recalculateIntraSubunitSplits(updated, currencyCode)
                }
                !entity.isExcluded -> {
                    val share = otherSharesByEntityId[entity.userId]
                    val cents = share?.amountCents ?: 0L
                    val updated = entity.copy(
                        amountCents = cents,
                        amountInput = addExpenseUiMapper.formatCentsValue(cents, decimalDigits),
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(cents, currencyCode)
                    )
                    recalculateIntraSubunitSplits(updated, currencyCode)
                }
                else -> entity
            }
        }.toImmutableList()

        _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
    }

    fun handleEntityPercentageChanged(entityId: String, typedPercentage: String) {
        val state = _uiState.value
        val currencyCode = state.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE
        val sourceAmountCents = parseSourceAmountToCents()

        val typedPct = splitPreviewService.parseToDecimal(typedPercentage)

        val otherActiveIds = state.entitySplits
            .filter { !it.isExcluded && it.userId != entityId }
            .map { it.userId }

        val editedAmountCents = splitPreviewService.calculateAmountFromPercentage(typedPct, sourceAmountCents)
        val otherSharesByEntityId = splitPreviewService.redistributeRemainingPercentage(
            typedPct, sourceAmountCents, otherActiveIds
        ).associateBy { it.userId }

        val updatedSplits = state.entitySplits.map { entity ->
            when {
                entity.userId == entityId && !entity.isExcluded -> {
                    val updated = entity.copy(
                        percentageInput = typedPercentage,
                        amountCents = editedAmountCents,
                        formattedAmount = if (sourceAmountCents > 0) {
                            addExpenseUiMapper.formatCentsWithCurrency(editedAmountCents, currencyCode)
                        } else ""
                    )
                    recalculateIntraSubunitSplits(updated, currencyCode)
                }
                !entity.isExcluded && entity.userId in otherActiveIds.toSet() -> {
                    val share = otherSharesByEntityId[entity.userId]
                    val pct = share?.percentage ?: BigDecimal.ZERO
                    val amountCents = share?.amountCents ?: 0L
                    val updated = entity.copy(
                        percentageInput = addExpenseUiMapper.formatPercentageForDisplay(pct),
                        amountCents = amountCents,
                        formattedAmount = if (sourceAmountCents > 0) {
                            addExpenseUiMapper.formatCentsWithCurrency(amountCents, currencyCode)
                        } else ""
                    )
                    recalculateIntraSubunitSplits(updated, currencyCode)
                }
                else -> entity
            }
        }.toImmutableList()

        _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
    }

    // ── Level 2: Intra-Sub-Unit Events ──────────────────────────────────

    fun handleIntraSubunitSplitTypeChanged(subunitId: String, splitTypeId: String) {
        val selectedType = _uiState.value.availableSplitTypes.find { it.id == splitTypeId } ?: return
        val currencyCode = _uiState.value.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE

        val updatedSplits = _uiState.value.entitySplits.map { entity ->
            if (entity.userId == subunitId) {
                val updated = entity.copy(entitySplitType = selectedType)
                recalculateIntraSubunitSplits(updated, currencyCode)
            } else {
                entity
            }
        }.toImmutableList()

        _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
    }

    fun handleIntraSubunitAmountChanged(subunitId: String, userId: String, typedAmount: String) {
        val state = _uiState.value
        val currencyCode = state.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE
        val decimalDigits = state.selectedCurrency?.decimalDigits ?: 2

        val updatedSplits = state.entitySplits.map { entity ->
            if (entity.userId == subunitId) {
                val subunitTotalCents = entity.amountCents
                val typedCents = splitPreviewService.parseAmountToCents(typedAmount, decimalDigits)
                val remainingCents = (subunitTotalCents - typedCents).coerceAtLeast(0)

                val otherMemberIds = entity.entityMembers
                    .filter { it.userId != userId }
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
                            formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(typedCents, currencyCode)
                        )
                        else -> {
                            val share = otherSharesMap[member.userId]
                            val cents = share?.amountCents ?: 0L
                            member.copy(
                                amountCents = cents,
                                amountInput = addExpenseUiMapper.formatCentsValue(cents, decimalDigits),
                                formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(cents, currencyCode)
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

                val editedAmountCents = splitPreviewService.calculateAmountFromPercentage(typedPct, subunitTotalCents)
                val otherSharesMap = splitPreviewService.redistributeRemainingPercentage(
                    typedPct, subunitTotalCents, otherMemberIds
                ).associateBy { it.userId }

                val updatedMembers = entity.entityMembers.map { member ->
                    when {
                        member.userId == userId -> member.copy(
                            percentageInput = typedPercentage,
                            amountCents = editedAmountCents,
                            formattedAmount = if (subunitTotalCents > 0) {
                                addExpenseUiMapper.formatCentsWithCurrency(editedAmountCents, currencyCode)
                            } else ""
                        )
                        member.userId in otherMemberIds.toSet() -> {
                            val share = otherSharesMap[member.userId]
                            val pct = share?.percentage ?: BigDecimal.ZERO
                            val amountCents = share?.amountCents ?: 0L
                            member.copy(
                                percentageInput = addExpenseUiMapper.formatPercentageForDisplay(pct),
                                amountCents = amountCents,
                                formattedAmount = if (subunitTotalCents > 0) {
                                    addExpenseUiMapper.formatCentsWithCurrency(amountCents, currencyCode)
                                } else ""
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

    // ── Recalculation (cross-handler) ───────────────────────────────────

    /**
     * Recalculates entity-level splits based on the current split type and source amount.
     * Called when source amount changes, split type changes, or sub-unit mode is toggled.
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
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(share.amountCents, currencyCode)
                    )
                    recalculateIntraSubunitSplits(updated, currencyCode)
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
                        amountInput = addExpenseUiMapper.formatCentsValue(share.amountCents, decimalDigits),
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(share.amountCents, currencyCode)
                    )
                    recalculateIntraSubunitSplits(updated, currencyCode)
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
            sourceAmountCents, activeEntityIds
        ).associateBy { it.userId }

        val updatedSplits = _uiState.value.entitySplits.map { entity ->
            val share = sharesByEntityId[entity.userId]
            if (!entity.isExcluded && share != null) {
                val pct = share.percentage ?: BigDecimal.ZERO
                val updated = entity.copy(
                    percentageInput = addExpenseUiMapper.formatPercentageForDisplay(pct),
                    amountCents = share.amountCents,
                    formattedAmount = if (sourceAmountCents > 0) {
                        addExpenseUiMapper.formatCentsWithCurrency(share.amountCents, currencyCode)
                    } else ""
                )
                recalculateIntraSubunitSplits(updated, currencyCode)
            } else if (entity.isExcluded) {
                entity.copy(percentageInput = "", amountCents = 0L, formattedAmount = "")
            } else {
                entity
            }
        }.toImmutableList()

        _uiState.update { it.copy(entitySplits = updatedSplits, splitError = null) }
    }

    // ── Private: Intra-sub-unit recalculation ───────────────────────────

    /**
     * Recalculates the member splits within a sub-unit entity row based on
     * the entity's current [SplitUiModel.amountCents] and [SplitUiModel.entitySplitType].
     *
     * Returns the entity with updated [SplitUiModel.entityMembers].
     */
    private fun recalculateIntraSubunitSplits(
        entity: SplitUiModel,
        currencyCode: String
    ): SplitUiModel {
        if (entity.entityMembers.isEmpty()) return entity

        val subunitTotalCents = entity.amountCents
        val intraType = entity.entitySplitType?.let { SplitType.fromString(it.id) } ?: SplitType.EQUAL
        val memberIds = entity.entityMembers.map { it.userId }

        if (subunitTotalCents <= 0 || memberIds.isEmpty()) return entity

        val decimalDigits = _uiState.value.selectedCurrency?.decimalDigits ?: 2

        val updatedMembers: ImmutableList<SplitUiModel> = when (intraType) {
            SplitType.EQUAL -> {
                // Use memberShares for proportional distribution when available,
                // mirroring SubunitAwareSplitService.expandByMemberShares().
                val subunit = groupSubunits.find { it.id == entity.userId }
                val memberShares = subunit?.memberShares ?: emptyMap()

                if (memberShares.isNotEmpty()) {
                    distributeByMemberShares(
                        entity.entityMembers, subunitTotalCents, memberShares, currencyCode
                    )
                } else {
                    try {
                        val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
                        val shares = calculator.calculateShares(subunitTotalCents, memberIds)
                            .associateBy { it.userId }
                        entity.entityMembers.map { member ->
                            val share = shares[member.userId]
                            if (share != null) {
                                member.copy(
                                    amountCents = share.amountCents,
                                    formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(
                                        share.amountCents, currencyCode
                                    )
                                )
                            } else member
                        }.toImmutableList()
                    } catch (_: Exception) {
                        entity.entityMembers
                    }
                }
            }
            SplitType.EXACT -> {
                // Pre-fill with even distribution so inputs are never blank
                try {
                    val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
                    val shares = calculator.calculateShares(subunitTotalCents, memberIds)
                        .associateBy { it.userId }
                    entity.entityMembers.map { member ->
                        val share = shares[member.userId]
                        if (share != null) {
                            member.copy(
                                amountCents = share.amountCents,
                                amountInput = addExpenseUiMapper.formatCentsValue(
                                    share.amountCents, decimalDigits
                                ),
                                formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(
                                    share.amountCents, currencyCode
                                )
                            )
                        } else member
                    }.toImmutableList()
                } catch (_: Exception) {
                    entity.entityMembers
                }
            }
            SplitType.PERCENT -> {
                // Pre-fill with even percentage distribution
                val shares = splitPreviewService.distributePercentagesEvenly(
                    subunitTotalCents, memberIds
                ).associateBy { it.userId }
                entity.entityMembers.map { member ->
                    val share = shares[member.userId]
                    if (share != null) {
                        val pct = share.percentage ?: BigDecimal.ZERO
                        member.copy(
                            percentageInput = addExpenseUiMapper.formatPercentageForDisplay(pct),
                            amountCents = share.amountCents,
                            formattedAmount = if (subunitTotalCents > 0) {
                                addExpenseUiMapper.formatCentsWithCurrency(
                                    share.amountCents, currencyCode
                                )
                            } else ""
                        )
                    } else member
                }.toImmutableList()
            }
        }

        return entity.copy(entityMembers = updatedMembers)
    }

    // ── Private helpers ─────────────────────────────────────────────────

    /**
     * Distributes [totalCents] among members proportionally based on [memberShares] weights.
     * Delegates to [SubunitAwareSplitService.distributeByMemberShares] for the core distribution
     * logic (DOWN rounding + remainder) to keep UI preview and domain calculations consistent.
     */
    private fun distributeByMemberShares(
        members: ImmutableList<SplitUiModel>,
        totalCents: Long,
        memberShares: Map<String, BigDecimal>,
        currencyCode: String
    ): ImmutableList<SplitUiModel> {
        val distributed = subunitAwareSplitService.distributeByMemberShares(
            memberIds = members.map { it.userId },
            totalCents = totalCents,
            memberShares = memberShares
        )
        return members.map { member ->
            val finalAmount = distributed[member.userId] ?: 0L
            member.copy(
                amountCents = finalAmount,
                formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(finalAmount, currencyCode)
            )
        }.toImmutableList()
    }

    private fun parseSourceAmountToCents(): Long {
        val state = _uiState.value
        val decimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
        return splitPreviewService.parseAmountToCents(state.sourceAmount.trim(), decimalPlaces)
    }
}


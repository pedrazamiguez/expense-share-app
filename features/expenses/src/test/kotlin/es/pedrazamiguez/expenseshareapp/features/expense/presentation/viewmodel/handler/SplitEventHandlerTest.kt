package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.RemainderDistributionService
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory
import es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseSplitMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitTypeUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for [SplitEventHandler].
 *
 * All split mutations (EQUAL / EXACT / PERCENT) are purely synchronous —
 * no coroutines are launched inside the handler. The [runTest] scope is
 * used only to satisfy [bind]'s [CoroutineScope] parameter.
 */
class SplitEventHandlerTest {

    private lateinit var handler: SplitEventHandler
    private lateinit var uiState: MutableStateFlow<AddExpenseUiState>
    private lateinit var actions: MutableSharedFlow<AddExpenseUiAction>

    private val eurCurrency = CurrencyUiModel(code = "EUR", displayText = "EUR (€)", decimalDigits = 2)

    private val equalSplitType = SplitTypeUiModel(id = "EQUAL", displayText = "Equal")
    private val exactSplitType = SplitTypeUiModel(id = "EXACT", displayText = "Exact")
    private val percentSplitType = SplitTypeUiModel(id = "PERCENT", displayText = "Percent")

    private fun makeSplit(
        userId: String,
        amountCents: Long = 0L,
        amountInput: String = "",
        percentageInput: String = "",
        isShareLocked: Boolean = false,
        isExcluded: Boolean = false
    ) = SplitUiModel(
        userId = userId,
        displayName = userId,
        amountCents = amountCents,
        amountInput = amountInput,
        percentageInput = percentageInput,
        isShareLocked = isShareLocked,
        isExcluded = isExcluded
    )

    /** Base state with 100 EUR and three members on EQUAL split. */
    private val baseState = AddExpenseUiState(
        loadedGroupId = "group-1",
        groupCurrency = eurCurrency,
        selectedCurrency = eurCurrency,
        sourceAmount = "100",
        availableSplitTypes = persistentListOf(equalSplitType, exactSplitType, percentSplitType),
        selectedSplitType = equalSplitType,
        splits = persistentListOf(
            makeSplit("user-1"),
            makeSplit("user-2"),
            makeSplit("user-3")
        )
    )

    @BeforeEach
    fun setUp() {
        val localeProvider = mockk<LocaleProvider>()
        every { localeProvider.getCurrentLocale() } returns Locale.US

        val splitPreviewService = SplitPreviewService()
        handler = SplitEventHandler(
            splitCalculatorFactory = ExpenseSplitCalculatorFactory(ExpenseCalculatorService()),
            splitPreviewService = splitPreviewService,
            addExpenseUiMapper = AddExpenseSplitMapper(
                localeProvider,
                FormattingHelper(localeProvider),
                splitPreviewService,
                RemainderDistributionService()
            )
        )

        uiState = MutableStateFlow(baseState)
        actions = MutableSharedFlow()
    }

    // ── handleSplitTypeChanged ───────────────────────────────────────────

    @Nested
    inner class SplitTypeChanged {

        @Test
        fun `changes selected split type in state`() = runTest {
            handler.bind(uiState, actions, this)

            handler.handleSplitTypeChanged(exactSplitType.id)

            assertEquals(exactSplitType, uiState.value.selectedSplitType)
        }

        @Test
        fun `clears all share locks when changing type`() = runTest {
            uiState.value = baseState.copy(
                splits = persistentListOf(
                    makeSplit("user-1", isShareLocked = true),
                    makeSplit("user-2", isShareLocked = true),
                    makeSplit("user-3")
                )
            )
            handler.bind(uiState, actions, this)

            handler.handleSplitTypeChanged(exactSplitType.id)

            uiState.value.splits.forEach { split ->
                assertFalse(split.isShareLocked, "Lock should be cleared for ${split.userId}")
            }
        }

        @Test
        fun `clears split error when changing type`() = runTest {
            uiState.value = baseState.copy(
                splitError = es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText.DynamicString("error")
            )
            handler.bind(uiState, actions, this)

            handler.handleSplitTypeChanged(equalSplitType.id)

            assertNull(uiState.value.splitError)
        }

        @Test
        fun `switching to EQUAL recalculates evenly for 100 EUR with 3 members`() = runTest {
            // 10000 cents / 3 = 3334, 3333, 3333 (remainder to first)
            handler.bind(uiState, actions, this)

            handler.handleSplitTypeChanged(equalSplitType.id)

            val splits = uiState.value.splits
            assertEquals(3, splits.size)
            // Each member should have a non-zero amount
            splits.forEach { assertTrue(it.amountCents > 0) }
            // Total must equal 10000 cents (100.00 EUR)
            assertEquals(10000L, splits.sumOf { it.amountCents })
        }

        @Test
        fun `switching to unknown type is a no-op`() = runTest {
            handler.bind(uiState, actions, this)

            handler.handleSplitTypeChanged("UNKNOWN_TYPE")

            // selectedSplitType must not have changed
            assertEquals(equalSplitType, uiState.value.selectedSplitType)
        }
    }

    // ── handleSplitExcludedToggled ───────────────────────────────────────

    @Nested
    inner class SplitExcludedToggled {

        @Test
        fun `marks the specified user as excluded`() = runTest {
            handler.bind(uiState, actions, this)

            handler.handleSplitExcludedToggled("user-2")

            assertTrue(uiState.value.splits.first { it.userId == "user-2" }.isExcluded)
        }

        @Test
        fun `re-including excluded user restores participation`() = runTest {
            uiState.value = baseState.copy(
                splits = persistentListOf(
                    makeSplit("user-1"),
                    makeSplit("user-2", isExcluded = true),
                    makeSplit("user-3")
                )
            )
            handler.bind(uiState, actions, this)

            handler.handleSplitExcludedToggled("user-2")

            assertFalse(uiState.value.splits.first { it.userId == "user-2" }.isExcluded)
        }

        @Test
        fun `clears all share locks on exclude toggle`() = runTest {
            uiState.value = baseState.copy(
                splits = persistentListOf(
                    makeSplit("user-1", isShareLocked = true),
                    makeSplit("user-2", isShareLocked = true),
                    makeSplit("user-3", isShareLocked = true)
                )
            )
            handler.bind(uiState, actions, this)

            handler.handleSplitExcludedToggled("user-1")

            uiState.value.splits.forEach { assertFalse(it.isShareLocked) }
        }

        @Test
        fun `excluding one member redistributes amount to remaining two`() = runTest {
            // 100 EUR, 3 members → exclude user-3 → 2 active members → 5000 each
            handler.bind(uiState, actions, this)

            handler.handleSplitExcludedToggled("user-3")

            val activeSplits = uiState.value.splits.filter { !it.isExcluded }
            assertEquals(2, activeSplits.size)
            assertEquals(10000L, activeSplits.sumOf { it.amountCents })
        }
    }

    // ── handleShareLockToggled ───────────────────────────────────────────

    @Nested
    inner class ShareLockToggled {

        @Test
        fun `toggles lock on for an unlocked member`() = runTest {
            handler.bind(uiState, actions, this)

            handler.handleShareLockToggled("user-1")

            assertTrue(uiState.value.splits.first { it.userId == "user-1" }.isShareLocked)
        }

        @Test
        fun `toggles lock off for a locked member`() = runTest {
            uiState.value = baseState.copy(
                splits = persistentListOf(
                    makeSplit("user-1", isShareLocked = true),
                    makeSplit("user-2"),
                    makeSplit("user-3")
                )
            )
            handler.bind(uiState, actions, this)

            handler.handleShareLockToggled("user-1")

            assertFalse(uiState.value.splits.first { it.userId == "user-1" }.isShareLocked)
        }

        @Test
        fun `does not affect other members' locks`() = runTest {
            uiState.value = baseState.copy(
                splits = persistentListOf(
                    makeSplit("user-1"),
                    makeSplit("user-2", isShareLocked = true),
                    makeSplit("user-3")
                )
            )
            handler.bind(uiState, actions, this)

            handler.handleShareLockToggled("user-1")

            assertTrue(uiState.value.splits.first { it.userId == "user-2" }.isShareLocked)
            assertFalse(uiState.value.splits.first { it.userId == "user-3" }.isShareLocked)
        }
    }

    // ── recalculateSplits — EQUAL ────────────────────────────────────────

    @Nested
    inner class RecalculateEqualSplits {

        @Test
        fun `distributes 100 EUR evenly across 3 members (remainder to first)`() = runTest {
            handler.bind(uiState, actions, this)

            handler.recalculateSplits()

            val splits = uiState.value.splits
            assertEquals(10000L, splits.sumOf { it.amountCents })
            // Each should be ~33.33; sum must balance
            assertTrue(splits.all { it.amountCents in 3333L..3334L })
        }

        @Test
        fun `no-op when source amount is zero`() = runTest {
            uiState.value = baseState.copy(sourceAmount = "0")
            handler.bind(uiState, actions, this)

            handler.recalculateSplits()

            uiState.value.splits.forEach { assertEquals(0L, it.amountCents) }
        }

        @Test
        fun `no-op when source amount is blank`() = runTest {
            uiState.value = baseState.copy(sourceAmount = "")
            handler.bind(uiState, actions, this)

            handler.recalculateSplits()

            uiState.value.splits.forEach { assertEquals(0L, it.amountCents) }
        }

        @Test
        fun `excluded members receive zero and are not counted in distribution`() = runTest {
            uiState.value = baseState.copy(
                splits = persistentListOf(
                    makeSplit("user-1"),
                    makeSplit("user-2"),
                    makeSplit("user-3", isExcluded = true)
                )
            )
            handler.bind(uiState, actions, this)

            handler.recalculateSplits()

            assertEquals(0L, uiState.value.splits.first { it.userId == "user-3" }.amountCents)
            // user-1 and user-2 split 100 EUR
            val activeTotal = uiState.value.splits
                .filter { !it.isExcluded }
                .sumOf { it.amountCents }
            assertEquals(10000L, activeTotal)
        }
    }

    // ── recalculateSplits — EXACT ─────────────────────────────────────────

    @Nested
    inner class RecalculateExactSplits {

        @Test
        fun `initializes EXACT splits with even distribution`() = runTest {
            uiState.value = baseState.copy(selectedSplitType = exactSplitType)
            handler.bind(uiState, actions, this)

            handler.recalculateSplits()

            val splits = uiState.value.splits
            assertEquals(10000L, splits.sumOf { it.amountCents })
            // amountInput should be populated for editable display
            splits.forEach { assertTrue(it.amountInput.isNotBlank()) }
        }

        @Test
        fun `no-op when source amount is empty`() = runTest {
            uiState.value = baseState.copy(
                selectedSplitType = exactSplitType,
                sourceAmount = ""
            )
            handler.bind(uiState, actions, this)

            // Record state before
            val before = uiState.value.splits.map { it.amountCents }
            handler.recalculateSplits()
            val after = uiState.value.splits.map { it.amountCents }

            assertEquals(before, after)
        }
    }

    // ── recalculateSplits — PERCENT ───────────────────────────────────────

    @Nested
    inner class RecalculatePercentSplits {

        @Test
        fun `distributes 100 percent evenly across 3 members`() = runTest {
            uiState.value = baseState.copy(selectedSplitType = percentSplitType)
            handler.bind(uiState, actions, this)

            handler.recalculateSplits()

            val splits = uiState.value.splits
            // Percentage inputs should be populated
            splits.forEach { assertTrue(it.percentageInput.isNotBlank()) }
            // Total cents must balance
            assertEquals(10000L, splits.sumOf { it.amountCents })
        }

        @Test
        fun `works with zero source amount (percentages populated, cents zero)`() = runTest {
            uiState.value = baseState.copy(
                selectedSplitType = percentSplitType,
                sourceAmount = "0"
            )
            handler.bind(uiState, actions, this)

            handler.recalculateSplits()

            val splits = uiState.value.splits
            splits.forEach { assertTrue(it.percentageInput.isNotBlank()) }
            splits.forEach { assertEquals(0L, it.amountCents) }
        }
    }

    // ── handleExactAmountChanged ─────────────────────────────────────────

    @Nested
    inner class ExactAmountChanged {

        @Test
        fun `typing an amount for one member auto-distributes remainder to others`() = runTest {
            uiState.value = baseState.copy(selectedSplitType = exactSplitType)
            handler.bind(uiState, actions, this)

            // user-1 claims 40.00 EUR → user-2 and user-3 share the remaining 60.00 EUR
            handler.handleExactAmountChanged("user-1", "40")

            val splits = uiState.value.splits
            val user1 = splits.first { it.userId == "user-1" }
            assertEquals(4000L, user1.amountCents)
            assertTrue(user1.isShareLocked)

            val others = splits.filter { it.userId != "user-1" }
            assertEquals(6000L, others.sumOf { it.amountCents })
        }

        @Test
        fun `total always sums to source amount after edit`() = runTest {
            uiState.value = baseState.copy(selectedSplitType = exactSplitType)
            handler.bind(uiState, actions, this)

            handler.handleExactAmountChanged("user-2", "33.33")

            val total = uiState.value.splits.sumOf { it.amountCents }
            assertEquals(10000L, total)
        }

        @Test
        fun `locked members keep their amount when another member edits`() = runTest {
            uiState.value = baseState.copy(
                selectedSplitType = exactSplitType,
                splits = persistentListOf(
                    makeSplit("user-1", amountCents = 3000L, isShareLocked = true),
                    makeSplit("user-2", amountCents = 3000L, isShareLocked = true),
                    makeSplit("user-3", amountCents = 4000L)
                )
            )
            handler.bind(uiState, actions, this)

            // user-3 edits their amount — user-1 and user-2 are locked
            handler.handleExactAmountChanged("user-3", "50")

            val user1 = uiState.value.splits.first { it.userId == "user-1" }
            val user2 = uiState.value.splits.first { it.userId == "user-2" }
            assertEquals(3000L, user1.amountCents)
            assertEquals(3000L, user2.amountCents)
        }

        @Test
        fun `stores typed value without distribution when source amount is zero`() = runTest {
            uiState.value = baseState.copy(
                selectedSplitType = exactSplitType,
                sourceAmount = ""
            )
            handler.bind(uiState, actions, this)

            handler.handleExactAmountChanged("user-1", "20")

            val user1 = uiState.value.splits.first { it.userId == "user-1" }
            assertEquals("20", user1.amountInput)
            assertTrue(user1.isShareLocked)
        }
    }

    // ── handlePercentageChanged ──────────────────────────────────────────

    @Nested
    inner class PercentageChanged {

        @Test
        fun `typing a percentage for one member redistributes the remaining percent`() = runTest {
            uiState.value = baseState.copy(selectedSplitType = percentSplitType)
            handler.bind(uiState, actions, this)

            // user-1 claims 50% → user-2 and user-3 share the remaining 50%
            handler.handlePercentageChanged("user-1", "50")

            val splits = uiState.value.splits
            val user1 = splits.first { it.userId == "user-1" }
            assertEquals("50", user1.percentageInput)
            assertTrue(user1.isShareLocked)
            assertEquals(5000L, user1.amountCents)

            val otherPercents = splits
                .filter { it.userId != "user-1" }
                .mapNotNull { it.percentageInput.toBigDecimalOrNull() }
            // The two remaining members split 50% → 25% each
            otherPercents.forEach { pct ->
                assertEquals(0, pct.compareTo(java.math.BigDecimal("25.00")))
            }
        }

        @Test
        fun `total cents equals source amount after percentage edit`() = runTest {
            uiState.value = baseState.copy(selectedSplitType = percentSplitType)
            handler.bind(uiState, actions, this)

            handler.handlePercentageChanged("user-2", "40")

            assertEquals(10000L, uiState.value.splits.sumOf { it.amountCents })
        }

        @Test
        fun `locked members keep their percentage when another edits`() = runTest {
            uiState.value = baseState.copy(
                selectedSplitType = percentSplitType,
                splits = persistentListOf(
                    makeSplit("user-1", percentageInput = "50.00", amountCents = 5000L, isShareLocked = true),
                    makeSplit("user-2", percentageInput = "25.00", amountCents = 2500L),
                    makeSplit("user-3", percentageInput = "25.00", amountCents = 2500L)
                )
            )
            handler.bind(uiState, actions, this)

            // user-2 changes to 30% — user-1 (locked 50%) must remain unchanged
            handler.handlePercentageChanged("user-2", "30")

            val user1 = uiState.value.splits.first { it.userId == "user-1" }
            assertEquals("50.00", user1.percentageInput)
            assertEquals(5000L, user1.amountCents)
        }
    }
}

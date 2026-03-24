package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.AddOn
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.RemainderDistributionService
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.AddOnUiModel
import io.mockk.mockk
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for [SubmitEventHandler.adjustForIncludedAddOns].
 *
 * These tests were originally in AddExpenseUiMapperTest but the INCLUDED
 * base-cost extraction was moved here (to the handler) as part of the
 * architecture correction that removes [ExpenseCalculatorService] from the
 * mapper (which is a presentation concern and must not hold domain services).
 */
class SubmitEventHandlerTest {

    private lateinit var handler: SubmitEventHandler

    /** A minimal [Expense] stub — only the fields used by adjustForIncludedAddOns matter. */
    private fun makeExpense(
        sourceAmount: Long,
        groupAmount: Long,
        addOns: List<AddOn> = emptyList(),
        splits: List<ExpenseSplit> = emptyList()
    ) = Expense(
        groupId = "group-1",
        title = "Test",
        sourceAmount = sourceAmount,
        sourceCurrency = "EUR",
        groupAmount = groupAmount,
        groupCurrency = "EUR",
        exchangeRate = BigDecimal.ONE,
        addOns = addOns,
        splits = splits,
        splitType = SplitType.EQUAL,
        paymentMethod = PaymentMethod.CASH,
        createdAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        val splitCalculatorFactory = ExpenseSplitCalculatorFactory(ExpenseCalculatorService())
        handler = SubmitEventHandler(
            addExpenseUseCase = mockk(relaxed = true),
            expenseValidationService = ExpenseValidationService(splitCalculatorFactory),
            expenseCalculatorService = ExpenseCalculatorService(),
            remainderDistributionService = RemainderDistributionService(),
            setGroupLastUsedCurrencyUseCase = mockk(relaxed = true),
            setGroupLastUsedPaymentMethodUseCase = mockk(relaxed = true),
            setGroupLastUsedCategoryUseCase = mockk(relaxed = true),
            addExpenseUiMapper = mockk(relaxed = true),
            formattingHelper = mockk(relaxed = true)
        )
    }

    // ── No INCLUDED add-ons ──────────────────────────────────────────────────

    @Nested
    inner class NoIncludedAddOns {

        @Test
        fun `ON_TOP only - expense is returned unchanged`() {
            val addOn = AddOn(
                id = "fee-1",
                type = AddOnType.FEE,
                mode = AddOnMode.ON_TOP,
                valueType = AddOnValueType.EXACT,
                amountCents = 500,
                currency = "EUR",
                exchangeRate = BigDecimal.ONE,
                groupAmountCents = 500
            )
            val expense = makeExpense(sourceAmount = 5000L, groupAmount = 5000L, addOns = listOf(addOn))

            val result = handler.adjustForIncludedAddOns(expense, persistentListOf())

            assertEquals(5000L, result.sourceAmount)
            assertEquals(5000L, result.groupAmount)
            assertEquals(500L, result.addOns[0].groupAmountCents)
        }

        @Test
        fun `no add-ons - expense is returned unchanged`() {
            val expense = makeExpense(sourceAmount = 8000L, groupAmount = 8000L)

            val result = handler.adjustForIncludedAddOns(expense, persistentListOf())

            assertEquals(8000L, result.sourceAmount)
            assertEquals(8000L, result.groupAmount)
        }
    }

    // ── EXACT INCLUDED ──────────────────────────────────────────────────────

    @Nested
    inner class ExactIncluded {

        @Test
        fun `EXACT INCLUDED tip subtracts from sourceAmount and groupAmount`() {
            // 68.31 EUR total with 6.21 EUR EXACT INCLUDED tip
            // Expected base: 68.31 - 6.21 = 62.10 EUR (6210 cents)
            val domainAddOn = AddOn(
                id = "tip-1",
                type = AddOnType.TIP,
                mode = AddOnMode.INCLUDED,
                valueType = AddOnValueType.EXACT,
                amountCents = 621,
                currency = "EUR",
                exchangeRate = BigDecimal.ONE,
                groupAmountCents = 621
            )
            val expense = makeExpense(
                sourceAmount = 6831L,
                groupAmount = 6831L,
                addOns = listOf(domainAddOn)
            )

            val result = handler.adjustForIncludedAddOns(expense, persistentListOf())

            assertEquals(6210L, result.sourceAmount)
            assertEquals(6210L, result.groupAmount)
            // EXACT add-on amount stays the same
            assertEquals(621L, result.addOns[0].groupAmountCents)
        }

        @Test
        fun `INCLUDED DISCOUNT is excluded from base cost adjustment`() {
            // An INCLUDED DISCOUNT must not trigger base cost extraction
            val domainAddOn = AddOn(
                id = "disc-1",
                type = AddOnType.DISCOUNT,
                mode = AddOnMode.INCLUDED,
                valueType = AddOnValueType.EXACT,
                amountCents = 500,
                currency = "EUR",
                exchangeRate = BigDecimal.ONE,
                groupAmountCents = 500
            )
            val expense = makeExpense(
                sourceAmount = 5000L,
                groupAmount = 5000L,
                addOns = listOf(domainAddOn)
            )

            val result = handler.adjustForIncludedAddOns(expense, persistentListOf())

            assertEquals(5000L, result.sourceAmount)
            assertEquals(5000L, result.groupAmount)
        }
    }

    // ── PERCENTAGE INCLUDED ─────────────────────────────────────────────────

    @Nested
    inner class PercentageIncluded {

        @Test
        fun `PERCENTAGE INCLUDED tip extracts base and recomputes add-on amount`() {
            // 68.31 EUR total with 10% INCLUDED tip
            // Base: 6831 / 1.10 = 6210 cents (floor)
            // Recomputed tip: 6210 * 10 / 100 = 621 cents
            val domainAddOn = AddOn(
                id = "tip-1",
                type = AddOnType.TIP,
                mode = AddOnMode.INCLUDED,
                valueType = AddOnValueType.PERCENTAGE,
                amountCents = 683,
                currency = "EUR",
                exchangeRate = BigDecimal.ONE,
                groupAmountCents = 683 // pre-adjustment; handler will recompute
            )
            val uiAddOn = AddOnUiModel(
                id = "tip-1",
                type = AddOnType.TIP,
                mode = AddOnMode.INCLUDED,
                valueType = AddOnValueType.PERCENTAGE,
                amountInput = "10",
                resolvedAmountCents = 683,
                groupAmountCents = 683
            )
            val expense = makeExpense(
                sourceAmount = 6831L,
                groupAmount = 6831L,
                addOns = listOf(domainAddOn)
            )

            val result = handler.adjustForIncludedAddOns(expense, listOf(uiAddOn).toImmutableList())

            assertEquals(6210L, result.sourceAmount)
            assertEquals(6210L, result.groupAmount)
            assertEquals(621L, result.addOns[0].groupAmountCents)
        }
    }

    // ── Split rescaling ──────────────────────────────────────────────────────

    @Nested
    inner class SplitRescaling {

        @Test
        fun `splits are rescaled proportionally for EXACT INCLUDED add-on`() {
            // 100.00 EUR total with 10 EUR EXACT INCLUDED tip, 2 equal splits (5000 each)
            // Base: 9000 cents → splits should be 4500 each
            val domainAddOn = AddOn(
                id = "tip-1",
                type = AddOnType.TIP,
                mode = AddOnMode.INCLUDED,
                valueType = AddOnValueType.EXACT,
                amountCents = 1000,
                currency = "EUR",
                exchangeRate = BigDecimal.ONE,
                groupAmountCents = 1000
            )
            val splits = listOf(
                ExpenseSplit(userId = "user-1", amountCents = 5000),
                ExpenseSplit(userId = "user-2", amountCents = 5000)
            )
            val expense = makeExpense(
                sourceAmount = 10000L,
                groupAmount = 10000L,
                addOns = listOf(domainAddOn),
                splits = splits
            )

            val result = handler.adjustForIncludedAddOns(expense, persistentListOf())

            assertEquals(9000L, result.sourceAmount)
            assertEquals(9000L, result.groupAmount)
            // Sum of splits equals the new base
            assertEquals(9000L, result.splits.sumOf { it.amountCents })
            assertEquals(4500L, result.splits[0].amountCents)
            assertEquals(4500L, result.splits[1].amountCents)
        }

        @Test
        fun `split remainder is distributed to preserve total`() {
            // 10 EUR total with 1 EUR EXACT INCLUDED tip → base 9 EUR, 3 splits
            // 9000 / 3 = 3000 each exactly — no remainder
            val domainAddOn = AddOn(
                id = "tip-1",
                type = AddOnType.TIP,
                mode = AddOnMode.INCLUDED,
                valueType = AddOnValueType.EXACT,
                amountCents = 1000,
                currency = "EUR",
                exchangeRate = BigDecimal.ONE,
                groupAmountCents = 1000
            )
            val splits = listOf(
                ExpenseSplit(userId = "a", amountCents = 334),
                ExpenseSplit(userId = "b", amountCents = 333),
                ExpenseSplit(userId = "c", amountCents = 333)
            )
            val expense = makeExpense(
                sourceAmount = 1000L,
                groupAmount = 1000L,
                addOns = listOf(domainAddOn),
                splits = splits
            )

            val result = handler.adjustForIncludedAddOns(expense, persistentListOf())

            // Sum of adjusted splits must equal the new base exactly
            assertEquals(result.groupAmount, result.splits.sumOf { it.amountCents })
        }
    }
}

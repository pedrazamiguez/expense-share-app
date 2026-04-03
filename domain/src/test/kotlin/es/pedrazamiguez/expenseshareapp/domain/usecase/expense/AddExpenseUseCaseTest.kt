package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.exception.InsufficientCashException
import es.pedrazamiguez.expenseshareapp.domain.exception.NotGroupMemberException
import es.pedrazamiguez.expenseshareapp.domain.model.AddOn
import es.pedrazamiguez.expenseshareapp.domain.model.CashTranche
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AddOnCalculationService
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.ExchangeRateCalculationService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AddExpenseUseCaseTest {

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var cashWithdrawalRepository: CashWithdrawalRepository
    private lateinit var expenseCalculatorService: ExpenseCalculatorService
    private lateinit var exchangeRateCalculationService: ExchangeRateCalculationService
    private lateinit var groupMembershipService: GroupMembershipService
    private lateinit var contributionRepository: ContributionRepository
    private lateinit var authenticationService: AuthenticationService
    private lateinit var addOnCalculationService: AddOnCalculationService
    private lateinit var useCase: AddExpenseUseCase

    private val groupId = "group-123"
    private val currentUserId = "current-user-456"

    private val baseExpense = Expense(
        id = "expense-1",
        title = "Dinner",
        sourceAmount = 5000L,
        sourceCurrency = "EUR",
        groupAmount = 5000L,
        groupCurrency = "EUR",
        paymentMethod = PaymentMethod.CREDIT_CARD
    )

    @BeforeEach
    fun setUp() {
        expenseRepository = mockk(relaxed = true)
        cashWithdrawalRepository = mockk(relaxed = true)
        expenseCalculatorService = mockk()
        exchangeRateCalculationService = mockk(relaxed = true)
        groupMembershipService = mockk()
        contributionRepository = mockk(relaxed = true)
        authenticationService = mockk()
        addOnCalculationService = mockk()

        coEvery { groupMembershipService.requireMembership(any()) } just Runs
        every { authenticationService.requireUserId() } returns currentUserId
        every { addOnCalculationService.calculateEffectiveGroupAmount(any(), any()) } answers {
            firstArg()
        }

        useCase = AddExpenseUseCase(
            expenseRepository,
            cashWithdrawalRepository,
            expenseCalculatorService,
            exchangeRateCalculationService,
            groupMembershipService,
            contributionRepository,
            authenticationService,
            addOnCalculationService
        )
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Nested
    inner class Validation {

        @Test
        fun `fails when groupId is null`() = runTest {
            val result = useCase(null, baseExpense)
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("Group ID") == true)
        }

        @Test
        fun `fails when groupId is blank`() = runTest {
            val result = useCase("  ", baseExpense)
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("Group ID") == true)
        }

        @Test
        fun `fails when sourceAmount is zero`() = runTest {
            val result = useCase(groupId, baseExpense.copy(sourceAmount = 0L))
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("amount") == true)
        }

        @Test
        fun `fails when title is blank`() = runTest {
            val result = useCase(groupId, baseExpense.copy(title = ""))
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("title") == true)
        }
    }

    // ── Membership validation ─────────────────────────────────────────────────

    @Nested
    inner class MembershipValidation {

        @Test
        fun `fails with NotGroupMemberException when user is not a member`() = runTest {
            coEvery {
                groupMembershipService.requireMembership(groupId)
            } throws NotGroupMemberException(groupId = groupId, userId = "user-123")

            val result = useCase(groupId, baseExpense)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is NotGroupMemberException)
        }

        @Test
        fun `does not save expense when membership check fails`() = runTest {
            coEvery {
                groupMembershipService.requireMembership(groupId)
            } throws NotGroupMemberException(groupId = groupId, userId = "user-123")

            useCase(groupId, baseExpense)

            coVerify(exactly = 0) { expenseRepository.addExpense(any(), any()) }
        }

        @Test
        fun `calls requireMembership with correct groupId on success`() = runTest {
            coEvery { expenseRepository.addExpense(any(), any()) } just Runs

            useCase(groupId, baseExpense)

            coVerify(exactly = 1) { groupMembershipService.requireMembership(groupId) }
        }
    }

    // ── Non-cash expense ──────────────────────────────────────────────────────

    @Nested
    inner class NonCashExpense {

        @Test
        fun `saves expense directly without touching withdrawal repository`() = runTest {
            coEvery { expenseRepository.addExpense(any(), any()) } just Runs

            val result = useCase(groupId, baseExpense)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { expenseRepository.addExpense(groupId, baseExpense) }
            coVerify(exactly = 0) { cashWithdrawalRepository.getAvailableWithdrawals(any(), any()) }
            coVerify(exactly = 0) { cashWithdrawalRepository.updateRemainingAmounts(any(), any()) }
        }
    }

    // ── Cash expense: batch update ─────────────────────────────────────────────

    @Nested
    inner class CashExpenseBatchUpdate {

        private val cashExpense = baseExpense.copy(
            paymentMethod = PaymentMethod.CASH,
            sourceCurrency = "THB",
            sourceAmount = 23000L
        )

        private val withdrawal1 = CashWithdrawal(
            id = "w-1",
            groupId = groupId,
            amountWithdrawn = 1000000L,
            remainingAmount = 5000L,
            currency = "THB",
            deductedBaseAmount = 26400L,
            createdAt = LocalDateTime.of(2026, 1, 10, 12, 0)
        )

        private val withdrawal2 = CashWithdrawal(
            id = "w-2",
            groupId = groupId,
            amountWithdrawn = 500000L,
            remainingAmount = 500000L,
            currency = "THB",
            deductedBaseAmount = 13587L,
            createdAt = LocalDateTime.of(2026, 1, 12, 12, 0)
        )

        private val fifoResult = ExpenseCalculatorService.FifoCashResult(
            groupAmountCents = 621L,
            tranches = listOf(
                CashTranche(withdrawalId = "w-1", amountConsumed = 5000L),
                CashTranche(withdrawalId = "w-2", amountConsumed = 18000L)
            )
        )

        @BeforeEach
        fun setUpCash() {
            coEvery {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, "THB")
            } returns listOf(withdrawal1, withdrawal2)

            every {
                expenseCalculatorService.hasInsufficientCash(
                    cashExpense.sourceAmount,
                    listOf(withdrawal1, withdrawal2)
                )
            } returns false

            coEvery {
                expenseCalculatorService.calculateFifoCashAmount(
                    amountToCover = cashExpense.sourceAmount,
                    availableWithdrawals = listOf(withdrawal1, withdrawal2)
                )
            } returns fifoResult

            every {
                exchangeRateCalculationService.calculateBlendedRate(
                    sourceAmountCents = cashExpense.sourceAmount,
                    groupAmountCents = fifoResult.groupAmountCents
                )
            } returns BigDecimal("0.027000")

            coEvery { cashWithdrawalRepository.updateRemainingAmounts(any(), any()) } just Runs
            coEvery { expenseRepository.addExpense(any(), any()) } just Runs
        }

        @Test
        fun `calls updateRemainingAmounts exactly once for multi-tranche expense`() = runTest {
            useCase(groupId, cashExpense)

            // Must be exactly one batch call, never the single-update method
            coVerify(exactly = 1) { cashWithdrawalRepository.updateRemainingAmounts(any(), any()) }
            coVerify(exactly = 0) { cashWithdrawalRepository.updateRemainingAmount(any(), any()) }
        }

        @Test
        fun `passes groupId to updateRemainingAmounts`() = runTest {
            val groupIdSlot = slot<String>()
            coEvery {
                cashWithdrawalRepository.updateRemainingAmounts(capture(groupIdSlot), any())
            } just Runs

            useCase(groupId, cashExpense)

            assertEquals(groupId, groupIdSlot.captured)
        }

        @Test
        fun `passes correctly deducted withdrawal objects to updateRemainingAmounts`() = runTest {
            val withdrawalsSlot = slot<List<CashWithdrawal>>()
            coEvery {
                cashWithdrawalRepository.updateRemainingAmounts(any(), capture(withdrawalsSlot))
            } just Runs

            useCase(groupId, cashExpense)

            val updated = withdrawalsSlot.captured
            assertEquals(2, updated.size)

            val updatedW1 = updated.first { it.id == "w-1" }
            assertEquals(0L, updatedW1.remainingAmount) // 5000 - 5000

            val updatedW2 = updated.first { it.id == "w-2" }
            assertEquals(482000L, updatedW2.remainingAmount) // 500000 - 18000
        }

        @Test
        fun `attaches tranches and blended group amount to saved expense`() = runTest {
            val savedExpenseSlot = slot<Expense>()
            coEvery {
                expenseRepository.addExpense(any(), capture(savedExpenseSlot))
            } just Runs

            useCase(groupId, cashExpense)

            val saved = savedExpenseSlot.captured
            assertEquals(fifoResult.tranches, saved.cashTranches)
            assertEquals(fifoResult.groupAmountCents, saved.groupAmount)
        }

        @Test
        fun `sets blended exchange rate on saved cash expense`() = runTest {
            val savedExpenseSlot = slot<Expense>()
            coEvery {
                expenseRepository.addExpense(any(), capture(savedExpenseSlot))
            } just Runs

            useCase(groupId, cashExpense)

            val saved = savedExpenseSlot.captured
            // exchangeRate must be the blended rate from FIFO, not the original API rate
            assertEquals(BigDecimal("0.027000"), saved.exchangeRate)
        }

        @Test
        fun `single-tranche cash expense still uses batch call`() = runTest {
            val singleTranche = fifoResult.copy(
                tranches = listOf(CashTranche(withdrawalId = "w-1", amountConsumed = 5000L))
            )
            coEvery {
                expenseCalculatorService.calculateFifoCashAmount(any(), any())
            } returns singleTranche

            useCase(groupId, cashExpense)

            coVerify(exactly = 1) { cashWithdrawalRepository.updateRemainingAmounts(any(), any()) }
            coVerify(exactly = 0) { cashWithdrawalRepository.updateRemainingAmount(any(), any()) }
        }
    }

    // ── Insufficient cash ─────────────────────────────────────────────────────

    @Nested
    inner class InsufficientCash {

        private val cashExpense = baseExpense.copy(
            paymentMethod = PaymentMethod.CASH,
            sourceCurrency = "THB",
            sourceAmount = 50000L // More than available
        )

        private val withdrawal = CashWithdrawal(
            id = "w-1",
            groupId = groupId,
            amountWithdrawn = 100000L,
            remainingAmount = 32000L, // Less than required
            currency = "THB",
            deductedBaseAmount = 86400L,
            createdAt = LocalDateTime.of(2026, 1, 10, 12, 0)
        )

        @BeforeEach
        fun setUpInsufficientCash() {
            coEvery {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, "THB")
            } returns listOf(withdrawal)

            every {
                expenseCalculatorService.hasInsufficientCash(
                    cashExpense.sourceAmount,
                    listOf(withdrawal)
                )
            } returns true
        }

        @Test
        fun `fails with InsufficientCashException when cash is not enough`() = runTest {
            val result = useCase(groupId, cashExpense)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is InsufficientCashException)
        }

        @Test
        fun `exception carries required and available cent values`() = runTest {
            val result = useCase(groupId, cashExpense)

            val exception = result.exceptionOrNull() as InsufficientCashException
            assertEquals(cashExpense.sourceAmount, exception.requiredCents)
            assertEquals(listOf(withdrawal).sumOf { it.remainingAmount }, exception.availableCents)
        }

        @Test
        fun `does not save expense when cash is insufficient`() = runTest {
            useCase(groupId, cashExpense)

            coVerify(exactly = 0) { expenseRepository.addExpense(any(), any()) }
        }

        @Test
        fun `does not update withdrawals when cash is insufficient`() = runTest {
            useCase(groupId, cashExpense)

            coVerify(exactly = 0) { cashWithdrawalRepository.updateRemainingAmounts(any(), any()) }
        }
    }

    // ── Out-of-pocket: non-cash (standard case) ───────────────────────────────

    @Nested
    inner class OutOfPocketNonCash {

        private val oopExpense = baseExpense.copy(
            payerType = PayerType.USER,
            payerId = currentUserId,
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        @Test
        fun `saves expense without FIFO processing`() = runTest {
            val result = useCase(groupId, oopExpense)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { expenseRepository.addExpense(groupId, oopExpense) }
            coVerify(exactly = 0) { cashWithdrawalRepository.getAvailableWithdrawals(any(), any()) }
        }

        @Test
        fun `creates paired contribution with correct linked expense id`() = runTest {
            val contributionSlot = slot<Contribution>()
            coEvery {
                contributionRepository.addContribution(any(), capture(contributionSlot))
            } just Runs

            useCase(groupId, oopExpense)

            assertEquals(oopExpense.id, contributionSlot.captured.linkedExpenseId)
        }

        @Test
        fun `paired contribution amount equals group amount when no add-ons`() = runTest {
            val contributionSlot = slot<Contribution>()
            coEvery {
                contributionRepository.addContribution(any(), capture(contributionSlot))
            } just Runs

            useCase(groupId, oopExpense)

            assertEquals(oopExpense.groupAmount, contributionSlot.captured.amount)
        }

        @Test
        fun `paired contribution uses group currency`() = runTest {
            val contributionSlot = slot<Contribution>()
            coEvery {
                contributionRepository.addContribution(any(), capture(contributionSlot))
            } just Runs

            useCase(groupId, oopExpense)

            assertEquals(oopExpense.groupCurrency, contributionSlot.captured.currency)
        }
    }

    // ── Out-of-pocket: cash (FIFO skip) ───────────────────────────────────────

    @Nested
    inner class OutOfPocketCash {

        private val oopCashExpense = baseExpense.copy(
            payerType = PayerType.USER,
            payerId = currentUserId,
            paymentMethod = PaymentMethod.CASH
        )

        @Test
        fun `skips FIFO processing entirely`() = runTest {
            useCase(groupId, oopCashExpense)

            coVerify(exactly = 0) { cashWithdrawalRepository.getAvailableWithdrawals(any(), any()) }
            coVerify(exactly = 0) { cashWithdrawalRepository.updateRemainingAmounts(any(), any()) }
        }

        @Test
        fun `saves expense with original amounts (no FIFO rewrite)`() = runTest {
            val savedSlot = slot<Expense>()
            coEvery { expenseRepository.addExpense(any(), capture(savedSlot)) } just Runs

            useCase(groupId, oopCashExpense)

            assertEquals(oopCashExpense.groupAmount, savedSlot.captured.groupAmount)
            assertTrue(savedSlot.captured.cashTranches.isEmpty())
        }

        @Test
        fun `creates paired contribution for out-of-pocket cash expense`() = runTest {
            val contributionSlot = slot<Contribution>()
            coEvery {
                contributionRepository.addContribution(any(), capture(contributionSlot))
            } just Runs

            useCase(groupId, oopCashExpense)

            assertEquals(oopCashExpense.id, contributionSlot.captured.linkedExpenseId)
            assertEquals(PayerType.USER, contributionSlot.captured.contributionScope)
        }
    }

    // ── Out-of-pocket: with add-ons (effective amount) ────────────────────────

    @Nested
    inner class OutOfPocketWithAddOns {

        private val tipAddOn = AddOn(
            type = AddOnType.TIP,
            mode = AddOnMode.ON_TOP,
            groupAmountCents = 500L
        )

        private val oopExpenseWithAddOns = baseExpense.copy(
            payerType = PayerType.USER,
            payerId = currentUserId,
            addOns = listOf(tipAddOn)
        )

        @Test
        fun `paired contribution uses effective amount (base plus add-ons)`() = runTest {
            val effectiveAmount = 5500L
            every {
                addOnCalculationService.calculateEffectiveGroupAmount(
                    oopExpenseWithAddOns.groupAmount,
                    oopExpenseWithAddOns.addOns
                )
            } returns effectiveAmount

            val contributionSlot = slot<Contribution>()
            coEvery {
                contributionRepository.addContribution(any(), capture(contributionSlot))
            } just Runs

            useCase(groupId, oopExpenseWithAddOns)

            assertEquals(effectiveAmount, contributionSlot.captured.amount)
        }

        @Test
        fun `calls calculateEffectiveGroupAmount with expense data`() = runTest {
            useCase(groupId, oopExpenseWithAddOns)

            io.mockk.verify(exactly = 1) {
                addOnCalculationService.calculateEffectiveGroupAmount(
                    oopExpenseWithAddOns.groupAmount,
                    oopExpenseWithAddOns.addOns
                )
            }
        }
    }

    // ── Out-of-pocket: multi-currency ─────────────────────────────────────────

    @Nested
    inner class OutOfPocketMultiCurrency {

        private val oopMultiCurrencyExpense = baseExpense.copy(
            payerType = PayerType.USER,
            payerId = currentUserId,
            sourceAmount = 500000L,
            sourceCurrency = "THB",
            groupAmount = 1350L,
            groupCurrency = "EUR",
            exchangeRate = BigDecimal("0.027000")
        )

        @Test
        fun `paired contribution uses group amount in group currency`() = runTest {
            val contributionSlot = slot<Contribution>()
            coEvery {
                contributionRepository.addContribution(any(), capture(contributionSlot))
            } just Runs

            useCase(groupId, oopMultiCurrencyExpense)

            assertEquals(1350L, contributionSlot.captured.amount)
            assertEquals("EUR", contributionSlot.captured.currency)
        }
    }

    // ── Out-of-pocket: contribution field validation ──────────────────────────

    @Nested
    inner class OutOfPocketContributionFields {

        private val oopExpense = baseExpense.copy(
            payerType = PayerType.USER,
            payerId = currentUserId
        )

        @Test
        fun `paired contribution has USER scope`() = runTest {
            val contributionSlot = slot<Contribution>()
            coEvery {
                contributionRepository.addContribution(any(), capture(contributionSlot))
            } just Runs

            useCase(groupId, oopExpense)

            assertEquals(PayerType.USER, contributionSlot.captured.contributionScope)
        }

        @Test
        fun `paired contribution userId matches payer`() = runTest {
            val contributionSlot = slot<Contribution>()
            coEvery {
                contributionRepository.addContribution(any(), capture(contributionSlot))
            } just Runs

            useCase(groupId, oopExpense)

            assertEquals(currentUserId, contributionSlot.captured.userId)
            assertEquals(currentUserId, contributionSlot.captured.createdBy)
        }

        @Test
        fun `paired contribution has non-blank UUID id`() = runTest {
            val contributionSlot = slot<Contribution>()
            coEvery {
                contributionRepository.addContribution(any(), capture(contributionSlot))
            } just Runs

            useCase(groupId, oopExpense)

            assertTrue(contributionSlot.captured.id.isNotBlank())
        }

        @Test
        fun `falls back to authenticationService when payerId is null`() = runTest {
            val expenseWithoutPayerId = oopExpense.copy(payerId = null)
            val contributionSlot = slot<Contribution>()
            coEvery {
                contributionRepository.addContribution(any(), capture(contributionSlot))
            } just Runs

            useCase(groupId, expenseWithoutPayerId)

            assertEquals(currentUserId, contributionSlot.captured.userId)
        }

        @Test
        fun `passes correct groupId when adding paired contribution`() = runTest {
            val groupIdSlot = slot<String>()
            coEvery {
                contributionRepository.addContribution(capture(groupIdSlot), any())
            } just Runs

            useCase(groupId, oopExpense)

            assertEquals(groupId, groupIdSlot.captured)
        }

        @Test
        fun `paired contribution created for scheduled out-of-pocket expense`() = runTest {
            val scheduledOopExpense = oopExpense.copy(
                paymentStatus = PaymentStatus.SCHEDULED,
                dueDate = LocalDateTime.now().plusDays(30)
            )
            val contributionSlot = slot<Contribution>()
            coEvery {
                contributionRepository.addContribution(any(), capture(contributionSlot))
            } just Runs

            useCase(groupId, scheduledOopExpense)

            assertEquals(scheduledOopExpense.id, contributionSlot.captured.linkedExpenseId)
        }
    }

    // ── GROUP-funded: no paired contribution ──────────────────────────────────

    @Nested
    inner class GroupFundedNoContribution {

        @Test
        fun `does not create paired contribution for GROUP non-cash expense`() = runTest {
            useCase(groupId, baseExpense)

            coVerify(exactly = 0) { contributionRepository.addContribution(any(), any()) }
        }

        @Test
        fun `preserves existing behavior for GROUP non-cash expense`() = runTest {
            coEvery { expenseRepository.addExpense(any(), any()) } just Runs

            val result = useCase(groupId, baseExpense)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { expenseRepository.addExpense(groupId, baseExpense) }
        }
    }

    // ── GROUP-funded + CASH: existing FIFO still works ────────────────────────

    @Nested
    inner class GroupFundedCashStillFIFO {

        private val groupCashExpense = baseExpense.copy(
            payerType = PayerType.GROUP,
            paymentMethod = PaymentMethod.CASH,
            sourceCurrency = "THB",
            sourceAmount = 10000L
        )

        private val withdrawal = CashWithdrawal(
            id = "w-1",
            groupId = groupId,
            amountWithdrawn = 500000L,
            remainingAmount = 500000L,
            currency = "THB",
            deductedBaseAmount = 13500L,
            createdAt = LocalDateTime.of(2026, 1, 10, 12, 0)
        )

        private val fifoResult = ExpenseCalculatorService.FifoCashResult(
            groupAmountCents = 270L,
            tranches = listOf(CashTranche(withdrawalId = "w-1", amountConsumed = 10000L))
        )

        @BeforeEach
        fun setUpGroupCash() {
            coEvery {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, "THB")
            } returns listOf(withdrawal)

            every {
                expenseCalculatorService.hasInsufficientCash(any(), any())
            } returns false

            coEvery {
                expenseCalculatorService.calculateFifoCashAmount(any(), any())
            } returns fifoResult

            every {
                exchangeRateCalculationService.calculateBlendedRate(any(), any())
            } returns BigDecimal("0.027000")

            coEvery { cashWithdrawalRepository.updateRemainingAmounts(any(), any()) } just Runs
            coEvery { expenseRepository.addExpense(any(), any()) } just Runs
        }

        @Test
        fun `triggers FIFO for GROUP-funded cash expense`() = runTest {
            useCase(groupId, groupCashExpense)

            coVerify(exactly = 1) {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, "THB")
            }
            coVerify(exactly = 1) {
                cashWithdrawalRepository.updateRemainingAmounts(any(), any())
            }
        }

        @Test
        fun `does not create paired contribution for GROUP cash expense`() = runTest {
            useCase(groupId, groupCashExpense)

            coVerify(exactly = 0) { contributionRepository.addContribution(any(), any()) }
        }
    }
}

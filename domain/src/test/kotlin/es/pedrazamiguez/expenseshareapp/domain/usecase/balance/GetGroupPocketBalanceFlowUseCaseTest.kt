package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GetGroupPocketBalanceFlowUseCaseTest {

    private lateinit var contributionRepository: ContributionRepository
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var cashWithdrawalRepository: CashWithdrawalRepository
    private lateinit var useCase: GetGroupPocketBalanceFlowUseCase

    private val groupId = "group-123"
    private val currency = "GBP"

    @BeforeEach
    fun setUp() {
        contributionRepository = mockk()
        expenseRepository = mockk()
        cashWithdrawalRepository = mockk()
        useCase = GetGroupPocketBalanceFlowUseCase(
            contributionRepository,
            expenseRepository,
            cashWithdrawalRepository
        )
    }

    @Nested
    inner class VirtualBalanceCalculation {

        @Test
        fun `cash expenses are excluded from virtual balance`() = runTest {
            // Given: 500 contributed, 100 cash expense, 200 withdrawal
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 50000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        groupAmount = 10000L,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.CASH
                    )
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        deductedBaseAmount = 20000L,
                        amountWithdrawn = 20000L,
                        remainingAmount = 10000L,
                        currency = currency
                    )
                )
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: virtualBalance = 50000 - 0 (no non-cash expenses) - 20000 = 30000
            assertEquals(30000L, result.virtualBalance)
            // totalExpenses still includes the cash expense for the UI summary
            assertEquals(10000L, result.totalExpenses)
        }

        @Test
        fun `non-cash expenses deduct from virtual balance`() = runTest {
            // Given: 500 contributed, 150 debit card expense, no withdrawals
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 50000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        groupAmount = 15000L,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.DEBIT_CARD
                    )
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                emptyList()
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: virtualBalance = 50000 - 15000 - 0 = 35000
            assertEquals(35000L, result.virtualBalance)
            assertEquals(15000L, result.totalExpenses)
        }

        @Test
        fun `mixed cash and non-cash expenses only deducts non-cash from virtual balance`() =
            runTest {
                // Given: 500 contributed, 100 cash expense + 150 debit card, 200 withdrawal
                every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                    listOf(Contribution(amount = 50000L, currency = currency))
                )
                every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                    listOf(
                        Expense(
                            groupAmount = 10000L,
                            groupCurrency = currency,
                            paymentMethod = PaymentMethod.CASH
                        ),
                        Expense(
                            groupAmount = 15000L,
                            groupCurrency = currency,
                            paymentMethod = PaymentMethod.DEBIT_CARD
                        )
                    )
                )
                every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                    listOf(
                        CashWithdrawal(
                            deductedBaseAmount = 20000L,
                            amountWithdrawn = 20000L,
                            remainingAmount = 10000L,
                            currency = currency
                        )
                    )
                )

                // When
                val result = useCase(groupId, currency).first()

                // Then: virtualBalance = 50000 - 15000 (non-cash only) - 20000 = 15000
                assertEquals(15000L, result.virtualBalance)
                // totalExpenses includes both cash + non-cash for UI
                assertEquals(25000L, result.totalExpenses)
            }

        @Test
        fun `reproduces the original double-deduction bug scenario`() = runTest {
            // Given: reproduces the issue from EXSHAPP-0415
            // Contribution: 15921 GBP (159.21)
            // Expenses: 10721 GBP total (107.21) — some paid with cash, some with card
            // Cash withdrawal: deducted 4547 GBP (45.47) from virtual pocket
            //
            // The old formula would give: 15921 - 10721 - 4547 = 653 (or negative
            // depending on exact amounts). With the fix, cash expenses (~45 GBP
            // worth) are excluded from virtualExpenses.

            val cashExpenseAmount = 4000L  // 40 GBP paid in cash
            val cardExpenseAmount = 6721L  // 67.21 GBP paid by card

            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 15921L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        groupAmount = cashExpenseAmount,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.CASH
                    ),
                    Expense(
                        groupAmount = cardExpenseAmount,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.CREDIT_CARD
                    )
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        deductedBaseAmount = 4547L,
                        amountWithdrawn = 4547L,
                        remainingAmount = 547L,
                        currency = currency
                    )
                )
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: virtualBalance = 15921 - 6721 (non-cash) - 4547 = 4653
            // This is positive! The old formula would give: 15921 - 10721 - 4547 = 653
            // (and with larger cash amounts it would go negative)
            assertEquals(4653L, result.virtualBalance)
            // totalExpenses still shows the full amount for UI
            assertEquals(10721L, result.totalExpenses)
        }

        @Test
        fun `all payment methods except CASH deduct from virtual balance`() = runTest {
            // Given: one expense per non-cash payment method
            val nonCashMethods = listOf(
                PaymentMethod.BIZUM,
                PaymentMethod.CREDIT_CARD,
                PaymentMethod.DEBIT_CARD,
                PaymentMethod.BANK_TRANSFER,
                PaymentMethod.PAYPAL,
                PaymentMethod.VENMO,
                PaymentMethod.OTHER
            )
            val expensePerMethod = 1000L
            val totalContributions = 100000L

            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = totalContributions, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                nonCashMethods.map { method ->
                    Expense(
                        groupAmount = expensePerMethod,
                        groupCurrency = currency,
                        paymentMethod = method
                    )
                }
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                emptyList()
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: all 7 non-cash expenses deduct from virtual balance
            val expectedVirtualExpenses = nonCashMethods.size * expensePerMethod
            assertEquals(
                totalContributions - expectedVirtualExpenses,
                result.virtualBalance
            )
        }

        @Test
        fun `empty data produces zero balance`() = runTest {
            // Given
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                emptyList()
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                emptyList()
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                emptyList()
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then
            assertEquals(0L, result.totalContributions)
            assertEquals(0L, result.totalExpenses)
            assertEquals(0L, result.virtualBalance)
            assertEquals(currency, result.currency)
            assertEquals(emptyMap<String, Long>(), result.cashBalances)
            assertEquals(emptyMap<String, Long>(), result.cashEquivalents)
            assertEquals(0L, result.totalCashEquivalent)
        }
    }

    @Nested
    inner class TotalCashEquivalentCalculation {

        @Test
        fun `totalCashEquivalent includes base currency cash at face value`() = runTest {
            // Given: 100 GBP remaining in cash (same as group currency)
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 50000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        deductedBaseAmount = 10000L,
                        amountWithdrawn = 10000L,
                        remainingAmount = 10000L,
                        currency = currency
                    )
                )
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: totalCashEquivalent = 10000 (base currency, face value)
            assertEquals(10000L, result.totalCashEquivalent)
        }

        @Test
        fun `totalCashEquivalent includes foreign cash converted proportionally`() = runTest {
            // Given: 5000 THB remaining out of 10000 THB withdrawn, which deducted 270 GBP
            // Proportional equivalent: (5000/10000) * 27000 = 13500
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 50000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        deductedBaseAmount = 27000L,
                        amountWithdrawn = 10000L,
                        remainingAmount = 5000L,
                        currency = "THB"
                    )
                )
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: totalCashEquivalent = 0 (no base currency cash) + 13500 (foreign equivalent)
            assertEquals(13500L, result.totalCashEquivalent)
        }

        @Test
        fun `totalCashEquivalent sums base currency and foreign cash`() = runTest {
            // Given: 100 GBP cash + 5000 THB (equivalent ~135 GBP)
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 100000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        deductedBaseAmount = 10000L,
                        amountWithdrawn = 10000L,
                        remainingAmount = 10000L,
                        currency = currency
                    ),
                    CashWithdrawal(
                        deductedBaseAmount = 27000L,
                        amountWithdrawn = 10000L,
                        remainingAmount = 5000L,
                        currency = "THB"
                    )
                )
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: totalCashEquivalent = 10000 (GBP) + 13500 (THB equivalent)
            assertEquals(23500L, result.totalCashEquivalent)
        }

        @Test
        fun `totalCashEquivalent is zero when no cash remains`() = runTest {
            // Given: withdrawal with zero remaining
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 50000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        deductedBaseAmount = 10000L,
                        amountWithdrawn = 10000L,
                        remainingAmount = 0L,
                        currency = "THB"
                    )
                )
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then
            assertEquals(0L, result.totalCashEquivalent)
            assertEquals(emptyMap<String, Long>(), result.cashBalances)
        }
    }

    @Nested
    inner class CashBalancesAndEquivalents {

        @Test
        fun `cashBalances groups remaining amounts by currency`() = runTest {
            // Given: two THB withdrawals
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 100000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        deductedBaseAmount = 10000L,
                        amountWithdrawn = 5000L,
                        remainingAmount = 3000L,
                        currency = "THB"
                    ),
                    CashWithdrawal(
                        deductedBaseAmount = 20000L,
                        amountWithdrawn = 10000L,
                        remainingAmount = 7000L,
                        currency = "THB"
                    )
                )
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then
            assertEquals(mapOf("THB" to 10000L), result.cashBalances)
        }

        @Test
        fun `cashBalances excludes currencies with zero remaining`() = runTest {
            // Given
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 100000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        deductedBaseAmount = 10000L,
                        amountWithdrawn = 5000L,
                        remainingAmount = 0L,
                        currency = "THB"
                    ),
                    CashWithdrawal(
                        deductedBaseAmount = 5000L,
                        amountWithdrawn = 5000L,
                        remainingAmount = 3000L,
                        currency = "EUR"
                    )
                )
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: only EUR remains
            assertEquals(mapOf("EUR" to 3000L), result.cashBalances)
        }

        @Test
        fun `cashEquivalents excludes base currency withdrawals`() = runTest {
            // Given: one GBP (base) and one EUR (foreign) withdrawal
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 100000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        deductedBaseAmount = 10000L,
                        amountWithdrawn = 10000L,
                        remainingAmount = 5000L,
                        currency = currency // base currency
                    ),
                    CashWithdrawal(
                        deductedBaseAmount = 20000L,
                        amountWithdrawn = 10000L,
                        remainingAmount = 5000L,
                        currency = "EUR" // foreign
                    )
                )
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: only EUR has an equivalent (foreign), GBP is base currency
            assertEquals(1, result.cashEquivalents.size)
            assertEquals(10000L, result.cashEquivalents["EUR"]) // (5000/10000) * 20000
        }
    }

    @Nested
    inner class ScheduledExpenses {

        @Test
        fun `future scheduled expense is excluded from totalExpenses`() = runTest {
            // Given: 1 regular expense + 1 future scheduled expense
            val futureDate = LocalDateTime.now().plusDays(10)
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 50000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        groupAmount = 5000L,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.CREDIT_CARD,
                        paymentStatus = PaymentStatus.FINISHED
                    ),
                    Expense(
                        groupAmount = 10000L,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.BANK_TRANSFER,
                        paymentStatus = PaymentStatus.SCHEDULED,
                        dueDate = futureDate
                    )
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                emptyList()
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: totalExpenses excludes the future scheduled expense
            assertEquals(5000L, result.totalExpenses)
        }

        @Test
        fun `future scheduled expense does not deduct from virtualBalance`() = runTest {
            // Given: 1 future scheduled credit card expense
            val futureDate = LocalDateTime.now().plusDays(5)
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 50000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        groupAmount = 10000L,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.CREDIT_CARD,
                        paymentStatus = PaymentStatus.SCHEDULED,
                        dueDate = futureDate
                    )
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                emptyList()
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: virtualBalance is unaffected by future scheduled expense
            assertEquals(50000L, result.virtualBalance)
            assertEquals(0L, result.totalExpenses)
        }

        @Test
        fun `future scheduled expense amount is reported as scheduledHoldAmount`() = runTest {
            // Given: one future scheduled expense of 10000 cents
            val futureDate = LocalDateTime.now().plusDays(7)
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 50000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        groupAmount = 10000L,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.BANK_TRANSFER,
                        paymentStatus = PaymentStatus.SCHEDULED,
                        dueDate = futureDate
                    )
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                emptyList()
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: scheduledHoldAmount equals the future scheduled expense amount
            assertEquals(10000L, result.scheduledHoldAmount)
        }

        @Test
        fun `multiple future scheduled expenses sum into scheduledHoldAmount`() = runTest {
            // Given: two future scheduled expenses
            val futureDate1 = LocalDateTime.now().plusDays(3)
            val futureDate2 = LocalDateTime.now().plusDays(14)
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 100000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        groupAmount = 8000L,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.CREDIT_CARD,
                        paymentStatus = PaymentStatus.SCHEDULED,
                        dueDate = futureDate1
                    ),
                    Expense(
                        groupAmount = 12000L,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.BANK_TRANSFER,
                        paymentStatus = PaymentStatus.SCHEDULED,
                        dueDate = futureDate2
                    )
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                emptyList()
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: hold amount = 8000 + 12000 = 20000
            assertEquals(20000L, result.scheduledHoldAmount)
            assertEquals(0L, result.totalExpenses)
            assertEquals(100000L, result.virtualBalance)
        }

        @Test
        fun `past scheduled expense is included normally in totalExpenses`() = runTest {
            // Given: a scheduled expense that is already past its due date
            val pastDate = LocalDateTime.now().minusDays(3)
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 50000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        groupAmount = 7500L,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.BANK_TRANSFER,
                        paymentStatus = PaymentStatus.SCHEDULED,
                        dueDate = pastDate
                    )
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                emptyList()
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: past scheduled expense is treated as effective (already due)
            assertEquals(7500L, result.totalExpenses)
            assertEquals(0L, result.scheduledHoldAmount)
        }

        @Test
        fun `today scheduled expense is included normally in totalExpenses`() = runTest {
            // Given: a scheduled expense due today (not strictly after today)
            val todayNoon = LocalDateTime.now().withHour(12)
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 50000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        groupAmount = 6000L,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.CREDIT_CARD,
                        paymentStatus = PaymentStatus.SCHEDULED,
                        dueDate = todayNoon
                    )
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                emptyList()
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: today's scheduled expense is included (not future)
            assertEquals(6000L, result.totalExpenses)
            assertEquals(0L, result.scheduledHoldAmount)
        }

        @Test
        fun `scheduled expense with null dueDate is treated as effective`() = runTest {
            // Given: SCHEDULED expense with no due date (edge case)
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 50000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        groupAmount = 3000L,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.CREDIT_CARD,
                        paymentStatus = PaymentStatus.SCHEDULED,
                        dueDate = null
                    )
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                emptyList()
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then: no dueDate → partition condition is false → treated as effective
            assertEquals(3000L, result.totalExpenses)
            assertEquals(0L, result.scheduledHoldAmount)
        }

        @Test
        fun `scheduledHoldAmount is zero when no future scheduled expenses`() = runTest {
            // Given: only regular finished expenses
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(amount = 50000L, currency = currency))
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        groupAmount = 5000L,
                        groupCurrency = currency,
                        paymentMethod = PaymentMethod.CREDIT_CARD,
                        paymentStatus = PaymentStatus.FINISHED
                    )
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                emptyList()
            )

            // When
            val result = useCase(groupId, currency).first()

            // Then
            assertEquals(0L, result.scheduledHoldAmount)
            assertEquals(5000L, result.totalExpenses)
        }
    }
}


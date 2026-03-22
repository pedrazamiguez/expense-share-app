package es.pedrazamiguez.expenseshareapp.domain.usecase.balance
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.AddOn
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
@DisplayName("GetMemberBalancesFlowUseCase")
class GetMemberBalancesFlowUseCaseTest {
    private lateinit var useCase: GetMemberBalancesFlowUseCase
    private val groupId = "group-123"
    private val groupMemberIds = listOf("user-1", "user-2", "user-3", "user-4")

    @BeforeEach
    fun setUp() {
        useCase = GetMemberBalancesFlowUseCase()
    }
    private fun compute(
        contributions: List<Contribution> = emptyList(),
        withdrawals: List<CashWithdrawal> = emptyList(),
        expenses: List<Expense> = emptyList(),
        subunits: List<Subunit> = emptyList(),
        memberIds: List<String> = groupMemberIds,
        groupCurrency: String = "EUR"
    ) = useCase.computeMemberBalances(contributions, withdrawals, expenses, subunits, memberIds, groupCurrency)

    @Nested
    @DisplayName("Zero data / edge cases")
    inner class ZeroData {
        @Test
        fun `all members have zero balances when no data exists`() {
            val result = compute()
            assertEquals(4, result.size)
            result.forEach { balance ->
                assertEquals(0L, balance.contributed)
                assertEquals(0L, balance.withdrawn)
                assertEquals(0L, balance.cashSpent)
                assertEquals(0L, balance.nonCashSpent)
                assertEquals(0L, balance.totalSpent)
                assertEquals(0L, balance.pocketBalance)
                assertEquals(0L, balance.cashInHand)
            }
        }

        @Test
        fun `returns empty list when group has no members and no data`() {
            val result = compute(memberIds = emptyList())
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("Contribution attribution")
    inner class ContributionAttribution {
        @Test
        fun `individual contributions attributed entirely to the contributor`() {
            val contributions = listOf(
                Contribution(userId = "user-1", contributionScope = PayerType.USER, amount = 5000L),
                Contribution(userId = "user-2", contributionScope = PayerType.USER, amount = 3000L)
            )
            val result = compute(contributions = contributions)
            val balanceMap = result.associateBy { it.userId }
            assertEquals(5000L, balanceMap["user-1"]!!.contributed)
            assertEquals(3000L, balanceMap["user-2"]!!.contributed)
            assertEquals(0L, balanceMap["user-3"]!!.contributed)
            assertEquals(0L, balanceMap["user-4"]!!.contributed)
        }

        @Test
        fun `GROUP-scoped contribution distributed equally among all members`() {
            val contributions = listOf(
                Contribution(userId = "user-1", contributionScope = PayerType.GROUP, amount = 10000L)
            )
            val result = compute(contributions = contributions)
            val balanceMap = result.associateBy { it.userId }
            assertEquals(2500L, balanceMap["user-1"]!!.contributed)
            assertEquals(2500L, balanceMap["user-2"]!!.contributed)
            assertEquals(2500L, balanceMap["user-3"]!!.contributed)
            assertEquals(2500L, balanceMap["user-4"]!!.contributed)
            assertEquals(10000L, balanceMap.values.sumOf { it.contributed })
        }

        @Test
        fun `GROUP-scoped contribution remainder allocated correctly`() {
            // 101 / 4 = 25 each + 1 remainder unit distributed to first member
            val contributions = listOf(
                Contribution(userId = "user-1", contributionScope = PayerType.GROUP, amount = 101L)
            )
            val result = compute(contributions = contributions)
            val balanceMap = result.associateBy { it.userId }
            val totalContributed = balanceMap.values.sumOf { it.contributed }
            assertEquals(101L, totalContributed)
        }

        @Test
        fun `subunit contribution distributed by memberShares (50-50 couple)`() {
            val subunit = Subunit(
                id = "sub-1",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )
            val contributions = listOf(
                Contribution(
                    userId = "user-1",
                    contributionScope = PayerType.SUBUNIT,
                    subunitId = "sub-1",
                    amount = 10000L
                )
            )
            val result = compute(contributions = contributions, subunits = listOf(subunit))
            val balanceMap = result.associateBy { it.userId }
            assertEquals(5000L, balanceMap["user-1"]!!.contributed)
            assertEquals(5000L, balanceMap["user-2"]!!.contributed)
        }

        @Test
        fun `subunit contribution with unequal shares (40-30-30 family)`() {
            val subunit = Subunit(
                id = "sub-fam",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2", "user-3"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.4"),
                    "user-2" to BigDecimal("0.3"),
                    "user-3" to BigDecimal("0.3")
                )
            )
            val contributions = listOf(
                Contribution(
                    userId = "user-1",
                    contributionScope = PayerType.SUBUNIT,
                    subunitId = "sub-fam",
                    amount = 10000L
                )
            )
            val result = compute(contributions = contributions, subunits = listOf(subunit))
            val balanceMap = result.associateBy { it.userId }
            assertEquals(4000L, balanceMap["user-1"]!!.contributed)
            assertEquals(3000L, balanceMap["user-2"]!!.contributed)
            assertEquals(3000L, balanceMap["user-3"]!!.contributed)
            assertEquals(10000L, balanceMap.values.sumOf { it.contributed })
        }

        @Test
        fun `mixed individual and subunit contributions`() {
            val subunit = Subunit(
                id = "sub-1",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )
            val contributions = listOf(
                Contribution(userId = "user-3", contributionScope = PayerType.USER, amount = 5000L),
                Contribution(
                    userId = "user-1",
                    contributionScope = PayerType.SUBUNIT,
                    subunitId = "sub-1",
                    amount = 10000L
                )
            )
            val result = compute(contributions = contributions, subunits = listOf(subunit))
            val balanceMap = result.associateBy { it.userId }
            assertEquals(5000L, balanceMap["user-1"]!!.contributed)
            assertEquals(5000L, balanceMap["user-2"]!!.contributed)
            assertEquals(5000L, balanceMap["user-3"]!!.contributed)
        }

        @Test
        fun `subunit contribution falls back to contributor when subunit not found`() {
            val contributions = listOf(
                Contribution(
                    userId = "user-1",
                    contributionScope = PayerType.SUBUNIT,
                    subunitId = "nonexistent-sub",
                    amount = 8000L
                )
            )
            val result = compute(contributions = contributions)
            val balanceMap = result.associateBy { it.userId }
            assertEquals(8000L, balanceMap["user-1"]!!.contributed)
        }

        @Test
        fun `rounding remainder allocated correctly for indivisible amounts`() {
            val thirdShare = BigDecimal.ONE.divide(BigDecimal(3), 10, RoundingMode.DOWN)
            val subunit = Subunit(
                id = "sub-3",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2", "user-3"),
                memberShares = mapOf(
                    "user-1" to thirdShare,
                    "user-2" to thirdShare,
                    "user-3" to thirdShare
                )
            )
            val contributions = listOf(
                Contribution(
                    userId = "user-1",
                    contributionScope = PayerType.SUBUNIT,
                    subunitId = "sub-3",
                    amount = 100L
                )
            )
            val result = compute(contributions = contributions, subunits = listOf(subunit))
            val balanceMap = result.associateBy { it.userId }
            val totalContributed = balanceMap.values.sumOf { it.contributed }
            assertEquals(100L, totalContributed)
            assertTrue(balanceMap["user-1"]!!.contributed >= 33L)
            assertTrue(balanceMap["user-2"]!!.contributed >= 33L)
            assertTrue(balanceMap["user-3"]!!.contributed >= 33L)
        }

        @Test
        fun `mixed GROUP, SUBUNIT, and USER contributions`() {
            val subunit = Subunit(
                id = "sub-1",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )
            val contributions = listOf(
                Contribution(userId = "user-1", contributionScope = PayerType.GROUP, amount = 4000L),
                Contribution(
                    userId = "user-1",
                    contributionScope = PayerType.SUBUNIT,
                    subunitId = "sub-1",
                    amount = 2000L
                ),
                Contribution(userId = "user-3", contributionScope = PayerType.USER, amount = 500L)
            )
            val result = compute(contributions = contributions, subunits = listOf(subunit))
            val balanceMap = result.associateBy { it.userId }
            // GROUP: 4000 / 4 = 1000 each
            // SUBUNIT: 2000 → user-1: 1000, user-2: 1000
            // USER: user-3: 500
            assertEquals(1000L + 1000L, balanceMap["user-1"]!!.contributed) // 2000
            assertEquals(1000L + 1000L, balanceMap["user-2"]!!.contributed) // 2000
            assertEquals(1000L + 500L, balanceMap["user-3"]!!.contributed) // 1500
            assertEquals(1000L, balanceMap["user-4"]!!.contributed) // 1000
        }
    }

    @Nested
    @DisplayName("Withdrawal attribution")
    inner class WithdrawalAttribution {
        @Test
        fun `GROUP-scoped withdrawal split equally among all members`() {
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.GROUP,
                    deductedBaseAmount = 10000L
                )
            )
            val result = compute(withdrawals = withdrawals)
            val balanceMap = result.associateBy { it.userId }
            assertEquals(2500L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(2500L, balanceMap["user-2"]!!.withdrawn)
            assertEquals(2500L, balanceMap["user-3"]!!.withdrawn)
            assertEquals(2500L, balanceMap["user-4"]!!.withdrawn)
        }

        @Test
        fun `GROUP-scoped withdrawal with remainder distributed correctly`() {
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.GROUP,
                    deductedBaseAmount = 10001L
                )
            )
            val result = compute(withdrawals = withdrawals)
            val totalWithdrawn = result.sumOf { it.withdrawn }
            assertEquals(10001L, totalWithdrawn)
        }

        @Test
        fun `SUBUNIT-scoped withdrawal distributed by memberShares`() {
            val subunit = Subunit(
                id = "sub-1",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.6"),
                    "user-2" to BigDecimal("0.4")
                )
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.SUBUNIT,
                    subunitId = "sub-1",
                    deductedBaseAmount = 10000L
                )
            )
            val result = compute(withdrawals = withdrawals, subunits = listOf(subunit))
            val balanceMap = result.associateBy { it.userId }
            assertEquals(6000L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(4000L, balanceMap["user-2"]!!.withdrawn)
            assertEquals(0L, balanceMap["user-3"]!!.withdrawn)
            assertEquals(0L, balanceMap["user-4"]!!.withdrawn)
        }

        @Test
        fun `USER-scoped withdrawal attributed entirely to withdrawer`() {
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-3",
                    withdrawalScope = PayerType.USER,
                    deductedBaseAmount = 500L
                )
            )
            val result = compute(withdrawals = withdrawals)
            val balanceMap = result.associateBy { it.userId }
            assertEquals(500L, balanceMap["user-3"]!!.withdrawn)
            assertEquals(0L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(0L, balanceMap["user-2"]!!.withdrawn)
            assertEquals(0L, balanceMap["user-4"]!!.withdrawn)
        }

        @Test
        fun `mixed withdrawal scopes in one group`() {
            val subunit = Subunit(
                id = "sub-1",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.GROUP,
                    deductedBaseAmount = 20000L
                ),
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.SUBUNIT,
                    subunitId = "sub-1",
                    deductedBaseAmount = 10000L
                ),
                CashWithdrawal(
                    withdrawnBy = "user-3",
                    withdrawalScope = PayerType.USER,
                    deductedBaseAmount = 500L
                )
            )
            val result = compute(withdrawals = withdrawals, subunits = listOf(subunit))
            val balanceMap = result.associateBy { it.userId }
            assertEquals(10000L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(10000L, balanceMap["user-2"]!!.withdrawn)
            assertEquals(5500L, balanceMap["user-3"]!!.withdrawn)
            assertEquals(5000L, balanceMap["user-4"]!!.withdrawn)
        }

        @Test
        fun `SUBUNIT-scoped withdrawal falls back to withdrawer when subunit not found`() {
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.SUBUNIT,
                    subunitId = "nonexistent",
                    deductedBaseAmount = 3000L
                )
            )
            val result = compute(withdrawals = withdrawals)
            val balanceMap = result.associateBy { it.userId }
            assertEquals(3000L, balanceMap["user-1"]!!.withdrawn)
        }
    }

    @Nested
    @DisplayName("Expense split attribution")
    inner class ExpenseSplitAttribution {
        @Test
        fun `non-cash expense splits go to nonCashSpent`() {
            val expenses = listOf(
                Expense(
                    id = "exp-1",
                    groupId = groupId,
                    sourceAmount = 10000L,
                    groupAmount = 10000L,
                    splitType = SplitType.EQUAL,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 2500L),
                        ExpenseSplit(userId = "user-2", amountCents = 2500L),
                        ExpenseSplit(userId = "user-3", amountCents = 2500L),
                        ExpenseSplit(userId = "user-4", amountCents = 2500L)
                    )
                )
            )
            val result = compute(expenses = expenses)
            val balanceMap = result.associateBy { it.userId }
            assertEquals(2500L, balanceMap["user-1"]!!.nonCashSpent)
            assertEquals(2500L, balanceMap["user-2"]!!.nonCashSpent)
            assertEquals(2500L, balanceMap["user-3"]!!.nonCashSpent)
            assertEquals(2500L, balanceMap["user-4"]!!.nonCashSpent)
            // Cash spent should be zero
            result.forEach { assertEquals(0L, it.cashSpent) }
        }

        @Test
        fun `cash expense splits go to cashSpent`() {
            val expenses = listOf(
                Expense(
                    id = "exp-cash",
                    groupId = groupId,
                    sourceAmount = 10000L,
                    groupAmount = 10000L,
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 5000L),
                        ExpenseSplit(userId = "user-2", amountCents = 5000L)
                    )
                )
            )
            val result = compute(expenses = expenses)
            val balanceMap = result.associateBy { it.userId }
            assertEquals(5000L, balanceMap["user-1"]!!.cashSpent)
            assertEquals(5000L, balanceMap["user-2"]!!.cashSpent)
            // Non-cash spent should be zero
            result.forEach { assertEquals(0L, it.nonCashSpent) }
        }

        @Test
        fun `mixed cash and non-cash expenses attributed correctly`() {
            val expenses = listOf(
                Expense(
                    id = "exp-cash",
                    sourceAmount = 5000L,
                    groupAmount = 5000L,
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 3000L),
                        ExpenseSplit(userId = "user-2", amountCents = 2000L)
                    )
                ),
                Expense(
                    id = "exp-card",
                    sourceAmount = 5000L,
                    groupAmount = 5000L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 1500L),
                        ExpenseSplit(userId = "user-3", amountCents = 3500L)
                    )
                )
            )
            val result = compute(expenses = expenses)
            val balanceMap = result.associateBy { it.userId }
            // user-1: cashSpent=3000, nonCashSpent=1500, totalSpent=4500
            assertEquals(3000L, balanceMap["user-1"]!!.cashSpent)
            assertEquals(1500L, balanceMap["user-1"]!!.nonCashSpent)
            assertEquals(4500L, balanceMap["user-1"]!!.totalSpent)
            // user-2: cashSpent=2000, nonCashSpent=0, totalSpent=2000
            assertEquals(2000L, balanceMap["user-2"]!!.cashSpent)
            assertEquals(0L, balanceMap["user-2"]!!.nonCashSpent)
            assertEquals(2000L, balanceMap["user-2"]!!.totalSpent)
            // user-3: cashSpent=0, nonCashSpent=3500, totalSpent=3500
            assertEquals(0L, balanceMap["user-3"]!!.cashSpent)
            assertEquals(3500L, balanceMap["user-3"]!!.nonCashSpent)
            assertEquals(3500L, balanceMap["user-3"]!!.totalSpent)
            // user-4: nothing
            assertEquals(0L, balanceMap["user-4"]!!.totalSpent)
        }

        @Test
        fun `default payment method OTHER routes to nonCashSpent`() {
            // Expense without explicit paymentMethod → defaults to OTHER (non-cash)
            val expenses = listOf(
                Expense(
                    id = "exp-default",
                    sourceAmount = 6000L,
                    groupAmount = 6000L,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 6000L)
                    )
                )
            )
            val result = compute(expenses = expenses)
            val balanceMap = result.associateBy { it.userId }
            assertEquals(0L, balanceMap["user-1"]!!.cashSpent)
            assertEquals(6000L, balanceMap["user-1"]!!.nonCashSpent)
            assertEquals(6000L, balanceMap["user-1"]!!.totalSpent)
        }

        @Test
        fun `excluded splits are not counted`() {
            val expenses = listOf(
                Expense(
                    id = "exp-1",
                    sourceAmount = 10000L,
                    groupAmount = 10000L,
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 5000L),
                        ExpenseSplit(userId = "user-2", amountCents = 5000L, isExcluded = true)
                    )
                )
            )
            val result = compute(expenses = expenses)
            val balanceMap = result.associateBy { it.userId }
            assertEquals(5000L, balanceMap["user-1"]!!.cashSpent)
            assertEquals(0L, balanceMap["user-2"]!!.cashSpent)
        }

        @Test
        fun `foreign currency expense splits converted to group currency`() {
            val expenses = listOf(
                Expense(
                    id = "exp-thb",
                    sourceAmount = 100000L,
                    groupAmount = 2683L,
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 50000L),
                        ExpenseSplit(userId = "user-2", amountCents = 50000L)
                    )
                )
            )
            val result = compute(expenses = expenses)
            val balanceMap = result.associateBy { it.userId }
            assertEquals(1342L, balanceMap["user-1"]!!.cashSpent)
            assertEquals(1342L, balanceMap["user-2"]!!.cashSpent)
            val totalCashSpent = result.sumOf { it.cashSpent }
            assertTrue(totalCashSpent in 2683L..2684L)
        }

        @Test
        fun `mixed same-currency and foreign-currency expenses with different payment methods`() {
            val expenses = listOf(
                Expense(
                    id = "exp-eur",
                    sourceAmount = 4000L,
                    groupAmount = 4000L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 2000L),
                        ExpenseSplit(userId = "user-2", amountCents = 2000L)
                    )
                ),
                Expense(
                    id = "exp-thb",
                    sourceAmount = 100000L,
                    groupAmount = 2700L,
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 50000L),
                        ExpenseSplit(userId = "user-2", amountCents = 50000L)
                    )
                )
            )
            val result = compute(expenses = expenses)
            val balanceMap = result.associateBy { it.userId }
            // user-1: nonCashSpent=2000 (EUR card), cashSpent=1350 (THB cash), totalSpent=3350
            assertEquals(1350L, balanceMap["user-1"]!!.cashSpent)
            assertEquals(2000L, balanceMap["user-1"]!!.nonCashSpent)
            assertEquals(3350L, balanceMap["user-1"]!!.totalSpent)
            // user-2: same
            assertEquals(1350L, balanceMap["user-2"]!!.cashSpent)
            assertEquals(2000L, balanceMap["user-2"]!!.nonCashSpent)
            assertEquals(3350L, balanceMap["user-2"]!!.totalSpent)
        }

        @Test
        fun `zero sourceAmount expense produces zero spent`() {
            val expenses = listOf(
                Expense(
                    id = "exp-zero",
                    sourceAmount = 0L,
                    groupAmount = 5000L,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 2500L)
                    )
                )
            )
            val result = compute(expenses = expenses)
            val balanceMap = result.associateBy { it.userId }
            assertEquals(0L, balanceMap["user-1"]!!.totalSpent)
        }
    }

    @Nested
    @DisplayName("Pocket balance and cash in hand calculation")
    inner class BalanceCalculation {
        @Test
        fun `pocketBalance equals contributed minus withdrawn minus nonCashSpent`() {
            val subunit = Subunit(
                id = "sub-1",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )
            val contributions = listOf(
                Contribution(
                    userId = "user-1",
                    contributionScope = PayerType.SUBUNIT,
                    subunitId = "sub-1",
                    amount = 10000L
                ),
                Contribution(userId = "user-3", contributionScope = PayerType.USER, amount = 5000L)
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.GROUP,
                    deductedBaseAmount = 4000L
                )
            )
            // Non-cash expense: card payment
            val expenses = listOf(
                Expense(
                    id = "exp-1",
                    sourceAmount = 8000L,
                    groupAmount = 8000L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 2000L),
                        ExpenseSplit(userId = "user-2", amountCents = 2000L),
                        ExpenseSplit(userId = "user-3", amountCents = 2000L),
                        ExpenseSplit(userId = "user-4", amountCents = 2000L)
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                expenses = expenses,
                subunits = listOf(subunit)
            )
            val balanceMap = result.associateBy { it.userId }
            // pocketBalance = contributed - withdrawn - nonCashSpent
            // user-1: 5000 - 1000 - 2000 = 2000
            assertEquals(2000L, balanceMap["user-1"]!!.pocketBalance)
            // user-2: 5000 - 1000 - 2000 = 2000
            assertEquals(2000L, balanceMap["user-2"]!!.pocketBalance)
            // user-3: 5000 - 1000 - 2000 = 2000
            assertEquals(2000L, balanceMap["user-3"]!!.pocketBalance)
            // user-4: 0 - 1000 - 2000 = -3000
            assertEquals(-3000L, balanceMap["user-4"]!!.pocketBalance)
            // Sum of pocketBalances = totalContributed - totalWithdrawn - totalNonCashSpent
            val totalPocket = result.sumOf { it.pocketBalance }
            assertEquals(15000L - 4000L - 8000L, totalPocket)
        }

        @Test
        fun `cashInHand equals withdrawn minus cashSpent`() {
            val contributions = listOf(
                Contribution(userId = "user-1", amount = 10000L)
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    deductedBaseAmount = 5000L
                )
            )
            val expenses = listOf(
                Expense(
                    id = "exp-1",
                    sourceAmount = 3000L,
                    groupAmount = 3000L,
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 3000L)
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                expenses = expenses
            )
            val balanceMap = result.associateBy { it.userId }
            assertEquals(5000L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(3000L, balanceMap["user-1"]!!.cashSpent)
            assertEquals(2000L, balanceMap["user-1"]!!.cashInHand)
            // pocketBalance = contributed - withdrawn - nonCashSpent = 10000 - 5000 - 0 = 5000
            assertEquals(5000L, balanceMap["user-1"]!!.pocketBalance)
        }

        @Test
        fun `negative pocketBalance indicates member overdrew from pocket`() {
            val contributions = listOf(
                Contribution(userId = "user-1", amount = 1000L)
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    deductedBaseAmount = 5000L
                )
            )
            val result = compute(contributions = contributions, withdrawals = withdrawals)
            val balanceMap = result.associateBy { it.userId }
            // pocketBalance = 1000 - 5000 - 0 = -4000
            assertEquals(-4000L, balanceMap["user-1"]!!.pocketBalance)
        }

        @Test
        fun `member with only non-cash expenses has zero cashInHand`() {
            val expenses = listOf(
                Expense(
                    id = "exp-1",
                    sourceAmount = 5000L,
                    groupAmount = 5000L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 5000L)
                    )
                )
            )
            val result = compute(expenses = expenses)
            val balanceMap = result.associateBy { it.userId }
            // pocketBalance = 0 - 0 - 5000 = -5000
            assertEquals(-5000L, balanceMap["user-1"]!!.pocketBalance)
            assertEquals(5000L, balanceMap["user-1"]!!.nonCashSpent)
            assertEquals(0L, balanceMap["user-1"]!!.cashInHand)
        }

        @Test
        fun `cash expense does not reduce pocketBalance`() {
            // Cash expense is funded from physical cash (withdrawals),
            // not from the virtual pocket — only nonCashSpent reduces pocket.
            val contributions = listOf(
                Contribution(userId = "user-1", amount = 10000L)
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    deductedBaseAmount = 5000L
                )
            )
            val expenses = listOf(
                Expense(
                    id = "exp-cash",
                    sourceAmount = 3000L,
                    groupAmount = 3000L,
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 3000L)
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                expenses = expenses
            )
            val balanceMap = result.associateBy { it.userId }
            // pocketBalance = 10000 - 5000 - 0 = 5000 (cash expense does NOT reduce pocket)
            assertEquals(5000L, balanceMap["user-1"]!!.pocketBalance)
            // cashInHand = 5000 - 3000 = 2000 (cash expense reduces physical cash)
            assertEquals(2000L, balanceMap["user-1"]!!.cashInHand)
        }

        @Test
        fun `non-cash expense reduces pocketBalance but not cashInHand`() {
            val contributions = listOf(
                Contribution(userId = "user-1", amount = 10000L)
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    deductedBaseAmount = 5000L
                )
            )
            val expenses = listOf(
                Expense(
                    id = "exp-card",
                    sourceAmount = 3000L,
                    groupAmount = 3000L,
                    paymentMethod = PaymentMethod.BIZUM,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 3000L)
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                expenses = expenses
            )
            val balanceMap = result.associateBy { it.userId }
            // pocketBalance = 10000 - 5000 - 3000 = 2000 (non-cash reduces pocket)
            assertEquals(2000L, balanceMap["user-1"]!!.pocketBalance)
            // cashInHand = 5000 - 0 = 5000 (non-cash does NOT reduce physical cash)
            assertEquals(5000L, balanceMap["user-1"]!!.cashInHand)
        }
    }

    @Nested
    @DisplayName("Full scenario — wiki example")
    inner class FullScenario {
        @Test
        fun `solo users plus couples plus families in one group`() {
            val couple = Subunit(
                id = "sub-couple",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )
            val contributions = listOf(
                Contribution(
                    userId = "user-1",
                    contributionScope = PayerType.SUBUNIT,
                    subunitId = "sub-couple",
                    amount = 10000L
                ),
                Contribution(userId = "user-3", contributionScope = PayerType.USER, amount = 5000L),
                Contribution(userId = "user-4", contributionScope = PayerType.USER, amount = 5000L)
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-3",
                    withdrawalScope = PayerType.GROUP,
                    deductedBaseAmount = 20000L
                ),
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.SUBUNIT,
                    subunitId = "sub-couple",
                    deductedBaseAmount = 5000L
                ),
                CashWithdrawal(
                    withdrawnBy = "user-3",
                    withdrawalScope = PayerType.USER,
                    deductedBaseAmount = 500L
                )
            )
            // Cash expense
            val expenses = listOf(
                Expense(
                    id = "exp-dinner",
                    sourceAmount = 12000L,
                    groupAmount = 12000L,
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 3000L),
                        ExpenseSplit(userId = "user-2", amountCents = 3000L),
                        ExpenseSplit(userId = "user-3", amountCents = 3000L),
                        ExpenseSplit(userId = "user-4", amountCents = 3000L)
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                expenses = expenses,
                subunits = listOf(couple)
            )
            val balanceMap = result.associateBy { it.userId }

            // user-1: contributed=5000, withdrawn=7500, cashSpent=3000
            assertEquals(5000L, balanceMap["user-1"]!!.contributed)
            assertEquals(7500L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(3000L, balanceMap["user-1"]!!.cashSpent)
            assertEquals(0L, balanceMap["user-1"]!!.nonCashSpent)
            assertEquals(7500L - 3000L, balanceMap["user-1"]!!.cashInHand)
            // pocketBalance = 5000 - 7500 - 0 = -2500
            assertEquals(5000L - 7500L, balanceMap["user-1"]!!.pocketBalance)

            // user-2: contributed=5000, withdrawn=7500, cashSpent=3000
            assertEquals(5000L, balanceMap["user-2"]!!.contributed)
            assertEquals(7500L, balanceMap["user-2"]!!.withdrawn)
            assertEquals(3000L, balanceMap["user-2"]!!.cashSpent)
            assertEquals(4500L, balanceMap["user-2"]!!.cashInHand)
            assertEquals(-2500L, balanceMap["user-2"]!!.pocketBalance)

            // user-3: contributed=5000, withdrawn=5500, cashSpent=3000
            assertEquals(5000L, balanceMap["user-3"]!!.contributed)
            assertEquals(5500L, balanceMap["user-3"]!!.withdrawn)
            assertEquals(3000L, balanceMap["user-3"]!!.cashSpent)
            assertEquals(2500L, balanceMap["user-3"]!!.cashInHand)
            assertEquals(-500L, balanceMap["user-3"]!!.pocketBalance)

            // user-4: contributed=5000, withdrawn=5000, cashSpent=3000
            assertEquals(5000L, balanceMap["user-4"]!!.contributed)
            assertEquals(5000L, balanceMap["user-4"]!!.withdrawn)
            assertEquals(3000L, balanceMap["user-4"]!!.cashSpent)
            assertEquals(2000L, balanceMap["user-4"]!!.cashInHand)
            assertEquals(0L, balanceMap["user-4"]!!.pocketBalance)

            // Invariant: Σ pocketBalance = totalContributed - totalWithdrawn - totalNonCashSpent
            val totalContributed = result.sumOf { it.contributed }
            val totalWithdrawn = result.sumOf { it.withdrawn }
            val totalNonCashSpent = result.sumOf { it.nonCashSpent }
            assertEquals(totalContributed - totalWithdrawn - totalNonCashSpent, result.sumOf { it.pocketBalance })
            // Invariant: Σ cashInHand = totalWithdrawn - totalCashSpent
            val totalCashSpent = result.sumOf { it.cashSpent }
            assertEquals(totalWithdrawn - totalCashSpent, result.sumOf { it.cashInHand })
        }

        @Test
        fun `mixed cash and non-cash expenses in multi-currency trip`() {
            val twoMembers = listOf("user-1", "user-2")
            val contributions = listOf(
                Contribution(userId = "user-1", amount = 10000L)
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.GROUP,
                    deductedBaseAmount = 2700L
                )
            )
            val expenses = listOf(
                // Non-cash EUR dinner (card)
                Expense(
                    id = "exp-dinner-eur",
                    sourceAmount = 4000L,
                    groupAmount = 4000L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 2000L),
                        ExpenseSplit(userId = "user-2", amountCents = 2000L)
                    )
                ),
                // Cash THB taxi
                Expense(
                    id = "exp-taxi-thb",
                    sourceAmount = 100000L,
                    groupAmount = 2700L,
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 50000L),
                        ExpenseSplit(userId = "user-2", amountCents = 50000L)
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                expenses = expenses,
                memberIds = twoMembers
            )
            val balanceMap = result.associateBy { it.userId }

            // user-1: contributed=10000, withdrawn=1350, nonCashSpent=2000, cashSpent=1350
            assertEquals(10000L, balanceMap["user-1"]!!.contributed)
            assertEquals(1350L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(2000L, balanceMap["user-1"]!!.nonCashSpent)
            assertEquals(1350L, balanceMap["user-1"]!!.cashSpent)
            assertEquals(3350L, balanceMap["user-1"]!!.totalSpent)
            // pocketBalance = 10000 - 1350 - 2000 = 6650
            assertEquals(6650L, balanceMap["user-1"]!!.pocketBalance)
            // cashInHand = 1350 - 1350 = 0
            assertEquals(0L, balanceMap["user-1"]!!.cashInHand)

            // user-2: contributed=0, withdrawn=1350, nonCashSpent=2000, cashSpent=1350
            assertEquals(0L, balanceMap["user-2"]!!.contributed)
            assertEquals(1350L, balanceMap["user-2"]!!.withdrawn)
            assertEquals(2000L, balanceMap["user-2"]!!.nonCashSpent)
            assertEquals(1350L, balanceMap["user-2"]!!.cashSpent)
            assertEquals(3350L, balanceMap["user-2"]!!.totalSpent)
            // pocketBalance = 0 - 1350 - 2000 = -3350
            assertEquals(-3350L, balanceMap["user-2"]!!.pocketBalance)
            // cashInHand = 1350 - 1350 = 0
            assertEquals(0L, balanceMap["user-2"]!!.cashInHand)
        }

        @Test
        fun `issue 612 scenario - cash expense does not affect pocket, non-cash does`() {
            // This test verifies the exact scenario from issue #612:
            // A member has 20€ withdrawn and pays 15€ credit card dinner.
            // Current (WRONG): available = 20 - 15 = 5€
            // Correct: cashInHand = 20 - 0 = 20€, pocketBalance = contributed - 20 - 15
            val twoMembers = listOf("user-1", "user-2")
            val contributions = listOf(
                Contribution(userId = "user-1", amount = 5000L), // 50€
                Contribution(userId = "user-2", amount = 5000L) // 50€
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    deductedBaseAmount = 2000L // 20€
                )
            )
            val expenses = listOf(
                Expense(
                    id = "exp-dinner",
                    sourceAmount = 1500L,
                    groupAmount = 1500L,
                    paymentMethod = PaymentMethod.CREDIT_CARD, // Non-cash!
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 1500L) // 15€
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                expenses = expenses,
                memberIds = twoMembers
            )
            val balanceMap = result.associateBy { it.userId }

            // user-1: cashInHand = 20 - 0 = 20€ (cash is untouched!)
            assertEquals(2000L, balanceMap["user-1"]!!.cashInHand)
            // user-1: pocketBalance = 50 - 20 - 15 = 15€
            assertEquals(1500L, balanceMap["user-1"]!!.pocketBalance)
            // user-1: nonCashSpent = 15€
            assertEquals(1500L, balanceMap["user-1"]!!.nonCashSpent)
            // user-1: cashSpent = 0€
            assertEquals(0L, balanceMap["user-1"]!!.cashSpent)
        }
    }

    @Nested
    @DisplayName("distributeByShares — static helper")
    inner class DistributeByShares {
        @Test
        fun `empty shares returns empty map`() {
            val result = GetMemberBalancesFlowUseCase.distributeByShares(10000L, emptyMap())
            assertTrue(result.isEmpty())
        }

        @Test
        fun `single member gets full amount`() {
            val result = GetMemberBalancesFlowUseCase.distributeByShares(
                10000L,
                mapOf("user-1" to BigDecimal.ONE)
            )
            assertEquals(10000L, result["user-1"])
        }

        @Test
        fun `two members 50-50`() {
            val result = GetMemberBalancesFlowUseCase.distributeByShares(
                10000L,
                mapOf("user-1" to BigDecimal("0.5"), "user-2" to BigDecimal("0.5"))
            )
            assertEquals(5000L, result["user-1"])
            assertEquals(5000L, result["user-2"])
        }

        @Test
        fun `total distributed equals original amount despite rounding`() {
            val thirdShare = BigDecimal.ONE.divide(BigDecimal(3), 10, RoundingMode.DOWN)
            val result = GetMemberBalancesFlowUseCase.distributeByShares(
                10000L,
                mapOf(
                    "user-1" to thirdShare,
                    "user-2" to thirdShare,
                    "user-3" to thirdShare
                )
            )
            assertEquals(10000L, result.values.sum())
        }

        @Test
        fun `remainder allocation is deterministic regardless of map insertion order`() {
            val thirdShare = BigDecimal.ONE.divide(BigDecimal(3), 10, RoundingMode.DOWN)
            val mapABC = linkedMapOf(
                "alice" to thirdShare,
                "bob" to thirdShare,
                "charlie" to thirdShare
            )
            val mapCBA = linkedMapOf(
                "charlie" to thirdShare,
                "bob" to thirdShare,
                "alice" to thirdShare
            )
            val resultABC = GetMemberBalancesFlowUseCase.distributeByShares(100L, mapABC)
            val resultCBA = GetMemberBalancesFlowUseCase.distributeByShares(100L, mapCBA)
            assertEquals(resultABC, resultCBA)
            assertEquals(100L, resultABC.values.sum())
        }
    }

    @Nested
    @DisplayName("distributeEvenly — static helper")
    inner class DistributeEvenly {
        @Test
        fun `empty members returns empty map`() {
            val result = GetMemberBalancesFlowUseCase.distributeEvenly(10000L, emptyList())
            assertTrue(result.isEmpty())
        }

        @Test
        fun `evenly divisible amount`() {
            val result = GetMemberBalancesFlowUseCase.distributeEvenly(
                10000L,
                listOf("user-1", "user-2", "user-3", "user-4")
            )
            assertEquals(2500L, result["user-1"])
            assertEquals(2500L, result["user-2"])
            assertEquals(2500L, result["user-3"])
            assertEquals(2500L, result["user-4"])
        }

        @Test
        fun `total distributed equals original amount with remainder`() {
            val result = GetMemberBalancesFlowUseCase.distributeEvenly(
                10001L,
                listOf("user-1", "user-2", "user-3", "user-4")
            )
            assertEquals(10001L, result.values.sum())
        }

        @Test
        fun `remainder allocation is deterministic regardless of input order`() {
            val resultAsc = GetMemberBalancesFlowUseCase.distributeEvenly(
                10001L,
                listOf("user-1", "user-2", "user-3")
            )
            val resultDesc = GetMemberBalancesFlowUseCase.distributeEvenly(
                10001L,
                listOf("user-3", "user-1", "user-2")
            )
            assertEquals(resultAsc, resultDesc)
            // "user-1" gets the extra cent (sorted first)
            assertEquals(3334L, resultAsc["user-1"])
            assertEquals(3334L, resultDesc["user-1"])
        }
    }

    @Nested
    @DisplayName("Per-currency breakdown")
    inner class PerCurrencyBreakdown {

        private val twoMembers = listOf("user-1", "user-2")

        @Test
        fun `single-currency EUR withdrawal produces cashInHandByCurrency with EUR entry`() {
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.GROUP,
                    amountWithdrawn = 10000L,
                    remainingAmount = 10000L,
                    currency = "EUR",
                    deductedBaseAmount = 10000L
                )
            )
            val result = compute(
                withdrawals = withdrawals,
                memberIds = twoMembers,
                groupCurrency = "EUR"
            )
            val u1 = result.first { it.userId == "user-1" }
            assertEquals(1, u1.cashInHandByCurrency.size)
            val eurEntry = u1.cashInHandByCurrency[0]
            assertEquals("EUR", eurEntry.currency)
            assertEquals(5000L, eurEntry.amountCents) // 10000 / 2 members
            assertEquals(5000L, eurEntry.equivalentCents) // same as native for group currency
        }

        @Test
        fun `foreign currency THB withdrawal tracks native and equivalent amounts`() {
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.GROUP,
                    amountWithdrawn = 1000000L, // 10000 THB
                    remainingAmount = 1000000L,
                    currency = "THB",
                    deductedBaseAmount = 2683L // ~26.83 EUR
                )
            )
            val result = compute(
                withdrawals = withdrawals,
                memberIds = twoMembers,
                groupCurrency = "EUR"
            )
            val u1 = result.first { it.userId == "user-1" }
            assertEquals(1, u1.cashInHandByCurrency.size)
            val thbEntry = u1.cashInHandByCurrency[0]
            assertEquals("THB", thbEntry.currency)
            assertEquals(500000L, thbEntry.amountCents) // 1000000 / 2 = 500000 THB cents
            // Equivalent: proportional = 500000 * 1342 / 1000000 ≈ 671 (rounding varies)
            // deducted 2683 split to 1342 + 1341 (remainder goes to first), native 500000 each
            assertTrue(thbEntry.equivalentCents > 0)
        }

        @Test
        fun `cashInHandByCurrency subtracts cash expenses per currency`() {
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    amountWithdrawn = 100000L, // 1000 THB
                    remainingAmount = 100000L,
                    currency = "THB",
                    deductedBaseAmount = 2700L // 27 EUR
                )
            )
            val expenses = listOf(
                Expense(
                    id = "exp-1",
                    sourceAmount = 4500L, // 45 THB
                    sourceCurrency = "THB",
                    groupAmount = 121L, // 1.21 EUR
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 4500L)
                    )
                )
            )
            val result = compute(
                withdrawals = withdrawals,
                expenses = expenses,
                memberIds = twoMembers,
                groupCurrency = "EUR"
            )
            val u1 = result.first { it.userId == "user-1" }
            assertEquals(1, u1.cashInHandByCurrency.size)
            val thbEntry = u1.cashInHandByCurrency[0]
            assertEquals("THB", thbEntry.currency)
            assertEquals(95500L, thbEntry.amountCents) // 100000 - 4500 = 95500 THB
            assertTrue(thbEntry.equivalentCents > 0) // proportional equivalent
        }

        @Test
        fun `cashInHandByCurrency excludes currency with zero remaining`() {
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    amountWithdrawn = 4500L,
                    remainingAmount = 0L,
                    currency = "THB",
                    deductedBaseAmount = 121L
                )
            )
            val expenses = listOf(
                Expense(
                    id = "exp-1",
                    sourceAmount = 4500L,
                    sourceCurrency = "THB",
                    groupAmount = 121L,
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 4500L)
                    )
                )
            )
            val result = compute(
                withdrawals = withdrawals,
                expenses = expenses,
                memberIds = twoMembers,
                groupCurrency = "EUR"
            )
            val u1 = result.first { it.userId == "user-1" }
            assertTrue(u1.cashInHandByCurrency.isEmpty())
        }

        @Test
        fun `cashSpentByCurrency tracks cash expenses per source currency`() {
            val expenses = listOf(
                Expense(
                    id = "exp-1",
                    sourceAmount = 4500L,
                    sourceCurrency = "THB",
                    groupAmount = 121L,
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 2250L),
                        ExpenseSplit(userId = "user-2", amountCents = 2250L)
                    )
                )
            )
            val result = compute(
                expenses = expenses,
                memberIds = twoMembers,
                groupCurrency = "EUR"
            )
            val u1 = result.first { it.userId == "user-1" }
            assertEquals(1, u1.cashSpentByCurrency.size)
            assertEquals("THB", u1.cashSpentByCurrency[0].currency)
            assertEquals(2250L, u1.cashSpentByCurrency[0].amountCents)
            // Equivalent: 2250 * 121 / 4500 = 61 (rounded)
            assertEquals(61L, u1.cashSpentByCurrency[0].equivalentCents)
        }

        @Test
        fun `nonCashSpentByCurrency tracks non-cash expenses per source currency`() {
            val expenses = listOf(
                Expense(
                    id = "exp-1",
                    sourceAmount = 2000L,
                    sourceCurrency = "EUR",
                    groupAmount = 2000L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 1000L),
                        ExpenseSplit(userId = "user-2", amountCents = 1000L)
                    )
                )
            )
            val result = compute(
                expenses = expenses,
                memberIds = twoMembers,
                groupCurrency = "EUR"
            )
            val u1 = result.first { it.userId == "user-1" }
            assertEquals(1, u1.nonCashSpentByCurrency.size)
            assertEquals("EUR", u1.nonCashSpentByCurrency[0].currency)
            assertEquals(1000L, u1.nonCashSpentByCurrency[0].amountCents)
            // Same currency → equivalent equals native
            assertEquals(1000L, u1.nonCashSpentByCurrency[0].equivalentCents)
        }

        @Test
        fun `multi-currency expenses produce separate entries per currency`() {
            val expenses = listOf(
                Expense(
                    id = "exp-eur",
                    sourceAmount = 2000L,
                    sourceCurrency = "EUR",
                    groupAmount = 2000L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 1000L)
                    )
                ),
                Expense(
                    id = "exp-thb",
                    sourceAmount = 100000L,
                    sourceCurrency = "THB",
                    groupAmount = 2700L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 50000L)
                    )
                )
            )
            val result = compute(
                expenses = expenses,
                memberIds = twoMembers,
                groupCurrency = "EUR"
            )
            val u1 = result.first { it.userId == "user-1" }
            assertEquals(2, u1.nonCashSpentByCurrency.size)
            val currencies = u1.nonCashSpentByCurrency.map { it.currency }
            assertTrue("EUR" in currencies)
            assertTrue("THB" in currencies)
            val eurEntry = u1.nonCashSpentByCurrency.first { it.currency == "EUR" }
            assertEquals(1000L, eurEntry.amountCents)
            assertEquals(1000L, eurEntry.equivalentCents)
            val thbEntry = u1.nonCashSpentByCurrency.first { it.currency == "THB" }
            assertEquals(50000L, thbEntry.amountCents)
            // Equivalent: 50000 * 2700 / 100000 = 1350
            assertEquals(1350L, thbEntry.equivalentCents)
        }

        @Test
        fun `no expenses and no withdrawals produce empty per-currency lists`() {
            val result = compute(
                memberIds = twoMembers,
                groupCurrency = "EUR"
            )
            val u1 = result.first { it.userId == "user-1" }
            assertTrue(u1.cashInHandByCurrency.isEmpty())
            assertTrue(u1.cashSpentByCurrency.isEmpty())
            assertTrue(u1.nonCashSpentByCurrency.isEmpty())
        }

        @Test
        fun `full scenario from issue matches expected per-member per-currency breakdown`() {
            // EUR group, THB additional currency, two members (50/50 couple)
            val subunit = Subunit(
                id = "sub-couple",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )
            val contributions = listOf(
                Contribution(
                    userId = "user-1",
                    subunitId = "sub-couple",
                    amount = 10000L // 100 EUR for couple → 50 each
                )
            )
            val withdrawals = listOf(
                // 1000 THB (26.83 EUR) for couple → ~13.41/13.42 each
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.SUBUNIT,
                    subunitId = "sub-couple",
                    amountWithdrawn = 100000L, // 1000 THB in cents
                    remainingAmount = 100000L,
                    currency = "THB",
                    deductedBaseAmount = 2683L
                ),
                // 10 EUR personal withdrawal by user-1
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    amountWithdrawn = 1000L, // 10 EUR
                    remainingAmount = 1000L,
                    currency = "EUR",
                    deductedBaseAmount = 1000L
                )
            )
            val expenses = listOf(
                // 45 THB (1.21 EUR) CASH expense for user-2
                Expense(
                    id = "exp-coffee",
                    sourceAmount = 4500L,
                    sourceCurrency = "THB",
                    groupAmount = 121L,
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-2", amountCents = 4500L)
                    )
                )
            )

            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                expenses = expenses,
                subunits = listOf(subunit),
                memberIds = listOf("user-1", "user-2"),
                groupCurrency = "EUR"
            )
            val balanceMap = result.associateBy { it.userId }

            // User-1: withdrew 10 EUR personal + 13.41 EUR (couple share of THB)
            val u1 = balanceMap["user-1"]!!
            // cashInHandByCurrency: EUR (1000 cents) + THB (50000 cents)
            assertEquals(2, u1.cashInHandByCurrency.size)
            val u1Eur = u1.cashInHandByCurrency.first { it.currency == "EUR" }
            assertEquals(1000L, u1Eur.amountCents)
            assertEquals(1000L, u1Eur.equivalentCents) // same currency
            val u1Thb = u1.cashInHandByCurrency.first { it.currency == "THB" }
            assertEquals(50000L, u1Thb.amountCents)
            assertTrue(u1Thb.equivalentCents > 0) // proportional from deductedBaseAmount
            assertTrue(u1.cashSpentByCurrency.isEmpty()) // no cash expenses for user-1

            // User-2: withdrew 13.42 EUR (couple share of THB), spent 45 THB cash
            val u2 = balanceMap["user-2"]!!
            // cashInHandByCurrency: THB with 50000 - 4500 = 45500
            assertEquals(1, u2.cashInHandByCurrency.size)
            assertEquals("THB", u2.cashInHandByCurrency[0].currency)
            assertEquals(45500L, u2.cashInHandByCurrency[0].amountCents)
            // cashSpentByCurrency: THB 4500
            assertEquals(1, u2.cashSpentByCurrency.size)
            assertEquals("THB", u2.cashSpentByCurrency[0].currency)
            assertEquals(4500L, u2.cashSpentByCurrency[0].amountCents)
        }

        @Test
        fun `per-currency lists are sorted alphabetically by currency code`() {
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    amountWithdrawn = 5000L,
                    remainingAmount = 5000L,
                    currency = "USD",
                    deductedBaseAmount = 4500L
                ),
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    amountWithdrawn = 1000L,
                    remainingAmount = 1000L,
                    currency = "EUR",
                    deductedBaseAmount = 1000L
                ),
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    amountWithdrawn = 100000L,
                    remainingAmount = 100000L,
                    currency = "THB",
                    deductedBaseAmount = 2700L
                )
            )
            val result = compute(
                withdrawals = withdrawals,
                memberIds = listOf("user-1"),
                groupCurrency = "EUR"
            )
            val u1 = result.first { it.userId == "user-1" }
            assertEquals(3, u1.cashInHandByCurrency.size)
            assertEquals("EUR", u1.cashInHandByCurrency[0].currency)
            assertEquals("THB", u1.cashInHandByCurrency[1].currency)
            assertEquals("USD", u1.cashInHandByCurrency[2].currency)
        }
    }

    @Nested
    @DisplayName("Add-on integration")
    inner class AddOnIntegration {
        private val twoMembers = listOf("user-1", "user-2")

        @Test
        fun `expenses without add-ons use base group amount`() {
            val expenses = listOf(
                Expense(
                    id = "exp-1",
                    sourceAmount = 10000L,
                    groupAmount = 10000L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 5000L),
                        ExpenseSplit(userId = "user-2", amountCents = 5000L)
                    )
                )
            )
            val result = compute(expenses = expenses, memberIds = twoMembers)
            val u1 = result.first { it.userId == "user-1" }
            val u2 = result.first { it.userId == "user-2" }
            assertEquals(5000L, u1.nonCashSpent)
            assertEquals(5000L, u2.nonCashSpent)
        }

        @Test
        fun `ON_TOP fee increases effective group amount for balance calculation`() {
            // Base: 100 EUR, Fee: 2.50 EUR ON_TOP → Effective: 102.50 EUR
            val expenses = listOf(
                Expense(
                    id = "exp-with-fee",
                    sourceAmount = 10000L,
                    groupAmount = 10000L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    addOns = listOf(
                        AddOn(
                            id = "fee-1",
                            type = AddOnType.FEE,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 250L,
                            groupAmountCents = 250L
                        )
                    ),
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 5000L),
                        ExpenseSplit(userId = "user-2", amountCents = 5000L)
                    )
                )
            )
            val result = compute(expenses = expenses, memberIds = twoMembers)
            val u1 = result.first { it.userId == "user-1" }
            val u2 = result.first { it.userId == "user-2" }
            // Effective: 10250L. Each split is 5000/10000 * 10250 = 5125
            assertEquals(5125L, u1.nonCashSpent)
            assertEquals(5125L, u2.nonCashSpent)
        }

        @Test
        fun `ON_TOP tip increases effective group amount`() {
            // Base: 60 EUR, Tip: 10 EUR ON_TOP → Effective: 70 EUR
            val expenses = listOf(
                Expense(
                    id = "exp-with-tip",
                    sourceAmount = 6000L,
                    groupAmount = 6000L,
                    paymentMethod = PaymentMethod.CASH,
                    addOns = listOf(
                        AddOn(
                            id = "tip-1",
                            type = AddOnType.TIP,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 1000L,
                            groupAmountCents = 1000L
                        )
                    ),
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 3000L),
                        ExpenseSplit(userId = "user-2", amountCents = 3000L)
                    )
                )
            )
            val result = compute(expenses = expenses, memberIds = twoMembers)
            // Effective: 7000L. Each split is 3000/6000 * 7000 = 3500
            val u1 = result.first { it.userId == "user-1" }
            assertEquals(3500L, u1.cashSpent)
        }

        @Test
        fun `INCLUDED tip does NOT alter effective group amount`() {
            // Base: 72 EUR with INCLUDED tip → Effective still 72 EUR
            val expenses = listOf(
                Expense(
                    id = "exp-included-tip",
                    sourceAmount = 7200L,
                    groupAmount = 7200L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    addOns = listOf(
                        AddOn(
                            id = "tip-1",
                            type = AddOnType.TIP,
                            mode = AddOnMode.INCLUDED,
                            amountCents = 648L, // 9% tip included in the 72 EUR
                            groupAmountCents = 648L
                        )
                    ),
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 3600L),
                        ExpenseSplit(userId = "user-2", amountCents = 3600L)
                    )
                )
            )
            val result = compute(expenses = expenses, memberIds = twoMembers)
            val u1 = result.first { it.userId == "user-1" }
            val u2 = result.first { it.userId == "user-2" }
            // No increase — effective = base = 7200
            assertEquals(3600L, u1.nonCashSpent)
            assertEquals(3600L, u2.nonCashSpent)
        }

        @Test
        fun `DISCOUNT reduces effective group amount`() {
            // Base: 100 EUR, Discount: 5 EUR → Effective: 95 EUR
            val expenses = listOf(
                Expense(
                    id = "exp-with-discount",
                    sourceAmount = 10000L,
                    groupAmount = 10000L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    addOns = listOf(
                        AddOn(
                            id = "discount-1",
                            type = AddOnType.DISCOUNT,
                            mode = AddOnMode.ON_TOP, // mode doesn't matter for discounts
                            amountCents = 500L,
                            groupAmountCents = 500L
                        )
                    ),
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 5000L),
                        ExpenseSplit(userId = "user-2", amountCents = 5000L)
                    )
                )
            )
            val result = compute(expenses = expenses, memberIds = twoMembers)
            val u1 = result.first { it.userId == "user-1" }
            val u2 = result.first { it.userId == "user-2" }
            // Effective: 9500L. Each split is 5000/10000 * 9500 = 4750
            assertEquals(4750L, u1.nonCashSpent)
            assertEquals(4750L, u2.nonCashSpent)
        }

        @Test
        fun `mixed add-ons combine correctly for balance calculation`() {
            // Base: 100 EUR, Fee: 10 EUR ON_TOP, Tip: 5 EUR ON_TOP, Discount: 2 EUR
            // Effective: 100 + 10 + 5 - 2 = 113 EUR
            val expenses = listOf(
                Expense(
                    id = "exp-mixed",
                    sourceAmount = 10000L,
                    groupAmount = 10000L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    addOns = listOf(
                        AddOn(
                            id = "fee-1",
                            type = AddOnType.FEE,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 1000L,
                            groupAmountCents = 1000L
                        ),
                        AddOn(
                            id = "tip-1",
                            type = AddOnType.TIP,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 500L,
                            groupAmountCents = 500L
                        ),
                        AddOn(
                            id = "discount-1",
                            type = AddOnType.DISCOUNT,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 200L,
                            groupAmountCents = 200L
                        )
                    ),
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 4000L),
                        ExpenseSplit(userId = "user-2", amountCents = 6000L)
                    )
                )
            )
            val result = compute(expenses = expenses, memberIds = twoMembers)
            val u1 = result.first { it.userId == "user-1" }
            val u2 = result.first { it.userId == "user-2" }
            // Effective: 11300L
            // user-1: 4000/10000 * 11300 = 4520
            // user-2: 6000/10000 * 11300 = 6780
            assertEquals(4520L, u1.nonCashSpent)
            assertEquals(6780L, u2.nonCashSpent)
        }

        @Test
        fun `foreign currency expense with ON_TOP fee scales correctly to group currency`() {
            // 200 THB boat ride with 9.25 THB ON_TOP booking fee
            // Exchange rate: 1 EUR = 37.22 THB → groupAmount = 5.37 EUR (537 cents)
            // Fee groupAmount: 25 cents (9.25/37.22)
            // Effective: 537 + 25 = 562 EUR cents
            val expenses = listOf(
                Expense(
                    id = "exp-boat-thb",
                    sourceAmount = 20000L, // 200 THB
                    sourceCurrency = "THB",
                    groupAmount = 537L, // 5.37 EUR
                    groupCurrency = "EUR",
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    addOns = listOf(
                        AddOn(
                            id = "fee-1",
                            type = AddOnType.FEE,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 925L, // 9.25 THB
                            currency = "THB",
                            groupAmountCents = 25L // 0.25 EUR
                        )
                    ),
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 10000L),
                        ExpenseSplit(userId = "user-2", amountCents = 10000L)
                    )
                )
            )
            val result = compute(expenses = expenses, memberIds = twoMembers, groupCurrency = "EUR")
            val u1 = result.first { it.userId == "user-1" }
            val u2 = result.first { it.userId == "user-2" }
            // Effective: 562L. Each split is 10000/20000 * 562 = 281
            assertEquals(281L, u1.nonCashSpent)
            assertEquals(281L, u2.nonCashSpent)
            // Total: 562
            assertEquals(562L, u1.nonCashSpent + u2.nonCashSpent)
        }

        @Test
        fun `pocketBalance reflects add-on adjusted expenses`() {
            val contributions = listOf(
                Contribution(userId = "user-1", contributionScope = PayerType.USER, amount = 10000L)
            )
            val expenses = listOf(
                Expense(
                    id = "exp-1",
                    sourceAmount = 6000L,
                    groupAmount = 6000L,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    addOns = listOf(
                        AddOn(
                            id = "fee-1",
                            type = AddOnType.FEE,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 200L,
                            groupAmountCents = 200L
                        )
                    ),
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 6000L)
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                expenses = expenses,
                memberIds = listOf("user-1")
            )
            val u1 = result.first { it.userId == "user-1" }
            // Effective expense: 6200L
            // pocketBalance = contributed - withdrawn - nonCashSpent = 10000 - 0 - 6200 = 3800
            assertEquals(6200L, u1.nonCashSpent)
            assertEquals(3800L, u1.pocketBalance)
        }

        @Test
        fun `cash expense with add-on affects cashSpent correctly`() {
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    amountWithdrawn = 10000L,
                    remainingAmount = 10000L,
                    currency = "EUR",
                    deductedBaseAmount = 10000L
                )
            )
            val expenses = listOf(
                Expense(
                    id = "exp-cash",
                    sourceAmount = 5000L,
                    groupAmount = 5000L,
                    paymentMethod = PaymentMethod.CASH,
                    addOns = listOf(
                        AddOn(
                            id = "tip-1",
                            type = AddOnType.TIP,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 500L,
                            groupAmountCents = 500L
                        )
                    ),
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 5000L)
                    )
                )
            )
            val result = compute(
                withdrawals = withdrawals,
                expenses = expenses,
                memberIds = listOf("user-1")
            )
            val u1 = result.first { it.userId == "user-1" }
            // Effective: 5500L cash spent
            assertEquals(5500L, u1.cashSpent)
            // cashInHand = withdrawn - cashSpent = 10000 - 5500 = 4500
            assertEquals(4500L, u1.cashInHand)
        }

        @Test
        fun `ATM fee add-on on withdrawal increases effective withdrawn amount`() {
            // Withdrawal of 10000 THB with 5 EUR ATM fee (706 cents in group currency)
            val contributions = listOf(
                Contribution(userId = "user-1", contributionScope = PayerType.USER, amount = 50000L)
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    amountWithdrawn = 1000000L, // 10,000 THB in cents
                    remainingAmount = 1000000L,
                    currency = "THB",
                    deductedBaseAmount = 27000L, // 270 EUR deducted from pocket
                    addOns = listOf(
                        AddOn(
                            id = "atm-fee-1",
                            type = AddOnType.FEE,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 500L, // 5 EUR fee charged
                            currency = "EUR",
                            groupAmountCents = 500L // Already in group currency
                        )
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                memberIds = listOf("user-1")
            )
            val u1 = result.first { it.userId == "user-1" }
            // Effective withdrawn = deductedBaseAmount + ATM fee groupAmountCents = 27000 + 500 = 27500
            assertEquals(27500L, u1.withdrawn)
            // pocketBalance = contributed - withdrawn - nonCashSpent = 50000 - 27500 - 0 = 22500
            assertEquals(22500L, u1.pocketBalance)
        }

        @Test
        fun `GROUP-scoped withdrawal with ATM fee distributes effective amount equally`() {
            val contributions = groupMemberIds.map {
                Contribution(userId = it, contributionScope = PayerType.USER, amount = 10000L)
            }
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.GROUP,
                    amountWithdrawn = 500000L, // 5000 THB
                    remainingAmount = 500000L,
                    currency = "THB",
                    deductedBaseAmount = 13587L, // ~135.87 EUR
                    addOns = listOf(
                        AddOn(
                            id = "atm-fee-2",
                            type = AddOnType.FEE,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 706L, // ~7.06 EUR fee in group currency
                            currency = "EUR",
                            groupAmountCents = 706L
                        )
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                memberIds = groupMemberIds
            )
            // Effective deducted = 13587 + 706 = 14293
            // Per member = 14293 / 4 = 3573 (with remainder distribution)
            val totalEffectiveWithdrawn = result.sumOf { it.withdrawn }
            assertEquals(14293L, totalEffectiveWithdrawn)
            // Each member's withdrawn should be ~3573 (3573 * 3 + 3574 = 14293)
            val balanceMap = result.associateBy { it.userId }
            assertTrue(balanceMap.values.all { it.withdrawn in 3573L..3574L })
        }

        @Test
        fun `SUBUNIT-scoped withdrawal with ATM fee distributes by member shares`() {
            // Couple subunit: user-1 and user-2 with 50-50 shares
            val coupleSubunit = Subunit(
                id = "couple-1",
                name = "Couple",
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )
            val contributions = listOf(
                Contribution(userId = "user-1", contributionScope = PayerType.USER, amount = 20000L),
                Contribution(userId = "user-2", contributionScope = PayerType.USER, amount = 20000L)
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.SUBUNIT,
                    subunitId = "couple-1",
                    amountWithdrawn = 1000000L, // 10,000 THB
                    remainingAmount = 1000000L,
                    currency = "THB",
                    deductedBaseAmount = 27000L, // 270 EUR
                    addOns = listOf(
                        AddOn(
                            id = "atm-fee-3",
                            type = AddOnType.FEE,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 500L, // 5 EUR fee
                            currency = "EUR",
                            groupAmountCents = 500L
                        )
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                subunits = listOf(coupleSubunit),
                memberIds = listOf("user-1", "user-2", "user-3", "user-4")
            )
            val balanceMap = result.associateBy { it.userId }

            // Effective deducted = 27000 + 500 = 27500
            // Split 50-50 between user-1 and user-2: 13750 each
            assertEquals(13750L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(13750L, balanceMap["user-2"]!!.withdrawn)
            // user-3 and user-4 are not in the subunit, so they get nothing
            assertEquals(0L, balanceMap["user-3"]!!.withdrawn)
            assertEquals(0L, balanceMap["user-4"]!!.withdrawn)

            // pocketBalance for user-1 = 20000 - 13750 = 6250
            assertEquals(6250L, balanceMap["user-1"]!!.pocketBalance)
            assertEquals(6250L, balanceMap["user-2"]!!.pocketBalance)
        }

        @Test
        fun `issue 679 - ATM fee does NOT inflate cashInHand scalar`() {
            // Exact reproduction from issue #679:
            // Contribution = 500 EUR, Withdrawal = 162 EUR with 1.25 EUR ATM fee, no expenses.
            // cashInHand should be 162 EUR (physical cash), NOT 163.25 EUR (effective deducted).
            val contributions = listOf(
                Contribution(userId = "user-1", contributionScope = PayerType.USER, amount = 50000L)
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    amountWithdrawn = 16200L, // 162 EUR in cents
                    remainingAmount = 16200L,
                    currency = "EUR",
                    deductedBaseAmount = 16200L, // 162 EUR deducted from pocket (base)
                    addOns = listOf(
                        AddOn(
                            id = "atm-fee-679",
                            type = AddOnType.FEE,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 125L, // 1.25 EUR ATM fee
                            currency = "EUR",
                            groupAmountCents = 125L
                        )
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                memberIds = listOf("user-1"),
                groupCurrency = "EUR"
            )
            val u1 = result.first { it.userId == "user-1" }
            // withdrawn (effective) = 16200 + 125 = 16325 (includes ATM fee for pocket math)
            assertEquals(16325L, u1.withdrawn)
            // cashInHand = rawWithdrawn - cashSpent = 16200 - 0 = 16200 (physical cash only)
            assertEquals(16200L, u1.cashInHand)
            // pocketBalance = 50000 - 16325 - 0 = 33675
            assertEquals(33675L, u1.pocketBalance)
            // cashInHandByCurrency should agree with the scalar
            assertEquals(1, u1.cashInHandByCurrency.size)
            assertEquals(16200L, u1.cashInHandByCurrency[0].amountCents)
        }

        @Test
        fun `ATM fee excluded from cashInHand with cash expense partially spent`() {
            // Withdrawal of 10000 THB (270 EUR base) with 5 EUR ATM fee.
            // Cash expense spent 5000 THB (half).
            // cashInHand should be 270 EUR - 135 EUR = 135 EUR (not 275 - 135 = 140).
            val contributions = listOf(
                Contribution(userId = "user-1", contributionScope = PayerType.USER, amount = 50000L)
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.USER,
                    amountWithdrawn = 1000000L, // 10,000 THB
                    remainingAmount = 500000L, // 5,000 THB remaining
                    currency = "THB",
                    deductedBaseAmount = 27000L, // 270 EUR
                    addOns = listOf(
                        AddOn(
                            id = "atm-fee-partial",
                            type = AddOnType.FEE,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 500L, // 5 EUR fee
                            currency = "EUR",
                            groupAmountCents = 500L
                        )
                    )
                )
            )
            val expenses = listOf(
                Expense(
                    id = "exp-cash-thb",
                    sourceAmount = 500000L, // 5000 THB
                    sourceCurrency = "THB",
                    groupAmount = 13500L, // 135 EUR equivalent
                    paymentMethod = PaymentMethod.CASH,
                    splits = listOf(
                        ExpenseSplit(userId = "user-1", amountCents = 500000L)
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                expenses = expenses,
                memberIds = listOf("user-1"),
                groupCurrency = "EUR"
            )
            val u1 = result.first { it.userId == "user-1" }
            // withdrawn (effective) = 27000 + 500 = 27500
            assertEquals(27500L, u1.withdrawn)
            // cashSpent = 13500 EUR equivalent of THB cash expense
            assertEquals(13500L, u1.cashSpent)
            // cashInHand = rawWithdrawn - cashSpent = 27000 - 13500 = 13500 (no ATM fee)
            assertEquals(13500L, u1.cashInHand)
            // pocketBalance = 50000 - 27500 - 0 = 22500
            assertEquals(22500L, u1.pocketBalance)

            // Verify remaining THB cash equivalent excludes ATM fee at per-currency level.
            val nonGroupCurrencyCash = u1.cashInHandByCurrency.filter { it.currency != "EUR" }
            assertEquals(1, nonGroupCurrencyCash.size)
            val thbCash = nonGroupCurrencyCash.single()
            // Remaining THB equivalent should be 13500 EUR (raw withdrawn 27000 - spent 13500),
            // not 13750 (which would incorrectly include part of the ATM fee).
            assertEquals(13500L, thbCash.equivalentCents)
        }

        @Test
        fun `GROUP-scoped withdrawal ATM fee excluded from per-member cashInHand`() {
            val contributions = groupMemberIds.map {
                Contribution(userId = it, contributionScope = PayerType.USER, amount = 10000L)
            }
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.GROUP,
                    amountWithdrawn = 500000L, // 5000 THB
                    remainingAmount = 500000L,
                    currency = "THB",
                    deductedBaseAmount = 13587L, // ~135.87 EUR
                    addOns = listOf(
                        AddOn(
                            id = "atm-fee-group",
                            type = AddOnType.FEE,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 706L, // ~7.06 EUR fee
                            currency = "EUR",
                            groupAmountCents = 706L
                        )
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                memberIds = groupMemberIds,
                groupCurrency = "EUR"
            )
            // Effective deducted = 13587 + 706 = 14293, per member ~3573
            // Raw deducted = 13587, per member = 13587/4 = 3396 (with remainder)
            val totalEffectiveWithdrawn = result.sumOf { it.withdrawn }
            assertEquals(14293L, totalEffectiveWithdrawn)
            // Total raw cashInHand should equal raw deducted (no expenses)
            val totalCashInHand = result.sumOf { it.cashInHand }
            assertEquals(13587L, totalCashInHand)
            // Verify per-member: cashInHand < withdrawn (ATM fee excluded from cashInHand)
            result.forEach { balance ->
                assertTrue(
                    balance.cashInHand <= balance.withdrawn,
                    "cashInHand (${balance.cashInHand}) should be <= withdrawn (${balance.withdrawn})"
                )
            }
        }

        @Test
        fun `SUBUNIT-scoped withdrawal ATM fee excluded from per-member cashInHand`() {
            val coupleSubunit = Subunit(
                id = "couple-1",
                name = "Couple",
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )
            val contributions = listOf(
                Contribution(userId = "user-1", contributionScope = PayerType.USER, amount = 20000L),
                Contribution(userId = "user-2", contributionScope = PayerType.USER, amount = 20000L)
            )
            val withdrawals = listOf(
                CashWithdrawal(
                    withdrawnBy = "user-1",
                    withdrawalScope = PayerType.SUBUNIT,
                    subunitId = "couple-1",
                    amountWithdrawn = 1000000L, // 10,000 THB
                    remainingAmount = 1000000L,
                    currency = "THB",
                    deductedBaseAmount = 27000L, // 270 EUR
                    addOns = listOf(
                        AddOn(
                            id = "atm-fee-sub",
                            type = AddOnType.FEE,
                            mode = AddOnMode.ON_TOP,
                            amountCents = 500L, // 5 EUR fee
                            currency = "EUR",
                            groupAmountCents = 500L
                        )
                    )
                )
            )
            val result = compute(
                contributions = contributions,
                withdrawals = withdrawals,
                subunits = listOf(coupleSubunit),
                memberIds = listOf("user-1", "user-2", "user-3", "user-4"),
                groupCurrency = "EUR"
            )
            val balanceMap = result.associateBy { it.userId }

            // Effective deducted = 27000 + 500 = 27500, split 50-50: 13750 each
            assertEquals(13750L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(13750L, balanceMap["user-2"]!!.withdrawn)

            // Raw deducted = 27000, split 50-50: 13500 each
            // cashInHand = rawWithdrawn - cashSpent = 13500 - 0 = 13500
            assertEquals(13500L, balanceMap["user-1"]!!.cashInHand)
            assertEquals(13500L, balanceMap["user-2"]!!.cashInHand)

            // Non-subunit members: zero
            assertEquals(0L, balanceMap["user-3"]!!.cashInHand)
            assertEquals(0L, balanceMap["user-4"]!!.cashInHand)
        }
    }
}

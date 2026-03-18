package es.pedrazamiguez.expenseshareapp.domain.usecase.balance
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
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
                Contribution(userId = "user-1", amount = 5000L),
                Contribution(userId = "user-2", amount = 3000L)
            )
            val result = compute(contributions = contributions)
            val balanceMap = result.associateBy { it.userId }
            assertEquals(5000L, balanceMap["user-1"]!!.contributed)
            assertEquals(3000L, balanceMap["user-2"]!!.contributed)
            assertEquals(0L, balanceMap["user-3"]!!.contributed)
            assertEquals(0L, balanceMap["user-4"]!!.contributed)
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
                Contribution(userId = "user-1", subunitId = "sub-1", amount = 10000L)
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
                Contribution(userId = "user-1", subunitId = "sub-fam", amount = 10000L)
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
                Contribution(userId = "user-3", amount = 5000L),
                Contribution(userId = "user-1", subunitId = "sub-1", amount = 10000L)
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
                Contribution(userId = "user-1", subunitId = "nonexistent-sub", amount = 8000L)
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
                Contribution(userId = "user-1", subunitId = "sub-3", amount = 100L)
            )
            val result = compute(contributions = contributions, subunits = listOf(subunit))
            val balanceMap = result.associateBy { it.userId }
            val totalContributed = balanceMap.values.sumOf { it.contributed }
            assertEquals(100L, totalContributed)
            assertTrue(balanceMap["user-1"]!!.contributed >= 33L)
            assertTrue(balanceMap["user-2"]!!.contributed >= 33L)
            assertTrue(balanceMap["user-3"]!!.contributed >= 33L)
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
                Contribution(userId = "user-1", subunitId = "sub-1", amount = 10000L),
                Contribution(userId = "user-3", amount = 5000L)
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
                Contribution(userId = "user-1", subunitId = "sub-couple", amount = 10000L),
                Contribution(userId = "user-3", amount = 5000L),
                Contribution(userId = "user-4", amount = 5000L)
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
                Contribution(userId = "user-2", amount = 5000L)  // 50€
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
}

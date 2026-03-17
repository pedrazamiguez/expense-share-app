package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.SubunitRepository
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GetMemberBalancesFlowUseCase")
class GetMemberBalancesFlowUseCaseTest {

    private lateinit var contributionRepository: ContributionRepository
    private lateinit var cashWithdrawalRepository: CashWithdrawalRepository
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var subunitRepository: SubunitRepository
    private lateinit var useCase: GetMemberBalancesFlowUseCase

    private val groupId = "group-123"
    private val groupMemberIds = listOf("user-1", "user-2", "user-3", "user-4")

    @BeforeEach
    fun setUp() {
        contributionRepository = mockk()
        cashWithdrawalRepository = mockk()
        expenseRepository = mockk()
        subunitRepository = mockk()
        useCase = GetMemberBalancesFlowUseCase(
            contributionRepository = contributionRepository,
            cashWithdrawalRepository = cashWithdrawalRepository,
            expenseRepository = expenseRepository,
            subunitRepository = subunitRepository
        )
    }

    private fun mockEmptyFlows() {
        every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
        every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
        every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
        every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())
    }

    @Nested
    @DisplayName("Zero data / edge cases")
    inner class ZeroData {

        @Test
        fun `all members have zero balances when no data exists`() = runTest {
            mockEmptyFlows()

            val result = useCase(groupId, groupMemberIds).first()

            assertEquals(4, result.size)
            result.forEach { balance ->
                assertEquals(0L, balance.contributed)
                assertEquals(0L, balance.withdrawn)
                assertEquals(0L, balance.spent)
                assertEquals(0L, balance.available)
                assertEquals(0L, balance.netBalance)
            }
        }

        @Test
        fun `returns empty list when group has no members and no data`() = runTest {
            mockEmptyFlows()

            val result = useCase(groupId, emptyList()).first()

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("Contribution attribution")
    inner class ContributionAttribution {

        @Test
        fun `individual contributions attributed entirely to the contributor`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(
                    Contribution(userId = "user-1", amount = 5000L),
                    Contribution(userId = "user-2", amount = 3000L)
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            assertEquals(5000L, balanceMap["user-1"]!!.contributed)
            assertEquals(3000L, balanceMap["user-2"]!!.contributed)
            assertEquals(0L, balanceMap["user-3"]!!.contributed)
            assertEquals(0L, balanceMap["user-4"]!!.contributed)
        }

        @Test
        fun `subunit contribution distributed by memberShares (50-50 couple)`() = runTest {
            val subunit = Subunit(
                id = "sub-1",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(
                    Contribution(userId = "user-1", subunitId = "sub-1", amount = 10000L)
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(listOf(subunit))

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            assertEquals(5000L, balanceMap["user-1"]!!.contributed)
            assertEquals(5000L, balanceMap["user-2"]!!.contributed)
        }

        @Test
        fun `subunit contribution with unequal shares (40-30-30 family)`() = runTest {
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
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(
                    Contribution(userId = "user-1", subunitId = "sub-fam", amount = 10000L)
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(listOf(subunit))

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            assertEquals(4000L, balanceMap["user-1"]!!.contributed)
            assertEquals(3000L, balanceMap["user-2"]!!.contributed)
            assertEquals(3000L, balanceMap["user-3"]!!.contributed)
            // Total should equal the original amount
            assertEquals(10000L, balanceMap.values.sumOf { it.contributed })
        }

        @Test
        fun `mixed individual and subunit contributions`() = runTest {
            val subunit = Subunit(
                id = "sub-1",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(
                    // user-3 contributes individually
                    Contribution(userId = "user-3", amount = 5000L),
                    // user-1 contributes on behalf of the couple
                    Contribution(userId = "user-1", subunitId = "sub-1", amount = 10000L)
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(listOf(subunit))

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            assertEquals(5000L, balanceMap["user-1"]!!.contributed) // 50% of 10000
            assertEquals(5000L, balanceMap["user-2"]!!.contributed) // 50% of 10000
            assertEquals(5000L, balanceMap["user-3"]!!.contributed) // individual
        }

        @Test
        fun `subunit contribution falls back to contributor when subunit not found`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(
                    Contribution(userId = "user-1", subunitId = "nonexistent-sub", amount = 8000L)
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            assertEquals(8000L, balanceMap["user-1"]!!.contributed)
        }

        @Test
        fun `rounding remainder allocated correctly for indivisible amounts`() = runTest {
            // 3 members, amount = 100 → 33 + 33 + 34 = 100 (remainder goes to first)
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
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(
                    Contribution(userId = "user-1", subunitId = "sub-3", amount = 100L)
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(listOf(subunit))

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            // Total must equal the original amount
            val totalContributed = balanceMap.values.sumOf { it.contributed }
            assertEquals(100L, totalContributed)
            // Each member gets at least floor(100 * 1/3) = 33
            assertTrue(balanceMap["user-1"]!!.contributed >= 33L)
            assertTrue(balanceMap["user-2"]!!.contributed >= 33L)
            assertTrue(balanceMap["user-3"]!!.contributed >= 33L)
        }
    }

    @Nested
    @DisplayName("Withdrawal attribution")
    inner class WithdrawalAttribution {

        @Test
        fun `GROUP-scoped withdrawal split equally among all members`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        withdrawnBy = "user-1",
                        withdrawalScope = PayerType.GROUP,
                        deductedBaseAmount = 10000L
                    )
                )
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            // 10000 / 4 = 2500 each
            assertEquals(2500L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(2500L, balanceMap["user-2"]!!.withdrawn)
            assertEquals(2500L, balanceMap["user-3"]!!.withdrawn)
            assertEquals(2500L, balanceMap["user-4"]!!.withdrawn)
        }

        @Test
        fun `GROUP-scoped withdrawal with remainder distributed correctly`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        withdrawnBy = "user-1",
                        withdrawalScope = PayerType.GROUP,
                        deductedBaseAmount = 10001L  // Not evenly divisible by 4
                    )
                )
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()

            // Total withdrawn must equal the original amount
            val totalWithdrawn = result.sumOf { it.withdrawn }
            assertEquals(10001L, totalWithdrawn)
        }

        @Test
        fun `SUBUNIT-scoped withdrawal distributed by memberShares`() = runTest {
            val subunit = Subunit(
                id = "sub-1",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.6"),
                    "user-2" to BigDecimal("0.4")
                )
            )
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        withdrawnBy = "user-1",
                        withdrawalScope = PayerType.SUBUNIT,
                        subunitId = "sub-1",
                        deductedBaseAmount = 10000L
                    )
                )
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(listOf(subunit))

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            assertEquals(6000L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(4000L, balanceMap["user-2"]!!.withdrawn)
            assertEquals(0L, balanceMap["user-3"]!!.withdrawn)
            assertEquals(0L, balanceMap["user-4"]!!.withdrawn)
        }

        @Test
        fun `USER-scoped withdrawal attributed entirely to withdrawer`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        withdrawnBy = "user-3",
                        withdrawalScope = PayerType.USER,
                        deductedBaseAmount = 500L
                    )
                )
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            assertEquals(500L, balanceMap["user-3"]!!.withdrawn)
            assertEquals(0L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(0L, balanceMap["user-2"]!!.withdrawn)
            assertEquals(0L, balanceMap["user-4"]!!.withdrawn)
        }

        @Test
        fun `mixed withdrawal scopes in one group`() = runTest {
            val subunit = Subunit(
                id = "sub-1",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    // GROUP scope: 20000 / 4 = 5000 each
                    CashWithdrawal(
                        withdrawnBy = "user-1",
                        withdrawalScope = PayerType.GROUP,
                        deductedBaseAmount = 20000L
                    ),
                    // SUBUNIT scope: 10000 * 0.5 = 5000 each for sub-1 members
                    CashWithdrawal(
                        withdrawnBy = "user-1",
                        withdrawalScope = PayerType.SUBUNIT,
                        subunitId = "sub-1",
                        deductedBaseAmount = 10000L
                    ),
                    // USER scope: 500 to user-3
                    CashWithdrawal(
                        withdrawnBy = "user-3",
                        withdrawalScope = PayerType.USER,
                        deductedBaseAmount = 500L
                    )
                )
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(listOf(subunit))

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            // user-1: GROUP(5000) + SUBUNIT(5000) = 10000
            assertEquals(10000L, balanceMap["user-1"]!!.withdrawn)
            // user-2: GROUP(5000) + SUBUNIT(5000) = 10000
            assertEquals(10000L, balanceMap["user-2"]!!.withdrawn)
            // user-3: GROUP(5000) + USER(500) = 5500
            assertEquals(5500L, balanceMap["user-3"]!!.withdrawn)
            // user-4: GROUP(5000) only
            assertEquals(5000L, balanceMap["user-4"]!!.withdrawn)
        }

        @Test
        fun `SUBUNIT-scoped withdrawal falls back to withdrawer when subunit not found`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        withdrawnBy = "user-1",
                        withdrawalScope = PayerType.SUBUNIT,
                        subunitId = "nonexistent",
                        deductedBaseAmount = 3000L
                    )
                )
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            assertEquals(3000L, balanceMap["user-1"]!!.withdrawn)
        }
    }

    @Nested
    @DisplayName("Expense split attribution")
    inner class ExpenseSplitAttribution {

        @Test
        fun `expense splits summed per user`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        id = "exp-1",
                        groupId = groupId,
                        sourceAmount = 10000L,
                        groupAmount = 10000L,
                        splitType = SplitType.EQUAL,
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 2500L),
                            ExpenseSplit(userId = "user-2", amountCents = 2500L),
                            ExpenseSplit(userId = "user-3", amountCents = 2500L),
                            ExpenseSplit(userId = "user-4", amountCents = 2500L)
                        )
                    )
                )
            )
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            assertEquals(2500L, balanceMap["user-1"]!!.spent)
            assertEquals(2500L, balanceMap["user-2"]!!.spent)
            assertEquals(2500L, balanceMap["user-3"]!!.spent)
            assertEquals(2500L, balanceMap["user-4"]!!.spent)
        }

        @Test
        fun `multiple expenses accumulated per user`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        id = "exp-1",
                        sourceAmount = 5000L,
                        groupAmount = 5000L,
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 3000L),
                            ExpenseSplit(userId = "user-2", amountCents = 2000L)
                        )
                    ),
                    Expense(
                        id = "exp-2",
                        sourceAmount = 5000L,
                        groupAmount = 5000L,
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 1500L),
                            ExpenseSplit(userId = "user-3", amountCents = 3500L)
                        )
                    )
                )
            )
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            assertEquals(4500L, balanceMap["user-1"]!!.spent) // 3000 + 1500
            assertEquals(2000L, balanceMap["user-2"]!!.spent)
            assertEquals(3500L, balanceMap["user-3"]!!.spent)
            assertEquals(0L, balanceMap["user-4"]!!.spent)
        }

        @Test
        fun `excluded splits are not counted`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        id = "exp-1",
                        sourceAmount = 10000L,
                        groupAmount = 10000L,
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 5000L),
                            ExpenseSplit(userId = "user-2", amountCents = 5000L, isExcluded = true)
                        )
                    )
                )
            )
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            assertEquals(5000L, balanceMap["user-1"]!!.spent)
            assertEquals(0L, balanceMap["user-2"]!!.spent)
        }

        @Test
        fun `foreign currency expense splits converted to group currency`() = runTest {
            // THB expense: 1000 THB (sourceAmount) ≈ 26.83 EUR (groupAmount = 2683 cents)
            // Splits in source currency (THB): user-1 = 500 THB, user-2 = 500 THB
            // Expected in group currency (EUR): each = 500 * 2683 / 1000 = 1342 (HALF_UP)
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        id = "exp-thb",
                        sourceAmount = 100000L,   // 1000.00 THB in cents
                        groupAmount = 2683L,       // 26.83 EUR in cents
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 50000L),  // 500 THB
                            ExpenseSplit(userId = "user-2", amountCents = 50000L)   // 500 THB
                        )
                    )
                )
            )
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            // 50000 * 2683 / 100000 = 1341.5 → HALF_UP → 1342
            assertEquals(1342L, balanceMap["user-1"]!!.spent)
            assertEquals(1342L, balanceMap["user-2"]!!.spent)
            // Total converted spent should approximate groupAmount (rounding may add ±1 cent)
            val totalSpent = result.sumOf { it.spent }
            assertTrue(totalSpent in 2683L..2684L)
        }

        @Test
        fun `mixed same-currency and foreign-currency expenses`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    // Same-currency expense: 40 EUR split equally
                    Expense(
                        id = "exp-eur",
                        sourceAmount = 4000L,
                        groupAmount = 4000L,
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 2000L),
                            ExpenseSplit(userId = "user-2", amountCents = 2000L)
                        )
                    ),
                    // Foreign expense: 1000 THB → 27 EUR, split equally
                    Expense(
                        id = "exp-thb",
                        sourceAmount = 100000L,  // 1000.00 THB
                        groupAmount = 2700L,      // 27.00 EUR
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 50000L),
                            ExpenseSplit(userId = "user-2", amountCents = 50000L)
                        )
                    )
                )
            )
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            // user-1: EUR expense = 2000, THB expense = 50000*2700/100000 = 1350 → total 3350
            assertEquals(3350L, balanceMap["user-1"]!!.spent)
            // user-2: same as user-1
            assertEquals(3350L, balanceMap["user-2"]!!.spent)
        }

        @Test
        fun `zero sourceAmount expense produces zero spent`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        id = "exp-zero",
                        sourceAmount = 0L,
                        groupAmount = 5000L,
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 2500L)
                        )
                    )
                )
            )
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            // sourceAmount == 0 → safety guard returns 0
            assertEquals(0L, balanceMap["user-1"]!!.spent)
        }
    }

    @Nested
    @DisplayName("Net balance and available calculation")
    inner class NetBalanceCalculation {

        @Test
        fun `net balance equals contributed minus withdrawn (pocket share)`() = runTest {
            val subunit = Subunit(
                id = "sub-1",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(
                    // user-1 contributes 10000 for couple → 5000 each
                    Contribution(userId = "user-1", subunitId = "sub-1", amount = 10000L),
                    // user-3 contributes individually
                    Contribution(userId = "user-3", amount = 5000L)
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    // GROUP withdrawal: 4000 / 4 = 1000 each
                    CashWithdrawal(
                        withdrawnBy = "user-1",
                        withdrawalScope = PayerType.GROUP,
                        deductedBaseAmount = 4000L
                    )
                )
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        id = "exp-1",
                        sourceAmount = 8000L,
                        groupAmount = 8000L,
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 2000L),
                            ExpenseSplit(userId = "user-2", amountCents = 2000L),
                            ExpenseSplit(userId = "user-3", amountCents = 2000L),
                            ExpenseSplit(userId = "user-4", amountCents = 2000L)
                        )
                    )
                )
            )
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(listOf(subunit))

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            // net = contributed - withdrawn (pocket share, expenses don't reduce pocket)
            // user-1: contributed=5000, withdrawn=1000, net=5000-1000=4000
            assertEquals(4000L, balanceMap["user-1"]!!.netBalance)
            // user-2: contributed=5000, withdrawn=1000, net=5000-1000=4000
            assertEquals(4000L, balanceMap["user-2"]!!.netBalance)
            // user-3: contributed=5000, withdrawn=1000, net=5000-1000=4000
            assertEquals(4000L, balanceMap["user-3"]!!.netBalance)
            // user-4: contributed=0, withdrawn=1000, net=0-1000=-1000
            assertEquals(-1000L, balanceMap["user-4"]!!.netBalance)

            // Verify per-member nets sum to group pocket balance
            // Group: contributed=15000, total withdrawn=4000, pocket=11000
            val totalNet = result.sumOf { it.netBalance }
            assertEquals(15000L - 4000L, totalNet)
        }

        @Test
        fun `available equals withdrawn minus spent (cash in hand)`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(userId = "user-1", amount = 10000L))
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        withdrawnBy = "user-1",
                        withdrawalScope = PayerType.USER,
                        deductedBaseAmount = 5000L
                    )
                )
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        id = "exp-1",
                        sourceAmount = 3000L,
                        groupAmount = 3000L,
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 3000L)
                        )
                    )
                )
            )
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            // user-1: withdrawn=5000, spent=3000, available=5000-3000=2000
            assertEquals(5000L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(3000L, balanceMap["user-1"]!!.spent)
            assertEquals(2000L, balanceMap["user-1"]!!.available)
            // net = contributed - withdrawn = 10000 - 5000 = 5000
            assertEquals(5000L, balanceMap["user-1"]!!.netBalance)
        }

        @Test
        fun `negative net balance indicates member overdrew from pocket`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(Contribution(userId = "user-1", amount = 1000L))
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    CashWithdrawal(
                        withdrawnBy = "user-1",
                        withdrawalScope = PayerType.USER,
                        deductedBaseAmount = 5000L
                    )
                )
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(emptyList())
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            // contributed=1000, withdrawn=5000, net=1000-5000=-4000
            assertEquals(-4000L, balanceMap["user-1"]!!.netBalance)
        }

        @Test
        fun `member with only expenses has zero net balance but available is negative`() = runTest {
            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(emptyList())
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        id = "exp-1",
                        sourceAmount = 5000L,
                        groupAmount = 5000L,
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 5000L)
                        )
                    )
                )
            )
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            // No contributions, no withdrawals → net = 0
            assertEquals(0L, balanceMap["user-1"]!!.netBalance)
            // spent=5000, withdrawn=0 → available = 0 - 5000 = -5000
            assertEquals(5000L, balanceMap["user-1"]!!.spent)
            assertEquals(-5000L, balanceMap["user-1"]!!.available)
        }
    }

    @Nested
    @DisplayName("Full scenario — wiki example")
    inner class FullScenario {

        @Test
        fun `solo users plus couples plus families in one group`() = runTest {
            // Setup: 4 people, one couple (user-1 & user-2), two solo (user-3, user-4)
            val couple = Subunit(
                id = "sub-couple",
                groupId = groupId,
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to BigDecimal("0.5"),
                    "user-2" to BigDecimal("0.5")
                )
            )

            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(
                    // user-1 contributes 100€ for the couple: 50€ each
                    Contribution(userId = "user-1", subunitId = "sub-couple", amount = 10000L),
                    // user-3 contributes 50€ individually
                    Contribution(userId = "user-3", amount = 5000L),
                    // user-4 contributes 50€ individually
                    Contribution(userId = "user-4", amount = 5000L)
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(
                listOf(
                    // GROUP withdrawal: 200€ water park → 50€ each
                    CashWithdrawal(
                        withdrawnBy = "user-3",
                        withdrawalScope = PayerType.GROUP,
                        deductedBaseAmount = 20000L
                    ),
                    // SUBUNIT withdrawal: 50€ souvenirs for couple → 25€ each
                    CashWithdrawal(
                        withdrawnBy = "user-1",
                        withdrawalScope = PayerType.SUBUNIT,
                        subunitId = "sub-couple",
                        deductedBaseAmount = 5000L
                    ),
                    // USER withdrawal: 5€ coffee for user-3
                    CashWithdrawal(
                        withdrawnBy = "user-3",
                        withdrawalScope = PayerType.USER,
                        deductedBaseAmount = 500L
                    )
                )
            )
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        id = "exp-dinner",
                        sourceAmount = 12000L,
                        groupAmount = 12000L,
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 3000L),
                            ExpenseSplit(userId = "user-2", amountCents = 3000L),
                            ExpenseSplit(userId = "user-3", amountCents = 3000L),
                            ExpenseSplit(userId = "user-4", amountCents = 3000L)
                        )
                    )
                )
            )
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(listOf(couple))

            val result = useCase(groupId, groupMemberIds).first()
            val balanceMap = result.associateBy { it.userId }

            // user-1: contributed=5000, withdrawn=GROUP(5000)+SUBUNIT(2500)=7500, spent=3000
            assertEquals(5000L, balanceMap["user-1"]!!.contributed)
            assertEquals(7500L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(3000L, balanceMap["user-1"]!!.spent)
            assertEquals(7500L - 3000L, balanceMap["user-1"]!!.available) // 4500
            assertEquals(5000L - 7500L, balanceMap["user-1"]!!.netBalance) // -2500

            // user-2: contributed=5000, withdrawn=GROUP(5000)+SUBUNIT(2500)=7500, spent=3000
            assertEquals(5000L, balanceMap["user-2"]!!.contributed)
            assertEquals(7500L, balanceMap["user-2"]!!.withdrawn)
            assertEquals(3000L, balanceMap["user-2"]!!.spent)
            assertEquals(4500L, balanceMap["user-2"]!!.available)
            assertEquals(-2500L, balanceMap["user-2"]!!.netBalance)

            // user-3: contributed=5000, withdrawn=GROUP(5000)+USER(500)=5500, spent=3000
            assertEquals(5000L, balanceMap["user-3"]!!.contributed)
            assertEquals(5500L, balanceMap["user-3"]!!.withdrawn)
            assertEquals(3000L, balanceMap["user-3"]!!.spent)
            assertEquals(2500L, balanceMap["user-3"]!!.available)
            assertEquals(-500L, balanceMap["user-3"]!!.netBalance)

            // user-4: contributed=5000, withdrawn=GROUP(5000), spent=3000
            assertEquals(5000L, balanceMap["user-4"]!!.contributed)
            assertEquals(5000L, balanceMap["user-4"]!!.withdrawn)
            assertEquals(3000L, balanceMap["user-4"]!!.spent)
            assertEquals(2000L, balanceMap["user-4"]!!.available)
            assertEquals(0L, balanceMap["user-4"]!!.netBalance)

            // Verify invariant: sum of net balances = total contributed - total withdrawn
            val totalContributed = result.sumOf { it.contributed }
            val totalWithdrawn = result.sumOf { it.withdrawn }
            assertEquals(totalContributed - totalWithdrawn, result.sumOf { it.netBalance })
            // Verify invariant: sum of available = total withdrawn - total spent
            val totalSpent = result.sumOf { it.spent }
            assertEquals(totalWithdrawn - totalSpent, result.sumOf { it.available })
        }

        @Test
        fun `multi-currency trip with EUR and THB expenses`() = runTest {
            // Group currency: EUR. Two members, no subunits.
            // Contributions: user-1 adds 100 EUR (10000 cents)
            // Expenses:
            //   - EUR dinner: 40 EUR (4000 cents), split 50/50 → 2000 each
            //   - THB taxi: 1000 THB (100000 cents) ≈ 27 EUR (2700 cents), split 50/50
            //     Each user spent 50000 THB → converted: 50000 * 2700 / 100000 = 1350 EUR cents
            val twoMembers = listOf("user-1", "user-2")

            every { contributionRepository.getGroupContributionsFlow(groupId) } returns flowOf(
                listOf(
                    Contribution(userId = "user-1", amount = 10000L)
                )
            )
            every { cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId) } returns flowOf(emptyList())
            every { expenseRepository.getGroupExpensesFlow(groupId) } returns flowOf(
                listOf(
                    Expense(
                        id = "exp-dinner-eur",
                        sourceAmount = 4000L,
                        groupAmount = 4000L,
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 2000L),
                            ExpenseSplit(userId = "user-2", amountCents = 2000L)
                        )
                    ),
                    Expense(
                        id = "exp-taxi-thb",
                        sourceAmount = 100000L,  // 1000.00 THB
                        groupAmount = 2700L,      // 27.00 EUR
                        splits = listOf(
                            ExpenseSplit(userId = "user-1", amountCents = 50000L),
                            ExpenseSplit(userId = "user-2", amountCents = 50000L)
                        )
                    )
                )
            )
            every { subunitRepository.getGroupSubunitsFlow(groupId) } returns flowOf(emptyList())

            val result = useCase(groupId, twoMembers).first()
            val balanceMap = result.associateBy { it.userId }

            // user-1: contributed=10000, withdrawn=0, spent=2000+1350=3350, net=10000-0=10000
            assertEquals(10000L, balanceMap["user-1"]!!.contributed)
            assertEquals(0L, balanceMap["user-1"]!!.withdrawn)
            assertEquals(3350L, balanceMap["user-1"]!!.spent)
            assertEquals(-3350L, balanceMap["user-1"]!!.available) // 0 - 3350
            assertEquals(10000L, balanceMap["user-1"]!!.netBalance)

            // user-2: contributed=0, withdrawn=0, spent=2000+1350=3350, net=0-0=0
            assertEquals(0L, balanceMap["user-2"]!!.contributed)
            assertEquals(0L, balanceMap["user-2"]!!.withdrawn)
            assertEquals(3350L, balanceMap["user-2"]!!.spent)
            assertEquals(-3350L, balanceMap["user-2"]!!.available) // 0 - 3350
            assertEquals(0L, balanceMap["user-2"]!!.netBalance)
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
}


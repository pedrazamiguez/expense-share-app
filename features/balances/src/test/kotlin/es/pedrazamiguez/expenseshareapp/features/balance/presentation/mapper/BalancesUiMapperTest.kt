package es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.CurrencyAmount
import es.pedrazamiguez.expenseshareapp.domain.model.MemberBalance
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ActivityItemUiModel
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("BalancesUiMapper")
class BalancesUiMapperTest {

    private lateinit var mapper: BalancesUiMapper
    private lateinit var localeProvider: LocaleProvider
    private lateinit var resourceProvider: ResourceProvider

    @BeforeEach
    fun setUp() {
        localeProvider = mockk()
        resourceProvider = mockk()
        every { localeProvider.getCurrentLocale() } returns Locale.US
        every { resourceProvider.getString(R.string.balances_contribution_scope_personal) } returns "Personal"
        every { resourceProvider.getString(R.string.balances_contribution_scope_group) } returns "Group"
        every { resourceProvider.getString(R.string.balances_withdraw_cash_scope_personal) } returns "Personal"
        every { resourceProvider.getString(R.string.balances_withdraw_cash_scope_group) } returns "Group"
        mapper = BalancesUiMapper(localeProvider, resourceProvider)
    }

    @Nested
    @DisplayName("mapActivity – merge & sort")
    inner class MapActivity {

        @Test
        fun `returns empty list when no contributions and no withdrawals`() {
            val result = mapper.mapActivity(
                contributions = emptyList(),
                withdrawals = emptyList(),
                groupCurrency = "EUR",
                currentUserId = null
            )

            assertTrue(result.isEmpty())
        }

        @Test
        fun `returns contributions sorted by date descending when no withdrawals`() {
            val older = Contribution(
                id = "c1",
                groupId = "g1",
                userId = "u1",
                amount = 10000,
                currency = "EUR",
                createdAt = LocalDateTime.of(2026, 1, 10, 10, 0)
            )
            val newer = Contribution(
                id = "c2",
                groupId = "g1",
                userId = "u2",
                amount = 20000,
                currency = "EUR",
                createdAt = LocalDateTime.of(2026, 1, 15, 14, 0)
            )

            val result = mapper.mapActivity(
                contributions = listOf(older, newer),
                withdrawals = emptyList(),
                groupCurrency = "EUR",
                currentUserId = null
            )

            assertEquals(2, result.size)
            // Newest first
            assertIsContribution(result[0], "c2")
            assertIsContribution(result[1], "c1")
        }

        @Test
        fun `returns withdrawals sorted by date descending when no contributions`() {
            val older = cashWithdrawal(
                id = "cw1",
                createdAt = LocalDateTime.of(2026, 1, 10, 10, 0)
            )
            val newer = cashWithdrawal(
                id = "cw2",
                createdAt = LocalDateTime.of(2026, 1, 15, 14, 0)
            )

            val result = mapper.mapActivity(
                contributions = emptyList(),
                withdrawals = listOf(older, newer),
                groupCurrency = "EUR",
                currentUserId = null
            )

            assertEquals(2, result.size)
            assertIsWithdrawal(result[0], "cw2")
            assertIsWithdrawal(result[1], "cw1")
        }

        @Test
        fun `interleaves contributions and withdrawals sorted by date descending`() {
            val jan10Contribution = Contribution(
                id = "c1",
                groupId = "g1",
                userId = "u1",
                amount = 10000,
                currency = "EUR",
                createdAt = LocalDateTime.of(2026, 1, 10, 10, 0)
            )
            val jan12Withdrawal = cashWithdrawal(
                id = "cw1",
                createdAt = LocalDateTime.of(2026, 1, 12, 8, 0)
            )
            val jan14Contribution = Contribution(
                id = "c2",
                groupId = "g1",
                userId = "u2",
                amount = 20000,
                currency = "EUR",
                createdAt = LocalDateTime.of(2026, 1, 14, 14, 0)
            )
            val jan16Withdrawal = cashWithdrawal(
                id = "cw2",
                createdAt = LocalDateTime.of(2026, 1, 16, 12, 0)
            )

            val result = mapper.mapActivity(
                contributions = listOf(jan10Contribution, jan14Contribution),
                withdrawals = listOf(jan12Withdrawal, jan16Withdrawal),
                groupCurrency = "EUR",
                currentUserId = null
            )

            assertEquals(4, result.size)
            // Jan 16 withdrawal → Jan 14 contribution → Jan 12 withdrawal → Jan 10 contribution
            assertIsWithdrawal(result[0], "cw2")
            assertIsContribution(result[1], "c2")
            assertIsWithdrawal(result[2], "cw1")
            assertIsContribution(result[3], "c1")
        }

        @Test
        fun `items with null createdAt are placed at the end`() {
            val withDate = Contribution(
                id = "c1",
                groupId = "g1",
                userId = "u1",
                amount = 10000,
                currency = "EUR",
                createdAt = LocalDateTime.of(2026, 1, 15, 10, 0)
            )
            val withoutDate = Contribution(
                id = "c2",
                groupId = "g1",
                userId = "u2",
                amount = 20000,
                currency = "EUR",
                createdAt = null
            )
            val withdrawalWithoutDate = cashWithdrawal(
                id = "cw1",
                createdAt = null
            )

            val result = mapper.mapActivity(
                contributions = listOf(withDate, withoutDate),
                withdrawals = listOf(withdrawalWithoutDate),
                groupCurrency = "EUR",
                currentUserId = null
            )

            assertEquals(3, result.size)
            // Dated item first, null-dated items at the end (timestamp = 0)
            assertIsContribution(result[0], "c1")
            // Both null-dated items have timestamp 0, stable order not guaranteed
            val nullDatedIds = listOf(result[1], result[2]).map { activityId(it) }.toSet()
            assertEquals(setOf("c2", "cw1"), nullDatedIds)
        }

        @Test
        fun `sortTimestamp is correctly computed from createdAt`() {
            val dateTime = LocalDateTime.of(2026, 3, 7, 12, 30)
            val contribution = Contribution(
                id = "c1",
                groupId = "g1",
                userId = "u1",
                amount = 5000,
                currency = "EUR",
                createdAt = dateTime
            )

            val result = mapper.mapActivity(
                contributions = listOf(contribution),
                withdrawals = emptyList(),
                groupCurrency = "EUR",
                currentUserId = null
            )

            assertEquals(1, result.size)
            val item = result[0] as ActivityItemUiModel.ContributionItem
            assertTrue(item.sortTimestamp > 0)
        }

        @Test
        fun `currentUserId correctly marks isCurrentUser on contributions`() {
            val contribution = Contribution(
                id = "c1",
                groupId = "g1",
                userId = "current-user",
                amount = 5000,
                currency = "EUR",
                createdAt = LocalDateTime.of(2026, 1, 10, 10, 0)
            )
            val otherContribution = Contribution(
                id = "c2",
                groupId = "g1",
                userId = "other-user",
                amount = 3000,
                currency = "EUR",
                createdAt = LocalDateTime.of(2026, 1, 9, 10, 0)
            )

            val result = mapper.mapActivity(
                contributions = listOf(contribution, otherContribution),
                withdrawals = emptyList(),
                groupCurrency = "EUR",
                currentUserId = "current-user"
            )

            val c1 = result.first { it is ActivityItemUiModel.ContributionItem && it.contribution.id == "c1" }
                as ActivityItemUiModel.ContributionItem
            val c2 = result.first { it is ActivityItemUiModel.ContributionItem && it.contribution.id == "c2" }
                as ActivityItemUiModel.ContributionItem

            assertTrue(c1.contribution.isCurrentUser)
            assertTrue(!c2.contribution.isCurrentUser)
        }

        @Test
        fun `currentUserId correctly marks isCurrentUser on withdrawals`() {
            val withdrawal = cashWithdrawal(
                id = "cw1",
                withdrawnBy = "current-user",
                createdAt = LocalDateTime.of(2026, 1, 10, 10, 0)
            )
            val otherWithdrawal = cashWithdrawal(
                id = "cw2",
                withdrawnBy = "other-user",
                createdAt = LocalDateTime.of(2026, 1, 9, 10, 0)
            )

            val result = mapper.mapActivity(
                contributions = emptyList(),
                withdrawals = listOf(withdrawal, otherWithdrawal),
                groupCurrency = "EUR",
                currentUserId = "current-user"
            )

            val cw1 = result.first { it is ActivityItemUiModel.CashWithdrawalItem && it.withdrawal.id == "cw1" }
                as ActivityItemUiModel.CashWithdrawalItem
            val cw2 = result.first { it is ActivityItemUiModel.CashWithdrawalItem && it.withdrawal.id == "cw2" }
                as ActivityItemUiModel.CashWithdrawalItem

            assertTrue(cw1.withdrawal.isCurrentUser)
            assertTrue(!cw2.withdrawal.isCurrentUser)
        }

        @Test
        fun `foreign currency withdrawal shows formattedDeducted`() {
            val foreignWithdrawal = CashWithdrawal(
                id = "cw1", groupId = "g1", withdrawnBy = "u1",
                amountWithdrawn = 1000000, remainingAmount = 770000,
                currency = "THB", deductedBaseAmount = 27000,
                exchangeRate = BigDecimal("37.037"),
                createdAt = LocalDateTime.of(2026, 1, 15, 10, 0)
            )

            val result = mapper.mapActivity(
                contributions = emptyList(),
                withdrawals = listOf(foreignWithdrawal),
                groupCurrency = "EUR",
                currentUserId = null
            )

            val item = result[0] as ActivityItemUiModel.CashWithdrawalItem
            assertTrue(item.withdrawal.isForeignCurrency)
            assertTrue(item.withdrawal.formattedDeducted.isNotBlank())
        }

        @Test
        fun `same currency withdrawal has empty formattedDeducted`() {
            val sameWithdrawal = cashWithdrawal(
                id = "cw1",
                currency = "EUR",
                createdAt = LocalDateTime.of(2026, 1, 15, 10, 0)
            )

            val result = mapper.mapActivity(
                contributions = emptyList(),
                withdrawals = listOf(sameWithdrawal),
                groupCurrency = "EUR",
                currentUserId = null
            )

            val item = result[0] as ActivityItemUiModel.CashWithdrawalItem
            assertTrue(!item.withdrawal.isForeignCurrency)
            assertEquals("", item.withdrawal.formattedDeducted)
        }

        @Test
        fun `many items preserve strict descending chronological order`() {
            val contributions = (1..5).map { day ->
                Contribution(
                    id = "c$day",
                    groupId = "g1",
                    userId = "u1",
                    amount = (day * 1000).toLong(),
                    currency = "EUR",
                    createdAt = LocalDateTime.of(2026, 1, day * 2, 10, 0)
                )
            }
            val withdrawals = (1..5).map { day ->
                cashWithdrawal(
                    id = "cw$day",
                    createdAt = LocalDateTime.of(2026, 1, day * 2 + 1, 10, 0)
                )
            }

            val result = mapper.mapActivity(
                contributions = contributions,
                withdrawals = withdrawals,
                groupCurrency = "EUR",
                currentUserId = null
            )

            assertEquals(10, result.size)

            // Verify strictly descending timestamps
            for (i in 0 until result.size - 1) {
                assertTrue(
                    result[i].sortTimestamp >= result[i + 1].sortTimestamp,
                    "Item at index $i (ts=${result[i].sortTimestamp}) should be >= item at index ${i + 1} (ts=${result[i + 1].sortTimestamp})"
                )
            }
        }
    }

    @Nested
    @DisplayName("mapContributions – contribution scope")
    inner class ContributionScope {

        private val testSubunit = Subunit(
            id = "subunit-1",
            groupId = "g1",
            name = "Antonio & Me",
            memberIds = listOf("u1", "u2")
        )
        private val subunitsMap = mapOf("subunit-1" to testSubunit)

        @Test
        fun `SUBUNIT-scoped contribution resolves subunit name as scopeLabel`() {
            val contribution = Contribution(
                id = "c1",
                groupId = "g1",
                userId = "u1",
                contributionScope = PayerType.SUBUNIT,
                subunitId = "subunit-1",
                amount = 10000,
                currency = "EUR",
                createdAt = LocalDateTime.of(2026, 1, 15, 10, 0)
            )

            val result = mapper.mapContributions(
                contributions = listOf(contribution),
                currentUserId = "u1",
                subunits = subunitsMap
            )

            assertEquals(1, result.size)
            assertEquals("Antonio & Me", result[0].scopeLabel)
            assertTrue(result[0].isSubunitContribution)
            assertFalse(result[0].isPersonalContribution)
            assertFalse(result[0].isGroupContribution)
        }

        @Test
        fun `USER-scoped contribution has Personal as scopeLabel`() {
            val contribution = Contribution(
                id = "c1",
                groupId = "g1",
                userId = "u1",
                contributionScope = PayerType.USER,
                subunitId = null,
                amount = 10000,
                currency = "EUR",
                createdAt = LocalDateTime.of(2026, 1, 15, 10, 0)
            )

            val result = mapper.mapContributions(
                contributions = listOf(contribution),
                currentUserId = "u1",
                subunits = subunitsMap
            )

            assertEquals(1, result.size)
            assertEquals("Personal", result[0].scopeLabel)
            assertFalse(result[0].isSubunitContribution)
            assertTrue(result[0].isPersonalContribution)
            assertFalse(result[0].isGroupContribution)
        }

        @Test
        fun `GROUP-scoped contribution has Group as scopeLabel`() {
            val contribution = Contribution(
                id = "c1",
                groupId = "g1",
                userId = "u1",
                contributionScope = PayerType.GROUP,
                subunitId = null,
                amount = 10000,
                currency = "EUR",
                createdAt = LocalDateTime.of(2026, 1, 15, 10, 0)
            )

            val result = mapper.mapContributions(
                contributions = listOf(contribution),
                currentUserId = "u1",
                subunits = subunitsMap
            )

            assertEquals(1, result.size)
            assertEquals("Group", result[0].scopeLabel)
            assertFalse(result[0].isSubunitContribution)
            assertFalse(result[0].isPersonalContribution)
            assertTrue(result[0].isGroupContribution)
        }

        @Test
        fun `SUBUNIT-scoped contribution with unknown subunitId has null scopeLabel`() {
            val contribution = Contribution(
                id = "c1",
                groupId = "g1",
                userId = "u1",
                contributionScope = PayerType.SUBUNIT,
                subunitId = "nonexistent",
                amount = 10000,
                currency = "EUR",
                createdAt = LocalDateTime.of(2026, 1, 15, 10, 0)
            )

            val result = mapper.mapContributions(
                contributions = listOf(contribution),
                currentUserId = "u1",
                subunits = subunitsMap
            )

            assertEquals(1, result.size)
            assertEquals(null, result[0].scopeLabel)
            assertTrue(result[0].isSubunitContribution)
        }

        @Test
        fun `mapActivity passes scope fields through to contribution items`() {
            val contribution = Contribution(
                id = "c1",
                groupId = "g1",
                userId = "u1",
                contributionScope = PayerType.SUBUNIT,
                subunitId = "subunit-1",
                amount = 10000,
                currency = "EUR",
                createdAt = LocalDateTime.of(2026, 1, 15, 10, 0)
            )

            val result = mapper.mapActivity(
                contributions = listOf(contribution),
                withdrawals = emptyList(),
                groupCurrency = "EUR",
                currentUserId = "u1",
                subunits = subunitsMap
            )

            assertEquals(1, result.size)
            val item = result[0] as ActivityItemUiModel.ContributionItem
            assertEquals("Antonio & Me", item.contribution.scopeLabel)
            assertTrue(item.contribution.isSubunitContribution)
        }
    }

    @Nested
    @DisplayName("mapCashWithdrawals – withdrawal scope")
    inner class CashWithdrawalScope {

        private val subunitsMap = mapOf(
            "subunit-1" to Subunit(
                id = "subunit-1",
                name = "Antonio & Me",
                groupId = "g1",
                memberIds = listOf("u1", "u2")
            )
        )

        @Test
        fun `GROUP-scoped withdrawal has Group as scopeLabel`() {
            val withdrawal = cashWithdrawal(
                id = "cw1",
                withdrawalScope = PayerType.GROUP,
                createdAt = LocalDateTime.of(2026, 1, 15, 10, 0)
            )

            val result = mapper.mapCashWithdrawals(
                withdrawals = listOf(withdrawal),
                groupCurrency = "EUR",
                currentUserId = "u1",
                subunits = subunitsMap
            )

            assertEquals(1, result.size)
            assertEquals("Group", result[0].scopeLabel)
            assertEquals(false, result[0].isSubunitWithdrawal)
            assertEquals(false, result[0].isPersonalWithdrawal)
            assertEquals(true, result[0].isGroupWithdrawal)
        }

        @Test
        fun `SUBUNIT-scoped withdrawal has subunit name as scopeLabel`() {
            val withdrawal = cashWithdrawal(
                id = "cw1",
                withdrawalScope = PayerType.SUBUNIT,
                subunitId = "subunit-1",
                createdAt = LocalDateTime.of(2026, 1, 15, 10, 0)
            )

            val result = mapper.mapCashWithdrawals(
                withdrawals = listOf(withdrawal),
                groupCurrency = "EUR",
                currentUserId = "u1",
                subunits = subunitsMap
            )

            assertEquals(1, result.size)
            assertEquals("Antonio & Me", result[0].scopeLabel)
            assertEquals(true, result[0].isSubunitWithdrawal)
            assertEquals(false, result[0].isPersonalWithdrawal)
        }

        @Test
        fun `USER-scoped withdrawal has Personal as scopeLabel`() {
            val withdrawal = cashWithdrawal(
                id = "cw1",
                withdrawalScope = PayerType.USER,
                createdAt = LocalDateTime.of(2026, 1, 15, 10, 0)
            )

            val result = mapper.mapCashWithdrawals(
                withdrawals = listOf(withdrawal),
                groupCurrency = "EUR",
                currentUserId = "u1",
                subunits = subunitsMap
            )

            assertEquals(1, result.size)
            assertEquals("Personal", result[0].scopeLabel)
            assertEquals(false, result[0].isSubunitWithdrawal)
            assertEquals(true, result[0].isPersonalWithdrawal)
        }

        @Test
        fun `SUBUNIT-scoped withdrawal with unknown subunitId has null scopeLabel`() {
            val withdrawal = cashWithdrawal(
                id = "cw1",
                withdrawalScope = PayerType.SUBUNIT,
                subunitId = "nonexistent",
                createdAt = LocalDateTime.of(2026, 1, 15, 10, 0)
            )

            val result = mapper.mapCashWithdrawals(
                withdrawals = listOf(withdrawal),
                groupCurrency = "EUR",
                currentUserId = "u1",
                subunits = subunitsMap
            )

            assertEquals(1, result.size)
            assertEquals(null, result[0].scopeLabel)
            assertEquals(true, result[0].isSubunitWithdrawal)
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun cashWithdrawal(
        id: String,
        groupId: String = "g1",
        withdrawnBy: String = "u1",
        withdrawalScope: PayerType = PayerType.GROUP,
        subunitId: String? = null,
        amountWithdrawn: Long = 100000,
        remainingAmount: Long = 100000,
        currency: String = "THB",
        deductedBaseAmount: Long = 27000,
        exchangeRate: BigDecimal = BigDecimal("37.037"),
        createdAt: LocalDateTime? = null
    ) = CashWithdrawal(
        id = id,
        groupId = groupId,
        withdrawnBy = withdrawnBy,
        withdrawalScope = withdrawalScope,
        subunitId = subunitId,
        amountWithdrawn = amountWithdrawn,
        remainingAmount = remainingAmount,
        currency = currency,
        deductedBaseAmount = deductedBaseAmount,
        exchangeRate = exchangeRate,
        createdAt = createdAt
    )

    private fun assertIsContribution(item: ActivityItemUiModel, expectedId: String) {
        assertTrue(
            item is ActivityItemUiModel.ContributionItem,
            "Expected ContributionItem but got ${item::class.simpleName}"
        )
        assertEquals(expectedId, (item as ActivityItemUiModel.ContributionItem).contribution.id)
    }

    private fun assertIsWithdrawal(item: ActivityItemUiModel, expectedId: String) {
        assertTrue(
            item is ActivityItemUiModel.CashWithdrawalItem,
            "Expected CashWithdrawalItem but got ${item::class.simpleName}"
        )
        assertEquals(expectedId, (item as ActivityItemUiModel.CashWithdrawalItem).withdrawal.id)
    }

    private fun activityId(item: ActivityItemUiModel): String = when (item) {
        is ActivityItemUiModel.ContributionItem -> item.contribution.id
        is ActivityItemUiModel.CashWithdrawalItem -> item.withdrawal.id
    }

    @Nested
    @DisplayName("mapMemberBalances")
    inner class MapMemberBalances {

        private val currency = "EUR"
        private val currentUserId = "user-1"
        private val memberProfiles = mapOf(
            "user-1" to User(userId = "user-1", email = "alice@test.com", displayName = "Alice"),
            "user-2" to User(userId = "user-2", email = "bob@test.com", displayName = "Bob"),
            "user-3" to User(userId = "user-3", email = "charlie@test.com")
        )

        @Test
        fun `returns empty list when no balances`() {
            val result = mapper.mapMemberBalances(
                balances = emptyList(),
                currency = currency,
                currentUserId = currentUserId,
                memberProfiles = memberProfiles
            )

            assertTrue(result.isEmpty())
        }

        @Test
        fun `formats all amount fields correctly`() {
            val balances = listOf(
                MemberBalance(
                    userId = "user-1",
                    contributed = 5000L,
                    withdrawn = 3000L,
                    cashSpent = 500L,
                    nonCashSpent = 500L,
                    totalSpent = 1000L,
                    pocketBalance = 1500L,
                    cashInHand = 2500L
                )
            )

            val result = mapper.mapMemberBalances(
                balances = balances,
                currency = currency,
                currentUserId = currentUserId,
                memberProfiles = memberProfiles
            )

            assertEquals(1, result.size)
            val item = result[0]
            // US locale EUR formatting: €50.00 (contributed), €25.00 (cashInHand), €10.00 (spent), €15.00 (pocket)
            assertTrue(item.formattedContributed.contains("50"))
            assertTrue(item.formattedCashInHand.contains("25"))
            assertTrue(item.formattedTotalSpent.contains("10"))
            assertTrue(item.formattedPocketBalance.contains("15"))
        }

        @Test
        fun `resolves display name from profiles`() {
            val balances = listOf(
                MemberBalance(userId = "user-1", pocketBalance = 0L),
                MemberBalance(userId = "user-2", pocketBalance = 0L),
                MemberBalance(userId = "user-3", pocketBalance = 0L)
            )

            val result = mapper.mapMemberBalances(
                balances = balances,
                currency = currency,
                currentUserId = currentUserId,
                memberProfiles = memberProfiles
            )

            val byUser = result.associateBy { it.userId }
            assertEquals("Alice", byUser["user-1"]!!.displayName)
            assertEquals("Bob", byUser["user-2"]!!.displayName)
            // user-3 has no displayName, falls back to email
            assertEquals("charlie@test.com", byUser["user-3"]!!.displayName)
        }

        @Test
        fun `marks current user correctly`() {
            val balances = listOf(
                MemberBalance(userId = "user-1", pocketBalance = 100L),
                MemberBalance(userId = "user-2", pocketBalance = -100L)
            )

            val result = mapper.mapMemberBalances(
                balances = balances,
                currency = currency,
                currentUserId = "user-1",
                memberProfiles = memberProfiles
            )

            assertTrue(result[0].isCurrentUser)
            assertFalse(result[1].isCurrentUser)
        }

        @Test
        fun `current user is sorted first`() {
            val balances = listOf(
                MemberBalance(userId = "user-2", pocketBalance = -5000L),
                MemberBalance(userId = "user-1", pocketBalance = 100L)
            )

            val result = mapper.mapMemberBalances(
                balances = balances,
                currency = currency,
                currentUserId = "user-1",
                memberProfiles = memberProfiles
            )

            assertEquals("user-1", result[0].userId)
            assertEquals("user-2", result[1].userId)
        }

        @Test
        fun `members sorted by absolute pocketBalance descending after current user`() {
            val balances = listOf(
                MemberBalance(userId = "user-1", pocketBalance = 100L),
                MemberBalance(userId = "user-2", pocketBalance = -5000L),
                MemberBalance(userId = "user-3", pocketBalance = 3000L)
            )

            val result = mapper.mapMemberBalances(
                balances = balances,
                currency = currency,
                currentUserId = "user-1",
                memberProfiles = memberProfiles
            )

            // user-1 first (current user), then user-2 (|5000|), then user-3 (|3000|)
            assertEquals("user-1", result[0].userId)
            assertEquals("user-2", result[1].userId)
            assertEquals("user-3", result[2].userId)
        }

        @Test
        fun `positive balance flagged correctly`() {
            val balances = listOf(
                MemberBalance(userId = "user-1", pocketBalance = 1000L),
                MemberBalance(userId = "user-2", pocketBalance = -500L),
                MemberBalance(userId = "user-3", pocketBalance = 0L)
            )

            val result = mapper.mapMemberBalances(
                balances = balances,
                currency = currency,
                currentUserId = null,
                memberProfiles = memberProfiles
            )

            val byUser = result.associateBy { it.userId }
            assertTrue(byUser["user-1"]!!.isPositiveBalance)
            assertFalse(byUser["user-2"]!!.isPositiveBalance)
            assertTrue(byUser["user-3"]!!.isPositiveBalance) // zero is positive
        }

        @Test
        fun `formats cashSpent and nonCashSpent fields`() {
            val balances = listOf(
                MemberBalance(
                    userId = "user-1",
                    cashSpent = 1500L,
                    nonCashSpent = 2500L,
                    totalSpent = 4000L,
                    pocketBalance = 1000L
                )
            )

            val result = mapper.mapMemberBalances(
                balances = balances,
                currency = currency,
                currentUserId = currentUserId,
                memberProfiles = memberProfiles
            )

            val item = result[0]
            assertTrue(item.formattedCashSpent.contains("15"))
            assertTrue(item.formattedNonCashSpent.contains("25"))
        }

        @Test
        fun `maps currency breakdown with foreign equivalent`() {
            val balances = listOf(
                MemberBalance(
                    userId = "user-1",
                    pocketBalance = 1000L,
                    cashInHandByCurrency = listOf(
                        CurrencyAmount(currency = "THB", amountCents = 50000L, equivalentCents = 1342L)
                    )
                )
            )

            val result = mapper.mapMemberBalances(
                balances = balances,
                currency = currency,
                currentUserId = currentUserId,
                memberProfiles = memberProfiles,
                groupCurrency = "EUR"
            )

            val item = result[0]
            assertEquals(1, item.cashInHandByCurrency.size)
            val thb = item.cashInHandByCurrency[0]
            assertEquals("THB", thb.currency)
            assertTrue(thb.formattedAmount.isNotBlank())
            assertTrue(thb.formattedEquivalent.isNotBlank()) // foreign → shows equivalent
        }

        @Test
        fun `currency breakdown equivalent is empty for group currency`() {
            val balances = listOf(
                MemberBalance(
                    userId = "user-1",
                    pocketBalance = 1000L,
                    cashInHandByCurrency = listOf(
                        CurrencyAmount(currency = "EUR", amountCents = 5000L, equivalentCents = 5000L)
                    )
                )
            )

            val result = mapper.mapMemberBalances(
                balances = balances,
                currency = currency,
                currentUserId = currentUserId,
                memberProfiles = memberProfiles,
                groupCurrency = "EUR"
            )

            val item = result[0]
            assertEquals(1, item.cashInHandByCurrency.size)
            assertEquals("", item.cashInHandByCurrency[0].formattedEquivalent) // same currency → empty
        }

        @Test
        fun `empty per-currency lists produce empty ImmutableLists`() {
            val balances = listOf(
                MemberBalance(userId = "user-1", pocketBalance = 0L)
            )

            val result = mapper.mapMemberBalances(
                balances = balances,
                currency = currency,
                currentUserId = currentUserId,
                memberProfiles = memberProfiles,
                groupCurrency = "EUR"
            )

            val item = result[0]
            assertTrue(item.cashInHandByCurrency.isEmpty())
            assertTrue(item.cashSpentByCurrency.isEmpty())
            assertTrue(item.nonCashSpentByCurrency.isEmpty())
        }

        @Test
        fun `maps all three per-currency breakdown lists`() {
            val balances = listOf(
                MemberBalance(
                    userId = "user-1",
                    pocketBalance = 1000L,
                    cashInHandByCurrency = listOf(
                        CurrencyAmount(currency = "THB", amountCents = 50000L, equivalentCents = 1342L)
                    ),
                    cashSpentByCurrency = listOf(
                        CurrencyAmount(currency = "THB", amountCents = 4500L, equivalentCents = 121L)
                    ),
                    nonCashSpentByCurrency = listOf(
                        CurrencyAmount(currency = "EUR", amountCents = 1000L, equivalentCents = 1000L),
                        CurrencyAmount(currency = "THB", amountCents = 20000L, equivalentCents = 540L)
                    )
                )
            )

            val result = mapper.mapMemberBalances(
                balances = balances,
                currency = currency,
                currentUserId = currentUserId,
                memberProfiles = memberProfiles,
                groupCurrency = "EUR"
            )

            val item = result[0]
            assertEquals(1, item.cashInHandByCurrency.size)
            assertEquals(1, item.cashSpentByCurrency.size)
            assertEquals(2, item.nonCashSpentByCurrency.size)
        }
    }
}

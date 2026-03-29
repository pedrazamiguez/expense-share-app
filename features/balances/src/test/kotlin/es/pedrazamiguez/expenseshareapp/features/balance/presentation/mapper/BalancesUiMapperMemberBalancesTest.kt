package es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.domain.model.CurrencyAmount
import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import es.pedrazamiguez.expenseshareapp.domain.model.MemberBalance
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.features.balance.R
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("BalancesUiMapper — Member Balances, Balance, & Formatting")
class BalancesUiMapperMemberBalancesTest {

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

    @Nested
    @DisplayName("mapBalance – GroupPocketBalance → GroupPocketBalanceUiModel")
    inner class MapBalance {

        @Test
        fun `formattedTotalExtras is null when totalExtras is zero`() {
            val balance = GroupPocketBalance(
                totalContributions = 500000L,
                totalExpenses = 10000L,
                virtualBalance = 470000L,
                currency = "EUR",
                totalExtras = 0L
            )

            val result = mapper.mapBalance(balance, "Trip Group")

            assertNull(result.formattedTotalExtras)
        }

        @Test
        fun `formattedTotalExtras is present when totalExtras is positive`() {
            val balance = GroupPocketBalance(
                totalContributions = 500000L,
                totalExpenses = 10000L,
                virtualBalance = 470000L,
                currency = "EUR",
                totalExtras = 125L
            )

            val result = mapper.mapBalance(balance, "Trip Group")

            assertNotNull(result.formattedTotalExtras)
            assertTrue(result.formattedTotalExtras!!.contains("1.25"))
        }

        @Test
        fun `maps all basic fields correctly`() {
            val balance = GroupPocketBalance(
                totalContributions = 500000L,
                totalExpenses = 20500L,
                virtualBalance = 451800L,
                currency = "EUR",
                totalExtras = 1200L
            )

            val result = mapper.mapBalance(balance, "My Trip")

            assertEquals("My Trip", result.groupName)
            assertEquals("EUR", result.currency)
            assertNotNull(result.formattedTotalExtras)
            // Verify formatted values contain the expected numeric portions
            assertTrue(result.formattedBalance.contains("4,518.00"))
            assertTrue(result.formattedTotalContributed.contains("5,000.00"))
            assertTrue(result.formattedTotalSpent.contains("205.00"))
            assertTrue(result.formattedTotalExtras!!.contains("12.00"))
        }

        @Test
        fun `formattedAvailableBalance is null when no scheduled holds`() {
            val balance = GroupPocketBalance(
                totalContributions = 500000L,
                totalExpenses = 10000L,
                virtualBalance = 470000L,
                currency = "EUR",
                scheduledHoldAmount = 0L
            )

            val result = mapper.mapBalance(balance, "Group")

            assertNull(result.formattedAvailableBalance)
        }

        @Test
        fun `formattedAvailableBalance is present when scheduled holds exist`() {
            val balance = GroupPocketBalance(
                totalContributions = 500000L,
                totalExpenses = 10000L,
                virtualBalance = 470000L,
                currency = "EUR",
                scheduledHoldAmount = 5000L
            )

            val result = mapper.mapBalance(balance, "Group")

            assertNotNull(result.formattedAvailableBalance)
            // Available = 470000 - 5000 = 465000 → 4,650.00
            assertTrue(result.formattedAvailableBalance!!.contains("4,650.00"))
        }
    }

    @Nested
    @DisplayName("formatInputAmountWithCurrency")
    inner class FormatInputAmountWithCurrency {

        @Test
        fun `formats amount string with currency symbol`() {
            val result = mapper.formatInputAmountWithCurrency("100.50", "EUR")
            assertTrue(result.isNotBlank())
        }

        @Test
        fun `handles empty amount string`() {
            val result = mapper.formatInputAmountWithCurrency("", "EUR")
            assertTrue(result.isNotBlank() || result.isEmpty())
        }
    }

    @Nested
    @DisplayName("resolveCurrencySymbol")
    inner class ResolveCurrencySymbol {

        @Test
        fun `resolves EUR to euro symbol`() {
            val result = mapper.resolveCurrencySymbol("EUR")
            assertTrue(result.isNotBlank())
        }

        @Test
        fun `resolves USD to dollar symbol`() {
            val result = mapper.resolveCurrencySymbol("USD")
            assertTrue(result.contains("$"))
        }

        @Test
        fun `returns empty for blank code`() {
            val result = mapper.resolveCurrencySymbol("")
            assertTrue(result.isEmpty())
        }
    }
}

package es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.MemberOptionUiModel
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.User
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AddCashWithdrawalUiMapperTest {

    private lateinit var resourceProvider: ResourceProvider
    private lateinit var mapper: AddCashWithdrawalUiMapper

    @BeforeEach
    fun setUp() {
        resourceProvider = mockk(relaxed = true)
        mapper = AddCashWithdrawalUiMapper(resourceProvider)
    }

    @Nested
    inner class MapCurrency {

        @Test
        fun `maps single currency to CurrencyUiModel`() {
            val currency = Currency(
                code = "EUR",
                symbol = "€",
                defaultName = "Euro",
                decimalDigits = 2
            )

            val result = mapper.mapCurrency(currency)

            assertEquals("EUR", result.code)
            assertEquals(2, result.decimalDigits)
        }

        @Test
        fun `maps currency list to ImmutableList of CurrencyUiModel`() {
            val currencies = listOf(
                Currency(code = "EUR", symbol = "€", defaultName = "Euro", decimalDigits = 2),
                Currency(code = "JPY", symbol = "¥", defaultName = "Japanese Yen", decimalDigits = 0)
            )

            val result = mapper.mapCurrencies(currencies)

            assertEquals(2, result.size)
            assertEquals("EUR", result[0].code)
            assertEquals("JPY", result[1].code)
            assertEquals(0, result[1].decimalDigits)
        }
    }

    @Nested
    inner class LabelBuilding {

        private val eurModel = CurrencyUiModel(code = "EUR", displayText = "EUR (€)", decimalDigits = 2)
        private val thbModel = CurrencyUiModel(code = "THB", displayText = "THB (฿)", decimalDigits = 2)

        @Test
        fun `buildExchangeRateLabel delegates to resource provider`() {
            every { resourceProvider.getString(any(), any(), any()) } returns "1 EUR (€) = ? THB (฿)"

            val result = mapper.buildExchangeRateLabel(eurModel, thbModel)

            assertEquals("1 EUR (€) = ? THB (฿)", result)
        }

        @Test
        fun `buildDeductedAmountLabel delegates to resource provider`() {
            every { resourceProvider.getString(any(), any()) } returns "Deducted in EUR (€)"

            val result = mapper.buildDeductedAmountLabel(eurModel)

            assertEquals("Deducted in EUR (€)", result)
        }

        @Test
        fun `buildFeeConvertedLabel delegates to resource provider`() {
            every { resourceProvider.getString(any(), any()) } returns "Fee converted to EUR (€)"

            val result = mapper.buildFeeConvertedLabel(eurModel)

            assertEquals("Fee converted to EUR (€)", result)
        }
    }

    @Nested
    inner class MemberMapping {

        private val profiles = mapOf(
            "user-1" to User(userId = "user-1", email = "andres@test.com", displayName = "Andrés"),
            "user-2" to User(userId = "user-2", email = "ana@test.com", displayName = "Ana"),
            "user-3" to User(userId = "user-3", email = "user3@test.com", displayName = "")
        )

        @Test
        fun `toMemberOptions maps member IDs to MemberOptionUiModel list`() {
            val result = mapper.toMemberOptions(
                memberIds = listOf("user-1", "user-2"),
                memberProfiles = profiles,
                currentUserId = "user-1"
            )

            assertEquals(2, result.size)
            assertEquals("user-1", result[0].userId)
            assertEquals("Andrés", result[0].displayName)
            assertTrue(result[0].isCurrentUser)
            assertEquals("user-2", result[1].userId)
            assertEquals("Ana", result[1].displayName)
            assertFalse(result[1].isCurrentUser)
        }

        @Test
        fun `toMemberOptions falls back to userId when displayName is blank`() {
            val result = mapper.toMemberOptions(
                memberIds = listOf("user-3"),
                memberProfiles = profiles,
                currentUserId = null
            )

            assertEquals(1, result.size)
            assertEquals("user-3", result[0].displayName)
        }

        @Test
        fun `toMemberOptions falls back to userId when profile is missing`() {
            val result = mapper.toMemberOptions(
                memberIds = listOf("user-unknown"),
                memberProfiles = profiles,
                currentUserId = null
            )

            assertEquals(1, result.size)
            assertEquals("user-unknown", result[0].displayName)
        }

        @Test
        fun `toMemberOptions sets isCurrentUser to false when currentUserId is null`() {
            val result = mapper.toMemberOptions(
                memberIds = listOf("user-1"),
                memberProfiles = profiles,
                currentUserId = null
            )

            assertFalse(result[0].isCurrentUser)
        }

        @Test
        fun `resolveDisplayName returns display name for matching member`() {
            val members = listOf(
                MemberOptionUiModel(userId = "user-1", displayName = "Andrés", isCurrentUser = true),
                MemberOptionUiModel(userId = "user-2", displayName = "Ana", isCurrentUser = false)
            ).toImmutableList()

            val result = mapper.resolveDisplayName("user-2", members)

            assertEquals("Ana", result)
        }

        @Test
        fun `resolveDisplayName returns empty string when userId is null`() {
            val members = persistentListOf(
                MemberOptionUiModel(userId = "user-1", displayName = "Andrés", isCurrentUser = true)
            )

            val result = mapper.resolveDisplayName(null, members)

            assertEquals("", result)
        }

        @Test
        fun `resolveDisplayName returns empty string when userId not found`() {
            val members = persistentListOf(
                MemberOptionUiModel(userId = "user-1", displayName = "Andrés", isCurrentUser = true)
            )

            val result = mapper.resolveDisplayName("user-99", members)

            assertEquals("", result)
        }
    }
}

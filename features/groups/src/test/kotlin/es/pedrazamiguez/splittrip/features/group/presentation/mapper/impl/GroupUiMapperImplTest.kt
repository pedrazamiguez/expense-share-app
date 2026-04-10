package es.pedrazamiguez.splittrip.features.group.presentation.mapper.impl

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.Currency
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.features.group.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GroupUiMapperImplTest {

    private lateinit var localeProvider: LocaleProvider
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var mapper: GroupUiMapperImpl

    private val testLocale = Locale.US

    @BeforeEach
    fun setUp() {
        localeProvider = mockk {
            every { getCurrentLocale() } returns testLocale
        }
        resourceProvider = mockk()
        mapper = GroupUiMapperImpl(localeProvider, resourceProvider)
    }

    @Nested
    inner class MembersCountMapping {

        @Test
        fun `maps group with zero members correctly`() {
            // Given
            val group = createGroup(members = emptyList())
            every {
                resourceProvider.getQuantityString(R.plurals.group_members_count, 0, 0)
            } returns "0 travelers"

            // When
            val result = mapper.toGroupUiModel(group)

            // Then
            assertEquals("0 travelers", result.membersCountText)
            verify { resourceProvider.getQuantityString(R.plurals.group_members_count, 0, 0) }
        }

        @Test
        fun `maps group with one member using singular form`() {
            // Given
            val group = createGroup(members = listOf("user-1"))
            every {
                resourceProvider.getQuantityString(R.plurals.group_members_count, 1, 1)
            } returns "1 traveler"

            // When
            val result = mapper.toGroupUiModel(group)

            // Then
            assertEquals("1 traveler", result.membersCountText)
            verify { resourceProvider.getQuantityString(R.plurals.group_members_count, 1, 1) }
        }

        @Test
        fun `maps group with multiple members using plural form`() {
            // Given
            val members = listOf("user-1", "user-2", "user-3")
            val group = createGroup(members = members)
            every {
                resourceProvider.getQuantityString(R.plurals.group_members_count, 3, 3)
            } returns "3 travelers"

            // When
            val result = mapper.toGroupUiModel(group)

            // Then
            assertEquals("3 travelers", result.membersCountText)
            verify { resourceProvider.getQuantityString(R.plurals.group_members_count, 3, 3) }
        }

        @Test
        fun `maps group with many members correctly`() {
            // Given
            val members = (1..25).map { "user-$it" }
            val group = createGroup(members = members)
            every {
                resourceProvider.getQuantityString(R.plurals.group_members_count, 25, 25)
            } returns "25 travelers"

            // When
            val result = mapper.toGroupUiModel(group)

            // Then
            assertEquals("25 travelers", result.membersCountText)
            verify { resourceProvider.getQuantityString(R.plurals.group_members_count, 25, 25) }
        }
    }

    @Nested
    inner class MembersCountWithSpanishLocale {

        @BeforeEach
        fun setUpSpanishLocale() {
            every { localeProvider.getCurrentLocale() } returns Locale.forLanguageTag("es-ES")
        }

        @Test
        fun `maps group with one member using Spanish singular`() {
            // Given
            val group = createGroup(members = listOf("user-1"))
            every {
                resourceProvider.getQuantityString(R.plurals.group_members_count, 1, 1)
            } returns "1 viajero"

            // When
            val result = mapper.toGroupUiModel(group)

            // Then
            assertEquals("1 viajero", result.membersCountText)
        }

        @Test
        fun `maps group with multiple members using Spanish plural`() {
            // Given
            val group = createGroup(members = listOf("user-1", "user-2"))
            every {
                resourceProvider.getQuantityString(R.plurals.group_members_count, 2, 2)
            } returns "2 viajeros"

            // When
            val result = mapper.toGroupUiModel(group)

            // Then
            assertEquals("2 viajeros", result.membersCountText)
        }
    }

    @Nested
    inner class GroupListMapping {

        @Test
        fun `maps list of groups preserving member counts`() {
            // Given
            val groups = listOf(
                createGroup(id = "1", members = listOf("user-1")),
                createGroup(id = "2", members = listOf("user-1", "user-2", "user-3")),
                createGroup(id = "3", members = emptyList())
            )
            every {
                resourceProvider.getQuantityString(R.plurals.group_members_count, 1, 1)
            } returns "1 traveler"
            every {
                resourceProvider.getQuantityString(R.plurals.group_members_count, 3, 3)
            } returns "3 travelers"
            every {
                resourceProvider.getQuantityString(R.plurals.group_members_count, 0, 0)
            } returns "0 travelers"

            // When
            val result = mapper.toGroupUiModelList(groups)

            // Then
            assertEquals(3, result.size)
            assertEquals("1 traveler", result[0].membersCountText)
            assertEquals("3 travelers", result[1].membersCountText)
            assertEquals("0 travelers", result[2].membersCountText)
        }
    }

    @Nested
    inner class SyncStatusMapping {

        @Test
        fun `maps PENDING_SYNC status`() {
            every {
                resourceProvider.getQuantityString(R.plurals.group_members_count, 0, 0)
            } returns "0 travelers"
            val group = createGroup().copy(syncStatus = SyncStatus.PENDING_SYNC)
            val result = mapper.toGroupUiModel(group)
            assertEquals(SyncStatus.PENDING_SYNC, result.syncStatus)
        }

        @Test
        fun `maps SYNC_FAILED status`() {
            every {
                resourceProvider.getQuantityString(R.plurals.group_members_count, 0, 0)
            } returns "0 travelers"
            val group = createGroup().copy(syncStatus = SyncStatus.SYNC_FAILED)
            val result = mapper.toGroupUiModel(group)
            assertEquals(SyncStatus.SYNC_FAILED, result.syncStatus)
        }

        @Test
        fun `default maps to SYNCED`() {
            every {
                resourceProvider.getQuantityString(R.plurals.group_members_count, 0, 0)
            } returns "0 travelers"
            val group = createGroup()
            val result = mapper.toGroupUiModel(group)
            assertEquals(SyncStatus.SYNCED, result.syncStatus)
        }
    }

    @Nested
    inner class CurrencyMapping {

        @Test
        fun `maps known currency with localized name from resourceProvider`() {
            // Given
            val currency = createCurrency(code = "EUR", defaultName = "Euro")
            every { resourceProvider.getString(any()) } returns "Euro"

            // When
            val result = mapper.toCurrencyUiModel(currency)

            // Then
            assertEquals("EUR", result.code)
            assertEquals("Euro", result.defaultName)
            assertEquals("Euro", result.localizedName)
        }

        @Test
        fun `maps unknown currency falling back to defaultName`() {
            // Given
            val currency = createCurrency(code = "THB", defaultName = "Thai Baht")

            // When
            val result = mapper.toCurrencyUiModel(currency)

            // Then
            assertEquals("THB", result.code)
            assertEquals("Thai Baht", result.defaultName)
            assertEquals("Thai Baht", result.localizedName)
        }

        @Test
        fun `maps currency list preserving localized names`() {
            // Given
            val currencies = listOf(
                createCurrency(code = "EUR", defaultName = "Euro"),
                createCurrency(code = "THB", defaultName = "Thai Baht")
            )
            every { resourceProvider.getString(any()) } returns "Euro"

            // When
            val result = mapper.toCurrencyUiModels(currencies)

            // Then
            assertEquals(2, result.size)
            assertEquals("Euro", result[0].localizedName)
            assertEquals("Thai Baht", result[1].localizedName)
        }
    }

    @Nested
    inner class CurrencyMappingWithSpanishLocale {

        @BeforeEach
        fun setUpSpanishLocale() {
            every { localeProvider.getCurrentLocale() } returns Locale.forLanguageTag("es-ES")
        }

        @Test
        fun `maps GBP with Spanish localized name`() {
            // Given
            val currency = createCurrency(code = "GBP", defaultName = "British Pound Sterling")
            every { resourceProvider.getString(any()) } returns "Libra esterlina"

            // When
            val result = mapper.toCurrencyUiModel(currency)

            // Then
            assertEquals("GBP", result.code)
            assertEquals("British Pound Sterling", result.defaultName)
            assertEquals("Libra esterlina", result.localizedName)
        }

        @Test
        fun `maps USD with Spanish localized name`() {
            // Given
            val currency = createCurrency(code = "USD", defaultName = "United States Dollar")
            every { resourceProvider.getString(any()) } returns "Dólar estadounidense"

            // When
            val result = mapper.toCurrencyUiModel(currency)

            // Then
            assertEquals("USD", result.code)
            assertEquals("United States Dollar", result.defaultName)
            assertEquals("Dólar estadounidense", result.localizedName)
        }
    }

    private fun createCurrency(
        code: String = "EUR",
        symbol: String = "€",
        defaultName: String = "Euro",
        decimalDigits: Int = 2
    ) = Currency(
        code = code,
        symbol = symbol,
        defaultName = defaultName,
        decimalDigits = decimalDigits
    )

    private fun createGroup(
        id: String = "test-id",
        name: String = "Test Group",
        description: String = "Test Description",
        currency: String = "EUR",
        members: List<String> = emptyList(),
        createdAt: LocalDateTime? = LocalDateTime.of(2024, 1, 15, 12, 0)
    ) = Group(
        id = id,
        name = name,
        description = description,
        currency = currency,
        members = members,
        createdAt = createdAt
    )
}

package es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.impl

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.features.group.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Locale

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
            every { localeProvider.getCurrentLocale() } returns Locale("es", "ES")
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

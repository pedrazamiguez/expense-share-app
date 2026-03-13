package es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.impl

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.features.group.R
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale

class SubunitUiMapperImplTest {

    private lateinit var localeProvider: LocaleProvider
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var mapper: SubunitUiMapperImpl

    private val testLocale = Locale.US

    @BeforeEach
    fun setUp() {
        localeProvider = mockk {
            every { getCurrentLocale() } returns testLocale
        }
        resourceProvider = mockk(relaxed = true)
        mapper = SubunitUiMapperImpl(localeProvider, resourceProvider)
    }

    private fun createUser(id: String, displayName: String? = null, email: String = "$id@test.com") =
        User(userId = id, email = email, displayName = displayName)

    private fun createSubunit(
        id: String = "sub-1",
        name: String = "Couple",
        memberIds: List<String> = listOf("user-1", "user-2"),
        memberShares: Map<String, Double> = mapOf("user-1" to 0.5, "user-2" to 0.5)
    ) = Subunit(
        id = id,
        groupId = "group-1",
        name = name,
        memberIds = memberIds,
        memberShares = memberShares
    )

    @Nested
    inner class ToSubunitUiModel {

        @Test
        fun `maps subunit with display names`() {
            val profiles = mapOf(
                "user-1" to createUser("user-1", displayName = "Alice"),
                "user-2" to createUser("user-2", displayName = "Bob")
            )
            val subunit = createSubunit()
            every {
                resourceProvider.getQuantityString(R.plurals.subunit_member_count, 2, 2)
            } returns "2 members"

            val result = mapper.toSubunitUiModel(subunit, profiles)

            assertEquals("sub-1", result.id)
            assertEquals("Couple", result.name)
            assertEquals(listOf("Alice", "Bob"), result.memberNames)
            assertEquals("2 members", result.memberCount)
        }

        @Test
        fun `falls back to email when display name is null`() {
            val profiles = mapOf(
                "user-1" to createUser("user-1", displayName = null, email = "alice@test.com"),
                "user-2" to createUser("user-2", displayName = "Bob")
            )
            val subunit = createSubunit()
            every {
                resourceProvider.getQuantityString(R.plurals.subunit_member_count, 2, 2)
            } returns "2 members"

            val result = mapper.toSubunitUiModel(subunit, profiles)

            assertEquals("alice@test.com", result.memberNames[0])
            assertEquals("Bob", result.memberNames[1])
        }

        @Test
        fun `falls back to userId when profile is missing`() {
            val profiles = mapOf(
                "user-1" to createUser("user-1", displayName = "Alice")
            )
            val subunit = createSubunit()
            every {
                resourceProvider.getQuantityString(R.plurals.subunit_member_count, 2, 2)
            } returns "2 members"

            val result = mapper.toSubunitUiModel(subunit, profiles)

            assertEquals("Alice", result.memberNames[0])
            assertEquals("user-2", result.memberNames[1])
        }

        @Test
        fun `formats shares summary as percentages`() {
            val profiles = mapOf(
                "user-1" to createUser("user-1", displayName = "Alice"),
                "user-2" to createUser("user-2", displayName = "Bob")
            )
            val subunit = createSubunit(
                memberShares = mapOf("user-1" to 0.6, "user-2" to 0.4)
            )
            every {
                resourceProvider.getQuantityString(R.plurals.subunit_member_count, 2, 2)
            } returns "2 members"
            every {
                resourceProvider.getString(R.string.subunit_shares_summary_separator)
            } returns " / "

            val result = mapper.toSubunitUiModel(subunit, profiles)

            assertEquals("60% / 40%", result.sharesSummary)
        }

        @Test
        fun `returns empty shares summary when no shares defined`() {
            val profiles = mapOf(
                "user-1" to createUser("user-1", displayName = "Alice")
            )
            val subunit = createSubunit(
                memberIds = listOf("user-1"),
                memberShares = emptyMap()
            )
            every {
                resourceProvider.getQuantityString(R.plurals.subunit_member_count, 1, 1)
            } returns "1 member"

            val result = mapper.toSubunitUiModel(subunit, profiles)

            assertEquals("", result.sharesSummary)
        }
    }

    @Nested
    inner class ToSubunitUiModelList {

        @Test
        fun `maps list of subunits`() {
            val profiles = mapOf(
                "user-1" to createUser("user-1", displayName = "Alice"),
                "user-2" to createUser("user-2", displayName = "Bob"),
                "user-3" to createUser("user-3", displayName = "Charlie")
            )
            val subunits = listOf(
                createSubunit(id = "sub-1", name = "Couple", memberIds = listOf("user-1", "user-2")),
                createSubunit(id = "sub-2", name = "Solo", memberIds = listOf("user-3"))
            )
            every {
                resourceProvider.getQuantityString(R.plurals.subunit_member_count, any(), any())
            } returns "members"

            val result = mapper.toSubunitUiModelList(subunits, profiles)

            assertEquals(2, result.size)
            assertEquals("Couple", result[0].name)
            assertEquals("Solo", result[1].name)
        }

        @Test
        fun `returns empty list for empty input`() {
            val result = mapper.toSubunitUiModelList(emptyList(), emptyMap())
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class ToMemberUiModelList {

        @Test
        fun `marks members not in any subunit as available`() {
            val profiles = mapOf(
                "user-1" to createUser("user-1", displayName = "Alice"),
                "user-2" to createUser("user-2", displayName = "Bob")
            )

            val result = mapper.toMemberUiModelList(
                memberIds = listOf("user-1", "user-2"),
                memberProfiles = profiles,
                subunits = emptyList()
            )

            assertEquals(2, result.size)
            assertFalse(result[0].isAssigned)
            assertFalse(result[1].isAssigned)
        }

        @Test
        fun `marks members in existing subunits as assigned`() {
            val profiles = mapOf(
                "user-1" to createUser("user-1", displayName = "Alice"),
                "user-2" to createUser("user-2", displayName = "Bob"),
                "user-3" to createUser("user-3", displayName = "Charlie")
            )
            val existingSubunits = listOf(
                createSubunit(id = "sub-1", name = "Couple", memberIds = listOf("user-1", "user-2"))
            )

            val result = mapper.toMemberUiModelList(
                memberIds = listOf("user-1", "user-2", "user-3"),
                memberProfiles = profiles,
                subunits = existingSubunits
            )

            assertTrue(result[0].isAssigned)
            assertEquals("Couple", result[0].assignedSubunitName)
            assertTrue(result[1].isAssigned)
            assertEquals("Couple", result[1].assignedSubunitName)
            assertFalse(result[2].isAssigned)
        }

        @Test
        fun `excludes edited subunit from assigned check`() {
            val profiles = mapOf(
                "user-1" to createUser("user-1", displayName = "Alice"),
                "user-2" to createUser("user-2", displayName = "Bob")
            )
            val existingSubunits = listOf(
                createSubunit(id = "sub-1", name = "Couple", memberIds = listOf("user-1", "user-2"))
            )

            val result = mapper.toMemberUiModelList(
                memberIds = listOf("user-1", "user-2"),
                memberProfiles = profiles,
                subunits = existingSubunits,
                excludeSubunitId = "sub-1"
            )

            assertFalse(result[0].isAssigned)
            assertFalse(result[1].isAssigned)
        }

        @Test
        fun `resolves display name with fallback to email`() {
            val profiles = mapOf(
                "user-1" to createUser("user-1", displayName = null, email = "alice@test.com")
            )

            val result = mapper.toMemberUiModelList(
                memberIds = listOf("user-1"),
                memberProfiles = profiles,
                subunits = emptyList()
            )

            assertEquals("alice@test.com", result[0].displayName)
        }

        @Test
        fun `resolves userId fallback when no profile exists`() {
            val result = mapper.toMemberUiModelList(
                memberIds = listOf("user-unknown"),
                memberProfiles = emptyMap(),
                subunits = emptyList()
            )

            assertEquals("user-unknown", result[0].displayName)
        }
    }
}


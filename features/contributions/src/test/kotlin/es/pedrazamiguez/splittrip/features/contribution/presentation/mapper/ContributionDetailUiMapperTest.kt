package es.pedrazamiguez.splittrip.features.contribution.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.splittrip.core.designsystem.presentation.mapper.UserUiMapper
import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.MemberDisplay
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.contribution.R
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContributionDetailUiMapperTest {

    private lateinit var formattingHelper: FormattingHelper
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var localeProvider: LocaleProvider
    private lateinit var userUiMapper: UserUiMapper
    private lateinit var mapper: ContributionDetailUiMapper

    private val testMembers = listOf("user-1", "user-2")
    private val testMemberProfiles = mapOf(
        "user-1" to User(userId = "user-1", email = "user1@test.com", displayName = "Andrés"),
        "user-2" to User(userId = "user-2", email = "user2@test.com", displayName = "Ana")
    )
    private val testSubunits = mapOf(
        "subunit-1" to Subunit(
            id = "subunit-1",
            groupId = "group-1",
            name = "Couple A",
            memberIds = listOf("user-1", "user-2")
        )
    )

    @BeforeEach
    fun setUp() {
        localeProvider = mockk()
        every { localeProvider.getCurrentLocale() } returns Locale.US
        formattingHelper = FormattingHelper(localeProvider)
        resourceProvider = mockk(relaxed = true)
        userUiMapper = UserUiMapper(resourceProvider)

        every {
            resourceProvider.getString(R.string.contribution_member_picker_you_label)
        } returns "You"
        every {
            resourceProvider.getString(R.string.contribution_detail_contributed_by_you)
        } returns "Contributed by you"
        every {
            resourceProvider.getString(R.string.contribution_detail_contributed_by, any())
        } answers {
            val arg = (args[1] as Array<*>)[0]
            "Contributed by $arg"
        }
        every {
            resourceProvider.getString(R.string.contribution_detail_created_by_you)
        } returns "Added by you"
        every {
            resourceProvider.getString(R.string.contribution_detail_created_by, any())
        } answers {
            val arg = (args[1] as Array<*>)[0]
            "Added by $arg"
        }
        every {
            resourceProvider.getString(R.string.contribution_detail_scope_personal)
        } returns "Personal"
        every {
            resourceProvider.getString(R.string.contribution_detail_scope_personal_desc)
        } returns "Personal desc"
        every {
            resourceProvider.getString(R.string.contribution_detail_scope_group)
        } returns "Group"
        every {
            resourceProvider.getString(R.string.contribution_detail_scope_group_desc)
        } returns "Group desc"
        every {
            resourceProvider.getString(R.string.contribution_detail_scope_subunit_desc, any())
        } answers {
            val arg = (args[1] as Array<*>)[0]
            "Subunit desc for $arg"
        }

        mapper = ContributionDetailUiMapper(
            formattingHelper = formattingHelper,
            resourceProvider = resourceProvider,
            userUiMapper = userUiMapper
        )
    }

    @Test
    fun `map returns formatted model for standard group contribution`() {
        val contribution = Contribution(
            id = "contrib-1",
            groupId = "group-1",
            userId = "user-1",
            createdBy = "user-1",
            contributionScope = PayerType.GROUP,
            amount = 10000L,
            currency = "EUR",
            contributionDate = LocalDateTime.of(2026, 8, 15, 10, 0),
            createdAt = LocalDateTime.of(2026, 8, 15, 10, 0),
            syncStatus = SyncStatus.SYNCED
        )

        val result = mapper.map(
            contribution = contribution,
            groupCurrency = "EUR",
            memberProfiles = testMemberProfiles,
            subunitsMap = testSubunits,
            groupMemberIds = testMembers,
            currentUserId = "user-1"
        )

        assertEquals("contrib-1", result.id)
        assertEquals("group-1", result.groupId)
        assertTrue(result.isCurrentUser)
        assertEquals("You", result.contributorName)
        assertEquals("Contributed by you", result.contributedByText)
        assertEquals("Added by you", result.createdByText)
        assertEquals("Group", result.scopeLabel)
        assertEquals("Group desc", result.scopeDescription)
        assertEquals(PayerType.GROUP, result.scopeType)
        assertFalse(result.isForeignCurrency)
        assertFalse(result.isLinkedContribution)
        assertFalse(result.isSettlementContribution)
        assertEquals(SyncStatus.SYNCED, result.syncStatus)
    }

    @Test
    fun `map returns formatted model for personal contribution`() {
        val contribution = Contribution(
            id = "contrib-2",
            groupId = "group-1",
            userId = "user-2",
            createdBy = "user-2",
            contributionScope = PayerType.USER,
            amount = 5000L,
            currency = "EUR",
            createdAt = LocalDateTime.of(2026, 8, 15, 10, 0)
        )

        val result = mapper.map(
            contribution = contribution,
            groupCurrency = "EUR",
            memberProfiles = testMemberProfiles,
            subunitsMap = testSubunits,
            groupMemberIds = testMembers,
            currentUserId = "user-1"
        )

        assertFalse(result.isCurrentUser)
        assertEquals("Ana", result.contributorName)
        assertEquals("Contributed by Ana", result.contributedByText)
        assertEquals("Added by Ana", result.createdByText)
        assertEquals("Personal", result.scopeLabel)
        assertEquals(PayerType.USER, result.scopeType)
    }

    @Test
    fun `map returns formatted model with subunit name for subunit contribution`() {
        val contribution = Contribution(
            id = "contrib-3",
            groupId = "group-1",
            userId = "user-1",
            createdBy = "user-1",
            contributionScope = PayerType.SUBUNIT,
            subunitId = "subunit-1",
            amount = 7500L,
            currency = "EUR"
        )

        val result = mapper.map(
            contribution = contribution,
            groupCurrency = "EUR",
            memberProfiles = testMemberProfiles,
            subunitsMap = testSubunits,
            groupMemberIds = testMembers,
            currentUserId = "user-1"
        )

        assertEquals("Couple A", result.scopeLabel)
        assertEquals("Subunit desc for Couple A", result.scopeDescription)
        assertEquals("Couple A", result.subunitName)
        assertEquals(PayerType.SUBUNIT, result.scopeType)
    }

    @Test
    fun `map formats foreign currency with exchange rate and equivalent base amount`() {
        val contribution = Contribution(
            id = "contrib-4",
            groupId = "group-1",
            userId = "user-1",
            createdBy = "user-1",
            amount = 10000L, // $100.00
            currency = "USD",
            equivalentBaseAmount = 9200L, // 92.00 €
            exchangeRate = BigDecimal("0.92")
        )

        val result = mapper.map(
            contribution = contribution,
            groupCurrency = "EUR",
            memberProfiles = testMemberProfiles,
            subunitsMap = testSubunits,
            groupMemberIds = testMembers,
            currentUserId = "user-1"
        )

        assertTrue(result.isForeignCurrency)
        assertEquals("USD", result.sourceCurrency)
        assertTrue(result.formattedEquivalentAmount.isNotBlank())
        assertEquals("0.92", result.formattedExchangeRate)
    }

    @Test
    fun `map sets isLinkedContribution flag`() {
        val contribution = Contribution(
            id = "contrib-5",
            groupId = "group-1",
            userId = "user-1",
            createdBy = "user-1",
            amount = 3000L,
            currency = "EUR",
            linkedExpenseId = "expense-123"
        )

        val result = mapper.map(
            contribution = contribution,
            groupCurrency = "EUR",
            memberProfiles = testMemberProfiles,
            subunitsMap = testSubunits,
            groupMemberIds = testMembers,
            currentUserId = "user-1"
        )

        assertTrue(result.isLinkedContribution)
        assertEquals("expense-123", result.linkedExpenseId)
        assertFalse(result.isSettlementContribution)
    }

    @Test
    fun `map sets isSettlementContribution flag`() {
        val contribution = Contribution(
            id = "contrib-6",
            groupId = "group-1",
            userId = "user-1",
            createdBy = "user-1",
            amount = 4000L,
            currency = "EUR",
            linkedSettlementId = "settlement-456"
        )

        val result = mapper.map(
            contribution = contribution,
            groupCurrency = "EUR",
            memberProfiles = testMemberProfiles,
            subunitsMap = testSubunits,
            groupMemberIds = testMembers,
            currentUserId = "user-1"
        )

        assertTrue(result.isSettlementContribution)
        assertFalse(result.isLinkedContribution)
        assertNull(result.linkedExpenseId)
    }

    @Test
    fun `map resolves former member display when member is no longer in group`() {
        val contribution = Contribution(
            id = "contrib-7",
            groupId = "group-1",
            userId = "user-former",
            createdBy = "user-former",
            amount = 2000L,
            currency = "EUR"
        )
        val memberProfiles = testMemberProfiles + mapOf(
            "user-former" to User(
                userId = "user-former",
                email = "former@test.com",
                displayName = "Former Member"
            )
        )

        val result = mapper.map(
            contribution = contribution,
            groupCurrency = "EUR",
            memberProfiles = memberProfiles,
            subunitsMap = testSubunits,
            groupMemberIds = testMembers, // user-former not in groupMemberIds
            currentUserId = "user-1"
        )

        assertTrue(result.memberDisplay is MemberDisplay.Former)
        assertEquals("Former Member", result.memberDisplay.displayName)
    }

    @Test
    fun `map resolves impersonation when createdBy != userId`() {
        val contribution = Contribution(
            id = "contrib-8",
            groupId = "group-1",
            userId = "user-2",
            createdBy = "user-1",
            amount = 1500L,
            currency = "EUR"
        )

        val result = mapper.map(
            contribution = contribution,
            groupCurrency = "EUR",
            memberProfiles = testMemberProfiles,
            subunitsMap = testSubunits,
            groupMemberIds = testMembers,
            currentUserId = "user-1"
        )

        assertEquals("Contributed by Ana", result.contributedByText)
        assertEquals("Added by you", result.createdByText)
    }
}

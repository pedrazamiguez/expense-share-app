package es.pedrazamiguez.splittrip.features.group.presentation.mapper.impl

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.R as DesignSystemR
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.model.SettlementRowStatusStyle
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GroupSettlementOverviewUiMapperImplTest {

    private lateinit var localeProvider: LocaleProvider
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var mapper: GroupSettlementOverviewUiMapperImpl

    private val testLocale = Locale.US
    private val currentUserId = "user-1"

    @BeforeEach
    fun setUp() {
        localeProvider = mockk {
            every { getCurrentLocale() } returns testLocale
        }
        resourceProvider = mockk(relaxed = true)
        mapper = GroupSettlementOverviewUiMapperImpl(localeProvider, resourceProvider)
    }

    @Nested
    inner class EmptySettlements {

        @Test
        fun `empty settlements list produces empty groups and areAllSettlementsResolved false`() {
            val result = mapper.toUiState(emptyList(), emptyMap(), currentUserId)

            assertFalse(result.isLoading)
            assertFalse(result.areAllSettlementsResolved)
            assertTrue(result.pendingSettlements.isEmpty())
            assertTrue(result.disputedSettlements.isEmpty())
            assertTrue(result.resolvedSettlements.isEmpty())
        }
    }

    @Nested
    inner class SettlementGrouping {

        @Test
        fun `suggested settlements are grouped as pending`() {
            val settlements = listOf(
                createRecord("s-1", SettlementStatus.SUGGESTED)
            )
            val result = mapper.toUiState(settlements, emptyMap(), currentUserId)

            assertEquals(1, result.pendingSettlements.size)
            assertTrue(result.disputedSettlements.isEmpty())
            assertTrue(result.resolvedSettlements.isEmpty())
            assertFalse(result.areAllSettlementsResolved)
        }

        @Test
        fun `confirmed_by_payer settlements are grouped as pending`() {
            val settlements = listOf(
                createRecord("s-1", SettlementStatus.CONFIRMED_BY_PAYER)
            )
            val result = mapper.toUiState(settlements, emptyMap(), currentUserId)

            assertEquals(1, result.pendingSettlements.size)
            assertTrue(result.disputedSettlements.isEmpty())
            assertTrue(result.resolvedSettlements.isEmpty())
            assertFalse(result.areAllSettlementsResolved)
        }

        @Test
        fun `disputed settlements are grouped separately`() {
            val settlements = listOf(
                createRecord("s-1", SettlementStatus.DISPUTED)
            )
            val result = mapper.toUiState(settlements, emptyMap(), currentUserId)

            assertTrue(result.pendingSettlements.isEmpty())
            assertEquals(1, result.disputedSettlements.size)
            assertTrue(result.resolvedSettlements.isEmpty())
            assertFalse(result.areAllSettlementsResolved)
        }

        @Test
        fun `resolved settlements are grouped separately`() {
            val settlements = listOf(
                createRecord("s-1", SettlementStatus.RESOLVED)
            )
            val result = mapper.toUiState(settlements, emptyMap(), currentUserId)

            assertTrue(result.pendingSettlements.isEmpty())
            assertTrue(result.disputedSettlements.isEmpty())
            assertEquals(1, result.resolvedSettlements.size)
            assertTrue(result.areAllSettlementsResolved)
        }

        @Test
        fun `all resolved returns areAllSettlementsResolved true`() {
            val settlements = listOf(
                createRecord("s-1", SettlementStatus.RESOLVED),
                createRecord("s-2", SettlementStatus.RESOLVED)
            )
            val result = mapper.toUiState(settlements, emptyMap(), currentUserId)

            assertTrue(result.areAllSettlementsResolved)
        }

        @Test
        fun `mixed status returns areAllSettlementsResolved false`() {
            val settlements = listOf(
                createRecord("s-1", SettlementStatus.SUGGESTED),
                createRecord("s-2", SettlementStatus.RESOLVED)
            )
            val result = mapper.toUiState(settlements, emptyMap(), currentUserId)

            assertFalse(result.areAllSettlementsResolved)
        }
    }

    @Nested
    inner class SettlementRowMapping {

        @Test
        fun `maps debtor and creditor names from member profiles`() {
            val settlements = listOf(
                createRecord("s-1", SettlementStatus.SUGGESTED, fromUser = "user-1", toUser = "user-2")
            )
            val profiles = mapOf(
                "user-1" to User(userId = "user-1", email = "a@b.com", displayName = "Alice"),
                "user-2" to User(userId = "user-2", email = "c@d.com", displayName = "Bob")
            )
            val result = mapper.toUiState(settlements, profiles, "user-3")

            val row = result.pendingSettlements.first()
            assertEquals("Alice", row.debtorName)
            assertEquals("Bob", row.creditorName)
        }

        @Test
        fun `uses fallback for unknown member profiles`() {
            every {
                resourceProvider.getString(DesignSystemR.string.user_pending_fallback)
            } returns "Pending member"

            val settlements = listOf(
                createRecord("s-1", SettlementStatus.SUGGESTED, fromUser = "unknown-1", toUser = "unknown-2")
            )
            val result = mapper.toUiState(settlements, emptyMap(), currentUserId)

            val row = result.pendingSettlements.first()
            assertEquals("Pending member", row.debtorName)
            assertEquals("Pending member", row.creditorName)
        }

        @Test
        fun `marks current user as debtor when fromUserId matches`() {
            val settlements = listOf(
                createRecord("s-1", SettlementStatus.SUGGESTED, fromUser = "user-1", toUser = "user-2")
            )
            val result = mapper.toUiState(settlements, emptyMap(), "user-1")

            val row = result.pendingSettlements.first()
            assertTrue(row.isCurrentUserDebtor)
            assertFalse(row.isCurrentUserCreditor)
        }

        @Test
        fun `marks current user as creditor when toUserId matches`() {
            val settlements = listOf(
                createRecord("s-1", SettlementStatus.SUGGESTED, fromUser = "user-2", toUser = "user-1")
            )
            val result = mapper.toUiState(settlements, emptyMap(), "user-1")

            val row = result.pendingSettlements.first()
            assertFalse(row.isCurrentUserDebtor)
            assertTrue(row.isCurrentUserCreditor)
        }

        @Test
        fun `payer can confirm in SUGGESTED state`() {
            val settlements = listOf(
                createRecord("s-1", SettlementStatus.SUGGESTED, fromUser = "user-1", toUser = "user-2")
            )
            val result = mapper.toUiState(settlements, emptyMap(), "user-1")

            val row = result.pendingSettlements.first()
            assertTrue(row.canCurrentUserConfirm)
            assertTrue(row.canCurrentUserDispute)
        }

        @Test
        fun `payee can confirm in CONFIRMED_BY_PAYER state`() {
            val settlements = listOf(
                createRecord("s-1", SettlementStatus.CONFIRMED_BY_PAYER, fromUser = "user-1", toUser = "user-2")
            )
            val result = mapper.toUiState(settlements, emptyMap(), "user-2")

            val row = result.pendingSettlements.first()
            assertTrue(row.canCurrentUserConfirm)
            assertTrue(row.canCurrentUserDispute)
        }

        @Test
        fun `non-involved user cannot confirm or dispute`() {
            val settlements = listOf(
                createRecord("s-1", SettlementStatus.SUGGESTED, fromUser = "user-1", toUser = "user-2")
            )
            val result = mapper.toUiState(settlements, emptyMap(), "user-3")

            val row = result.pendingSettlements.first()
            assertFalse(row.canCurrentUserConfirm)
            assertFalse(row.canCurrentUserDispute)
        }

        @Test
        fun `resolved settlement has no confirm or dispute actions`() {
            val settlements = listOf(
                createRecord("s-1", SettlementStatus.RESOLVED, fromUser = "user-1", toUser = "user-2")
            )
            val result = mapper.toUiState(settlements, emptyMap(), "user-1")

            val row = result.resolvedSettlements.first()
            assertFalse(row.canCurrentUserConfirm)
            assertFalse(row.canCurrentUserDispute)
        }

        @Test
        fun `disputed settlement has no confirm action but shows dispute reason`() {
            val settlements = listOf(
                createRecord(
                    "s-1",
                    SettlementStatus.DISPUTED,
                    fromUser = "user-1",
                    toUser = "user-2",
                    disputedBy = "user-1",
                    disputeReason = "Amount incorrect"
                )
            )
            val result = mapper.toUiState(settlements, emptyMap(), "user-1")

            val row = result.disputedSettlements.first()
            assertFalse(row.canCurrentUserConfirm)
            assertFalse(row.canCurrentUserDispute)
            assertEquals("Amount incorrect", row.disputeReason)
            assertTrue(row.disputedByCurrentUser)
        }

        @Test
        fun `status labels are resolved from string resources`() {
            every { resourceProvider.getString(R.string.settlement_overview_status_suggested) } returns "Pending"
            every { resourceProvider.getString(R.string.settlement_overview_status_confirmed_by_payer) } returns
                "Awaiting"
            every { resourceProvider.getString(R.string.settlement_overview_status_disputed) } returns "Disputed"
            every { resourceProvider.getString(R.string.settlement_overview_status_resolved) } returns "Resolved"

            val suggested = createRecord("s-1", SettlementStatus.SUGGESTED)
            val confirmed = createRecord("s-2", SettlementStatus.CONFIRMED_BY_PAYER)
            val disputed = createRecord("s-3", SettlementStatus.DISPUTED)
            val resolved = createRecord("s-4", SettlementStatus.RESOLVED)

            val result = mapper.toUiState(listOf(suggested, confirmed, disputed, resolved), emptyMap(), currentUserId)

            assertEquals("Pending", result.pendingSettlements[0].statusLabel)
            assertEquals("Awaiting", result.pendingSettlements[1].statusLabel)
            assertEquals("Disputed", result.disputedSettlements[0].statusLabel)
            assertEquals("Resolved", result.resolvedSettlements[0].statusLabel)
        }

        @Test
        fun `status chip styles are mapped correctly`() {
            val suggested = createRecord("s-1", SettlementStatus.SUGGESTED)
            val confirmed = createRecord("s-2", SettlementStatus.CONFIRMED_BY_PAYER)
            val disputed = createRecord("s-3", SettlementStatus.DISPUTED)
            val resolved = createRecord("s-4", SettlementStatus.RESOLVED)

            val result = mapper.toUiState(listOf(suggested, confirmed, disputed, resolved), emptyMap(), currentUserId)

            assertEquals(SettlementRowStatusStyle.NEUTRAL, result.pendingSettlements[0].statusChipStyle)
            assertEquals(SettlementRowStatusStyle.WARNING, result.pendingSettlements[1].statusChipStyle)
            assertEquals(SettlementRowStatusStyle.ERROR, result.disputedSettlements[0].statusChipStyle)
            assertEquals(SettlementRowStatusStyle.SUCCESS, result.resolvedSettlements[0].statusChipStyle)
        }
    }

    private fun createRecord(
        id: String,
        status: SettlementStatus,
        fromUser: String = "user-1",
        toUser: String = "user-2",
        amount: Long = 10000L,
        disputedBy: String? = null,
        disputeReason: String? = null
    ): SettlementRecord = SettlementRecord(
        id = id,
        groupId = "group-1",
        settlement = Settlement(
            fromUserId = fromUser,
            toUserId = toUser,
            amount = amount,
            currency = "EUR",
            sourcePocket = SettlementPocketType.NET
        ),
        status = status,
        createdAt = LocalDateTime.now(),
        disputedBy = disputedBy,
        disputeReason = disputeReason
    )
}

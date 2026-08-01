package es.pedrazamiguez.splittrip.features.group.presentation.mapper.impl

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.R as DesignSystemR
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.splittrip.core.designsystem.presentation.mapper.UserUiMapper
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.domain.service.DebtSimplificationService
import es.pedrazamiguez.splittrip.features.group.R
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LeaveWizardUiMapperImplTest {

    private lateinit var localeProvider: LocaleProvider
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var formattingHelper: FormattingHelper
    private lateinit var userUiMapper: UserUiMapper
    private lateinit var debtSimplificationService: DebtSimplificationService
    private lateinit var mapper: LeaveWizardUiMapperImpl

    private val testLocale = Locale.US
    private val currentUserId = "user-1"

    @BeforeEach
    fun setUp() {
        localeProvider = mockk {
            every { getCurrentLocale() } returns testLocale
        }
        resourceProvider = mockk(relaxed = true)
        every { resourceProvider.getString(DesignSystemR.string.self_identification_nominative) } returns "You"
        every { resourceProvider.getString(DesignSystemR.string.user_pending_fallback) } returns "Pending member"
        formattingHelper = FormattingHelper(localeProvider)
        userUiMapper = UserUiMapper(resourceProvider)
        debtSimplificationService = mockk()
        mapper = LeaveWizardUiMapperImpl(formattingHelper, resourceProvider, userUiMapper, debtSimplificationService)
    }

    @Test
    fun `toBalanceSummaryUiModel formats pocket balance cash in hand and total balance correctly`() {
        val memberBalance = MemberBalance(
            userId = currentUserId,
            pocketBalance = 2500L,
            cashInHand = 1500L
        )
        every { debtSimplificationService.simplify(any()) } returns emptyList()

        val result = mapper.toBalanceSummaryUiModel(
            memberBalance = memberBalance,
            memberBalances = listOf(memberBalance),
            currentUserId = currentUserId,
            memberProfiles = emptyMap(),
            currency = "EUR"
        )

        assertEquals("€25.00", result.pocketBalanceFormatted)
        assertEquals("€15.00", result.cashInHandFormatted)
        assertEquals("€40.00", result.totalBalanceFormatted)
        assertTrue(result.perPersonNetPositions.isEmpty())
    }

    @Test
    fun `toBalanceSummaryUiModel computes perPersonNetPositions correctly`() {
        val memberBalance = MemberBalance(
            userId = currentUserId,
            pocketBalance = 2500L,
            cashInHand = 1500L
        )
        val otherBalances = listOf(
            memberBalance,
            MemberBalance(userId = "user-2", pocketBalance = -1000L),
            MemberBalance(userId = "user-3", pocketBalance = -1500L)
        )
        val settlements = listOf(
            Settlement(fromUserId = "user-2", toUserId = currentUserId, amount = 1000L),
            Settlement(fromUserId = currentUserId, toUserId = "user-3", amount = 1500L)
        )
        every { debtSimplificationService.simplify(otherBalances) } returns settlements
        every { resourceProvider.getString(DesignSystemR.string.balance_you) } returns "You"

        val profiles = mapOf(
            "user-1" to User(userId = "user-1", email = "a@b.com", displayName = "Alice"),
            "user-2" to User(userId = "user-2", email = "c@d.com", displayName = "Bob"),
            "user-3" to User(userId = "user-3", email = "e@f.com", displayName = "Charlie")
        )

        val result = mapper.toBalanceSummaryUiModel(
            memberBalance = memberBalance,
            memberBalances = otherBalances,
            currentUserId = currentUserId,
            memberProfiles = profiles,
            currency = "EUR"
        )

        assertEquals(2, result.perPersonNetPositions.size)

        val pos1 = result.perPersonNetPositions.find { it.memberName == "Bob" }
        assertEquals("€10.00", pos1?.amountFormatted)
        assertTrue(pos1?.isPositive == true)
        assertTrue(pos1?.isNegative == false)

        val pos2 = result.perPersonNetPositions.find { it.memberName == "Charlie" }
        assertEquals("€15.00", pos2?.amountFormatted)
        assertTrue(pos2?.isPositive == false)
        assertTrue(pos2?.isNegative == true)
    }

    @Test
    fun `toSettlementUiModels maps sender and receiver names formatted amounts and confirmation state`() {
        every { resourceProvider.getString(DesignSystemR.string.balance_you) } returns "You"
        every { resourceProvider.getString(DesignSystemR.string.settlement_pocket_type_net) } returns "Net"
        every { resourceProvider.getString(R.string.leave_wizard_settlement_you_owe, "Bob") } returns "You owe Bob"
        every { resourceProvider.getString(R.string.leave_wizard_settlement_action_required) } returns
            "Action required by you"

        val record = SettlementRecord(
            id = "s-1",
            groupId = "group-1",
            settlement = Settlement(
                fromUserId = "user-1",
                toUserId = "user-2",
                amount = 5000L,
                currency = "EUR",
                sourcePocket = SettlementPocketType.NET
            ),
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )

        val profiles = mapOf(
            "user-1" to User(userId = "user-1", email = "a@b.com", displayName = "Alice"),
            "user-2" to User(userId = "user-2", email = "c@d.com", displayName = "Bob")
        )

        val result = mapper.toSettlementUiModels(listOf(record), profiles, currentUserId)

        assertEquals(1, result.size)
        val uiModel = result.first()
        assertEquals("s-1", uiModel.settlementId)
        assertEquals("You", uiModel.debtorName)
        assertEquals("Bob", uiModel.creditorName)
        assertEquals("You owe Bob", uiModel.directionTitle)
        assertEquals("€50.00", uiModel.formattedAmount)
        assertEquals("Net", uiModel.pocketTypeLabel)
        assertTrue(uiModel.isCurrentUserDebtor)
        assertFalse(uiModel.isCurrentUserCreditor)
        assertTrue(uiModel.canCurrentUserConfirm)
        assertFalse(uiModel.isConfirmed)
    }

    @Test
    fun `toCashResolutionUiModel identifies deposit vs reimbursement based on cash held`() {
        val depositBalance = MemberBalance(userId = currentUserId, cashInHand = 3000L)
        val depositResult = mapper.toCashResolutionUiModel(depositBalance, "EUR")

        assertTrue(depositResult.requiresDeposit)
        assertFalse(depositResult.requiresReimbursement)
        assertEquals("€30.00", depositResult.formattedAmount)

        val reimburseBalance = MemberBalance(userId = currentUserId, cashInHand = -1500L)
        val reimburseResult = mapper.toCashResolutionUiModel(reimburseBalance, "EUR")

        assertFalse(reimburseResult.requiresDeposit)
        assertTrue(reimburseResult.requiresReimbursement)
        assertEquals("€15.00", reimburseResult.formattedAmount)
    }

    @Test
    fun `toSubunitImpactUiModel formats affected subunit list and redistribution message`() {
        every {
            resourceProvider.getString(R.string.leave_wizard_subunit_impact_none)
        } returns "No subunits affected"
        every {
            resourceProvider.getString(R.string.leave_wizard_subunit_impact_redistribute, "Couples")
        } returns "Subunit Couples redistributed"

        val emptyResult = mapper.toSubunitImpactUiModel(emptyList())
        assertFalse(emptyResult.hasSubunitImpact)
        assertEquals("No subunits affected", emptyResult.message)

        val subunit = Subunit(
            id = "sub-1",
            groupId = "group-1",
            name = "Couples",
            memberShares = mapOf(currentUserId to BigDecimal("1.0"))
        )
        val result = mapper.toSubunitImpactUiModel(listOf(subunit))

        assertTrue(result.hasSubunitImpact)
        assertEquals(listOf("Couples"), result.affectedSubunitNames)
        assertEquals("Subunit Couples redistributed", result.message)
    }
}

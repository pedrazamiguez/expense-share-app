package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.datasource.GroupDashboardDataSource
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConfirmSettlementUseCaseImplTest {

    private val settlementRepository = mockk<SettlementRepository>()
    private val authenticationService = mockk<AuthenticationService>()
    private val groupRepository = mockk<GroupRepository>()
    private val contributionRepository = mockk<ContributionRepository>()
    private val groupDashboardDataSource = mockk<GroupDashboardDataSource>()
    private val getMemberBalancesFlowUseCase = mockk<GetMemberBalancesFlowUseCase>()
    private lateinit var useCase: ConfirmSettlementUseCaseImpl

    private val groupId = "group-123"
    private val settlementId = "settlement-1"
    private val payerId = "user-payer"
    private val payeeId = "user-payee"
    private val creatorId = "user-creator"
    private val baseSettlement = Settlement(
        fromUserId = payerId,
        toUserId = payeeId,
        amount = 1000L,
        currency = "EUR",
        sourcePocket = SettlementPocketType.POCKET
    )

    private val baseGroup = Group(
        id = groupId,
        createdBy = creatorId,
        members = listOf(payerId, payeeId, creatorId)
    )

    @BeforeEach
    fun setUp() {
        coEvery { contributionRepository.addContribution(any(), any()) } returns Unit
        coEvery { groupRepository.getGroupById(any()) } returns baseGroup
        useCase = ConfirmSettlementUseCaseImpl(
            settlementRepository = settlementRepository,
            authenticationService = authenticationService,
            groupRepository = groupRepository,
            contributionRepository = contributionRepository,
            groupDashboardDataSource = groupDashboardDataSource,
            getMemberBalancesFlowUseCase = getMemberBalancesFlowUseCase
        )
    }

    @Test
    fun `payer confirms SUGGESTED settlement transitions to CONFIRMED_BY_PAYER`() = runTest {
        every { authenticationService.requireUserId() } returns payerId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement,
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record
        coEvery { settlementRepository.updateSettlement(any()) } returns Unit

        val result = useCase(groupId, settlementId)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(SettlementStatus.CONFIRMED_BY_PAYER, updated.status)
        assertNotNull(updated.confirmedByPayerAt)
        coVerify(exactly = 1) { settlementRepository.updateSettlement(any()) }
    }

    @Test
    fun `payee confirms CONFIRMED_BY_PAYER transitions to RESOLVED`() = runTest {
        every { authenticationService.requireUserId() } returns payeeId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement,
            status = SettlementStatus.CONFIRMED_BY_PAYER,
            createdAt = LocalDateTime.now(),
            confirmedByPayerAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record
        coEvery { settlementRepository.updateSettlement(any()) } returns Unit

        val result = useCase(groupId, settlementId)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(SettlementStatus.RESOLVED, updated.status)
        assertNotNull(updated.confirmedByPayeeAt)
        assertNotNull(updated.resolvedAt)
        coVerify(exactly = 1) {
            contributionRepository.addContribution(
                groupId,
                match { it.linkedSettlementId == settlementId }
            )
        }
    }

    @Test
    fun `wrong party throws when confirming SUGGESTED`() = runTest {
        every { authenticationService.requireUserId() } returns payeeId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement,
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record

        val result = useCase(groupId, settlementId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `wrong party throws when confirming CONFIRMED_BY_PAYER`() = runTest {
        every { authenticationService.requireUserId() } returns payerId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement,
            status = SettlementStatus.CONFIRMED_BY_PAYER,
            createdAt = LocalDateTime.now(),
            confirmedByPayerAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record

        val result = useCase(groupId, settlementId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `confirming RESOLVED settlement throws`() = runTest {
        every { authenticationService.requireUserId() } returns payerId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement,
            status = SettlementStatus.RESOLVED,
            createdAt = LocalDateTime.now(),
            resolvedAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record

        val result = useCase(groupId, settlementId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `creditor payee confirms DISPUTED settlement transitions to RESOLVED`() = runTest {
        every { authenticationService.requireUserId() } returns payeeId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement,
            status = SettlementStatus.DISPUTED,
            createdAt = LocalDateTime.now(),
            disputedBy = payerId,
            disputeReason = "Amount incorrect"
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record
        coEvery { groupRepository.getGroupById(groupId) } returns baseGroup
        coEvery { settlementRepository.updateSettlement(any()) } returns Unit

        val result = useCase(groupId, settlementId)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(SettlementStatus.RESOLVED, updated.status)
        assertNotNull(updated.confirmedByPayeeAt)
        assertNotNull(updated.resolvedAt)
        coVerify(exactly = 1) { settlementRepository.updateSettlement(any()) }
        coVerify(exactly = 1) {
            contributionRepository.addContribution(
                groupId,
                match { it.linkedSettlementId == settlementId }
            )
        }
    }

    @Test
    fun `group creator confirms DISPUTED settlement transitions to RESOLVED`() = runTest {
        every { authenticationService.requireUserId() } returns creatorId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement,
            status = SettlementStatus.DISPUTED,
            createdAt = LocalDateTime.now(),
            disputedBy = payerId,
            disputeReason = "Amount incorrect"
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record
        coEvery { groupRepository.getGroupById(groupId) } returns baseGroup
        coEvery { settlementRepository.updateSettlement(any()) } returns Unit

        val result = useCase(groupId, settlementId)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(SettlementStatus.RESOLVED, updated.status)
        assertNotNull(updated.confirmedByPayeeAt)
        assertNotNull(updated.resolvedAt)
        coVerify(exactly = 1) { settlementRepository.updateSettlement(any()) }
        coVerify(exactly = 1) {
            contributionRepository.addContribution(
                groupId,
                match { it.linkedSettlementId == settlementId }
            )
        }
    }

    @Test
    fun `unauthorized user confirming DISPUTED settlement throws`() = runTest {
        every { authenticationService.requireUserId() } returns payerId // payer, but not payee (creditor) or creator
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement,
            status = SettlementStatus.DISPUTED,
            createdAt = LocalDateTime.now(),
            disputedBy = payerId,
            disputeReason = "Amount incorrect"
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record
        coEvery { groupRepository.getGroupById(groupId) } returns baseGroup

        val result = useCase(groupId, settlementId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `unknown settlementId throws`() = runTest {
        every { authenticationService.requireUserId() } returns payerId
        coEvery { settlementRepository.getSettlementById("unknown") } returns null

        val result = useCase(groupId, "unknown")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `payer confirms SUGGESTED when payee is unregistered transitions to RESOLVED`() = runTest {
        every { authenticationService.requireUserId() } returns payerId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement.copy(toUserId = "pending_payee"),
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record
        coEvery { settlementRepository.updateSettlement(any()) } returns Unit

        val result = useCase(groupId, settlementId)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(SettlementStatus.RESOLVED, updated.status)
        assertNotNull(updated.confirmedByPayerAt)
        assertNotNull(updated.resolvedAt)
        coVerify(exactly = 1) { settlementRepository.updateSettlement(any()) }
        coVerify(exactly = 1) {
            contributionRepository.addContribution(
                groupId,
                match { it.linkedSettlementId == settlementId }
            )
        }
    }

    @Test
    fun `payee confirms SUGGESTED when payer is unregistered transitions to RESOLVED`() = runTest {
        every { authenticationService.requireUserId() } returns payeeId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement.copy(fromUserId = "pending_payer"),
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record
        coEvery { settlementRepository.updateSettlement(any()) } returns Unit

        val result = useCase(groupId, settlementId)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(SettlementStatus.RESOLVED, updated.status)
        assertNotNull(updated.confirmedByPayeeAt)
        assertNotNull(updated.resolvedAt)
        coVerify(exactly = 1) { settlementRepository.updateSettlement(any()) }
        coVerify(exactly = 1) {
            contributionRepository.addContribution(
                groupId,
                match { it.linkedSettlementId == settlementId }
            )
        }
    }

    @Test
    fun `payer confirms CONFIRMED_BY_PAYER when payee is unregistered transitions to RESOLVED`() = runTest {
        every { authenticationService.requireUserId() } returns payerId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement.copy(toUserId = "pending_payee"),
            status = SettlementStatus.CONFIRMED_BY_PAYER,
            createdAt = LocalDateTime.now(),
            confirmedByPayerAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record
        coEvery { settlementRepository.updateSettlement(any()) } returns Unit

        val result = useCase(groupId, settlementId)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(SettlementStatus.RESOLVED, updated.status)
        assertNotNull(updated.confirmedByPayerAt)
        assertNotNull(updated.resolvedAt)
        coVerify(exactly = 1) { settlementRepository.updateSettlement(any()) }
        coVerify(exactly = 1) {
            contributionRepository.addContribution(
                groupId,
                match { it.linkedSettlementId == settlementId }
            )
        }
    }

    @Test
    fun `payee confirms CONFIRMED_BY_PAYER when payer is unregistered transitions to RESOLVED`() = runTest {
        every { authenticationService.requireUserId() } returns payeeId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement.copy(fromUserId = "pending_payer"),
            status = SettlementStatus.CONFIRMED_BY_PAYER,
            createdAt = LocalDateTime.now(),
            confirmedByPayerAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record
        coEvery { settlementRepository.updateSettlement(any()) } returns Unit

        val result = useCase(groupId, settlementId)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(SettlementStatus.RESOLVED, updated.status)
        assertNotNull(updated.confirmedByPayeeAt)
        assertNotNull(updated.resolvedAt)
        coVerify(exactly = 1) { settlementRepository.updateSettlement(any()) }
        coVerify(exactly = 1) {
            contributionRepository.addContribution(
                groupId,
                match { it.linkedSettlementId == settlementId }
            )
        }
    }

    @Test
    fun `wrong party confirms SUGGESTED when payee is unregistered throws`() = runTest {
        every { authenticationService.requireUserId() } returns payeeId // payee trying to confirm but only payer can
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement.copy(toUserId = "pending_payee"),
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record

        val result = useCase(groupId, settlementId)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `wrong party confirms SUGGESTED when payer is unregistered throws`() = runTest {
        every { authenticationService.requireUserId() } returns payerId // payer trying to confirm but only payee can
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement.copy(fromUserId = "pending_payer"),
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record

        val result = useCase(groupId, settlementId)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `wrong party confirms CONFIRMED_BY_PAYER when payee is unregistered throws`() = runTest {
        every { authenticationService.requireUserId() } returns payeeId // payee trying to confirm but only payer can
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement.copy(toUserId = "pending_payee"),
            status = SettlementStatus.CONFIRMED_BY_PAYER,
            createdAt = LocalDateTime.now(),
            confirmedByPayerAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record

        val result = useCase(groupId, settlementId)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `wrong party confirms CONFIRMED_BY_PAYER when payer is unregistered throws`() = runTest {
        every { authenticationService.requireUserId() } returns payerId // payer trying to confirm but only payee can
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement.copy(fromUserId = "pending_payer"),
            status = SettlementStatus.CONFIRMED_BY_PAYER,
            createdAt = LocalDateTime.now(),
            confirmedByPayerAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record

        val result = useCase(groupId, settlementId)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `payee confirms DISPUTED when payer is unregistered transitions to RESOLVED`() = runTest {
        every { authenticationService.requireUserId() } returns payeeId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement.copy(fromUserId = "pending_payer"),
            status = SettlementStatus.DISPUTED,
            createdAt = LocalDateTime.now(),
            disputedBy = payerId,
            disputeReason = "Amount incorrect"
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record
        coEvery { settlementRepository.updateSettlement(any()) } returns Unit

        val result = useCase(groupId, settlementId)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(SettlementStatus.RESOLVED, updated.status)
        assertNotNull(updated.confirmedByPayeeAt)
        assertNotNull(updated.resolvedAt)
        coVerify(exactly = 1) { settlementRepository.updateSettlement(any()) }
        coVerify(exactly = 1) {
            contributionRepository.addContribution(
                groupId,
                match { it.linkedSettlementId == settlementId }
            )
        }
    }

    @Test
    fun `confirming RESOLVED when payee is unregistered throws`() = runTest {
        every { authenticationService.requireUserId() } returns payerId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement.copy(toUserId = "pending_payee"),
            status = SettlementStatus.RESOLVED,
            createdAt = LocalDateTime.now(),
            resolvedAt = LocalDateTime.now()
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record

        val result = useCase(groupId, settlementId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `payee confirms CONFIRMED_BY_PAYER with foreign currency calculates equivalentBaseAmount`() = runTest {
        every { authenticationService.requireUserId() } returns payeeId

        val foreignSettlement = baseSettlement.copy(currency = "USD", amount = 1100L)
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = foreignSettlement,
            status = SettlementStatus.CONFIRMED_BY_PAYER,
            createdAt = LocalDateTime.now(),
            confirmedByPayerAt = LocalDateTime.now()
        )

        val snapshot = es.pedrazamiguez.splittrip.domain.model.GroupDashboardReadModel(
            group = baseGroup,
            contributions = emptyList(),
            withdrawals = emptyList(),
            expenses = emptyList(),
            subunits = emptyList(),
            settlements = emptyList()
        )
        val memberBalance = es.pedrazamiguez.splittrip.domain.model.MemberBalance(
            userId = payerId,
            cashInHandByCurrency = listOf(
                es.pedrazamiguez.splittrip.domain.model.CurrencyAmount(
                    currency = "USD",
                    amountCents = 1100L,
                    equivalentCents = 1000L
                )
            )
        )

        coEvery { settlementRepository.getSettlementById(settlementId) } returns record
        coEvery { groupRepository.getGroupById(groupId) } returns baseGroup.copy(currency = "EUR")
        coEvery { settlementRepository.updateSettlement(any()) } returns Unit
        coEvery { groupDashboardDataSource.getDashboardSnapshotFlow(groupId) } returns
            kotlinx.coroutines.flow.flowOf(snapshot)
        coEvery { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns listOf(memberBalance)

        val result = useCase(groupId, settlementId)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(SettlementStatus.RESOLVED, updated.status)

        coVerify(exactly = 1) {
            contributionRepository.addContribution(
                groupId,
                match {
                    it.linkedSettlementId == settlementId &&
                        it.currency == "USD" &&
                        it.amount == 1100L &&
                        it.equivalentBaseAmount == 1000L
                }
            )
        }
    }
}

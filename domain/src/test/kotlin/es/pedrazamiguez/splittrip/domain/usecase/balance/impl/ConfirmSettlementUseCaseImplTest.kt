package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
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
    private lateinit var useCase: ConfirmSettlementUseCaseImpl

    private val groupId = "group-123"
    private val settlementId = "settlement-1"
    private val payerId = "user-payer"
    private val payeeId = "user-payee"
    private val baseSettlement = Settlement(
        fromUserId = payerId,
        toUserId = payeeId,
        amount = 1000L,
        currency = "EUR",
        sourcePocket = SettlementPocketType.POCKET
    )

    @BeforeEach
    fun setUp() {
        useCase = ConfirmSettlementUseCaseImpl(
            settlementRepository = settlementRepository,
            authenticationService = authenticationService
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
    fun `confirming DISPUTED settlement throws`() = runTest {
        every { authenticationService.requireUserId() } returns payerId
        val record = SettlementRecord(
            id = settlementId,
            groupId = groupId,
            settlement = baseSettlement,
            status = SettlementStatus.DISPUTED,
            createdAt = LocalDateTime.now(),
            disputedBy = payeeId,
            disputeReason = "Amount incorrect"
        )
        coEvery { settlementRepository.getSettlementById(settlementId) } returns record

        val result = useCase(groupId, settlementId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `unknown settlementId throws`() = runTest {
        every { authenticationService.requireUserId() } returns payerId
        coEvery { settlementRepository.getSettlementById("unknown") } returns null

        val result = useCase(groupId, "unknown")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}

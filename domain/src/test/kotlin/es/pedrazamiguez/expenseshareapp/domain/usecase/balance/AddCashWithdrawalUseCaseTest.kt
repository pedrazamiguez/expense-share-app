package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.exception.NotGroupMemberException
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.service.CashWithdrawalValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.CashWithdrawalValidationService.ValidationResult
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AddCashWithdrawalUseCaseTest {

    private lateinit var cashWithdrawalRepository: CashWithdrawalRepository
    private lateinit var validationService: CashWithdrawalValidationService
    private lateinit var groupMembershipService: GroupMembershipService
    private lateinit var useCase: AddCashWithdrawalUseCase

    private val groupId = "group-123"
    private val withdrawal = CashWithdrawal(
        id = "cw-1",
        groupId = groupId,
        amountWithdrawn = 100000L,
        remainingAmount = 100000L,
        currency = "THB",
        deductedBaseAmount = 2700L,
        exchangeRate = BigDecimal("37.037")
    )

    @BeforeEach
    fun setUp() {
        cashWithdrawalRepository = mockk(relaxed = true)
        validationService = mockk()
        groupMembershipService = mockk()
        coEvery { groupMembershipService.requireMembership(any()) } just Runs
        every { validationService.validateAmountWithdrawn(any()) } returns ValidationResult.Valid
        every { validationService.validateDeductedBaseAmount(any()) } returns ValidationResult.Valid
        every { validationService.validateCurrency(any()) } returns ValidationResult.Valid
        every { validationService.validateExchangeRate(any()) } returns ValidationResult.Valid
        useCase = AddCashWithdrawalUseCase(
            cashWithdrawalRepository,
            validationService,
            groupMembershipService
        )
    }

    // ── Membership validation ─────────────────────────────────────────────────

    @Nested
    inner class MembershipValidation {

        @Test
        fun `fails with NotGroupMemberException when user is not a member`() = runTest {
            // Given
            coEvery {
                groupMembershipService.requireMembership(groupId)
            } throws NotGroupMemberException(groupId = groupId, userId = "user-123")

            // When
            val result = useCase(groupId, withdrawal)

            // Then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is NotGroupMemberException)
        }

        @Test
        fun `does not save withdrawal when membership check fails`() = runTest {
            // Given
            coEvery {
                groupMembershipService.requireMembership(groupId)
            } throws NotGroupMemberException(groupId = groupId, userId = "user-123")

            // When
            useCase(groupId, withdrawal)

            // Then
            coVerify(exactly = 0) { cashWithdrawalRepository.addWithdrawal(any(), any()) }
        }

        @Test
        fun `calls requireMembership with correct groupId on success`() = runTest {
            // When
            useCase(groupId, withdrawal)

            // Then
            coVerify(exactly = 1) { groupMembershipService.requireMembership(groupId) }
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Nested
    inner class Validation {

        @Test
        fun `fails when groupId is null`() = runTest {
            val result = useCase(null, withdrawal)
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("Group ID") == true)
        }

        @Test
        fun `fails when groupId is blank`() = runTest {
            val result = useCase("  ", withdrawal)
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("Group ID") == true)
        }
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Nested
    inner class HappyPath {

        @Test
        fun `saves withdrawal when all validations pass`() = runTest {
            // When
            val result = useCase(groupId, withdrawal)

            // Then
            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { cashWithdrawalRepository.addWithdrawal(groupId, withdrawal) }
        }
    }
}


package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ContributionValidationService")
class ContributionValidationServiceTest {

    private val service = ContributionValidationService()

    @Nested
    @DisplayName("validateAmount")
    inner class ValidateAmount {

        @Test
        fun `returns Valid for positive amount`() {
            val result = service.validateAmount(100L)
            assertTrue(result is ContributionValidationService.ValidationResult.Valid)
        }

        @Test
        fun `returns Invalid for zero amount`() {
            val result = service.validateAmount(0L)
            assertTrue(result is ContributionValidationService.ValidationResult.Invalid)
            val invalid = result as ContributionValidationService.ValidationResult.Invalid
            assertTrue(invalid.error == ContributionValidationService.ValidationError.AMOUNT_MUST_BE_POSITIVE)
        }

        @Test
        fun `returns Invalid for negative amount`() {
            val result = service.validateAmount(-500L)
            assertTrue(result is ContributionValidationService.ValidationResult.Invalid)
        }

        @Test
        fun `returns Valid for large amount`() {
            val result = service.validateAmount(999_999_99L)
            assertTrue(result is ContributionValidationService.ValidationResult.Valid)
        }

        @Test
        fun `returns Valid for 1 cent`() {
            val result = service.validateAmount(1L)
            assertTrue(result is ContributionValidationService.ValidationResult.Valid)
        }
    }

    @Nested
    @DisplayName("validate (full Contribution)")
    inner class ValidateContribution {

        @Test
        fun `returns Valid for contribution with positive amount`() {
            val contribution = Contribution(
                id = "test-1",
                groupId = "group-1",
                userId = "user-1",
                amount = 2550L,
                currency = "EUR"
            )
            val result = service.validate(contribution)
            assertTrue(result is ContributionValidationService.ValidationResult.Valid)
        }

        @Test
        fun `returns Invalid for contribution with zero amount`() {
            val contribution = Contribution(
                id = "test-1",
                groupId = "group-1",
                userId = "user-1",
                amount = 0L,
                currency = "EUR"
            )
            val result = service.validate(contribution)
            assertTrue(result is ContributionValidationService.ValidationResult.Invalid)
        }
    }
}

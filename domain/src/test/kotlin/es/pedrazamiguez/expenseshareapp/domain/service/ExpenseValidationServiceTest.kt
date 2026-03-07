package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.model.ValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExpenseValidationServiceTest {

    private lateinit var service: ExpenseValidationService

    @BeforeEach
    fun setUp() {
        service = ExpenseValidationService()
    }

    @Nested
    inner class ValidateTitle {

        @Test
        fun `valid title returns Valid`() {
            val result = service.validateTitle("Dinner")
            assertEquals(ValidationResult.Valid, result)
        }

        @Test
        fun `empty title returns Invalid`() {
            val result = service.validateTitle("")
            assertTrue(result is ValidationResult.Invalid)
            assertEquals("Title cannot be empty", (result as ValidationResult.Invalid).message)
        }

        @Test
        fun `blank title returns Invalid`() {
            val result = service.validateTitle("   ")
            assertTrue(result is ValidationResult.Invalid)
            assertEquals("Title cannot be empty", (result as ValidationResult.Invalid).message)
        }

        @Test
        fun `title with only whitespace returns Invalid`() {
            val result = service.validateTitle("\t\n  ")
            assertTrue(result is ValidationResult.Invalid)
        }

        @Test
        fun `title with content returns Valid`() {
            val result = service.validateTitle("Hotel booking")
            assertEquals(ValidationResult.Valid, result)
        }
    }

    @Nested
    inner class ValidateAmount {

        @Test
        fun `valid amount returns Valid`() {
            val result = service.validateAmount("10.50")
            assertEquals(ValidationResult.Valid, result)
        }

        @Test
        fun `valid integer amount returns Valid`() {
            val result = service.validateAmount("100")
            assertEquals(ValidationResult.Valid, result)
        }

        @Test
        fun `empty amount returns Invalid`() {
            val result = service.validateAmount("")
            assertTrue(result is ValidationResult.Invalid)
        }

        @Test
        fun `blank amount returns Invalid`() {
            val result = service.validateAmount("   ")
            assertTrue(result is ValidationResult.Invalid)
        }

        @Test
        fun `zero amount returns Invalid`() {
            val result = service.validateAmount("0")
            assertTrue(result is ValidationResult.Invalid)
        }

        @Test
        fun `negative amount returns Invalid`() {
            val result = service.validateAmount("-5.00")
            assertTrue(result is ValidationResult.Invalid)
        }

        @Test
        fun `invalid characters returns Invalid`() {
            val result = service.validateAmount("abc")
            assertTrue(result is ValidationResult.Invalid)
        }

        @Test
        fun `comma decimal format returns Valid`() {
            val result = service.validateAmount("12,50")
            assertEquals(ValidationResult.Valid, result)
        }

        @Test
        fun `amount with thousand separators returns Valid`() {
            val result = service.validateAmount("1,250.00")
            assertEquals(ValidationResult.Valid, result)
        }
    }

    @Nested
    inner class ValidateUserCount {

        @Test
        fun `positive count returns Valid`() {
            val result = service.validateUserCount(3)
            assertEquals(ValidationResult.Valid, result)
        }

        @Test
        fun `count of one returns Valid`() {
            val result = service.validateUserCount(1)
            assertEquals(ValidationResult.Valid, result)
        }

        @Test
        fun `zero count returns Invalid`() {
            val result = service.validateUserCount(0)
            assertTrue(result is ValidationResult.Invalid)
            assertEquals("User count must be greater than zero", (result as ValidationResult.Invalid).message)
        }

        @Test
        fun `negative count returns Invalid`() {
            val result = service.validateUserCount(-1)
            assertTrue(result is ValidationResult.Invalid)
        }
    }
}


package es.pedrazamiguez.expenseshareapp.domain.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CashWithdrawalValidationServiceTest {

    private val service = CashWithdrawalValidationService()

    @Test
    fun `validateAmountWithdrawn returns Valid for positive amount`() {
        val result = service.validateAmountWithdrawn(10000L)
        assertTrue(result is CashWithdrawalValidationService.ValidationResult.Valid)
    }

    @Test
    fun `validateAmountWithdrawn returns Invalid for zero`() {
        val result = service.validateAmountWithdrawn(0L)
        assertTrue(result is CashWithdrawalValidationService.ValidationResult.Invalid)
        val invalid = result as CashWithdrawalValidationService.ValidationResult.Invalid
        assertTrue(invalid.error == CashWithdrawalValidationService.ValidationError.AMOUNT_MUST_BE_POSITIVE)
    }

    @Test
    fun `validateAmountWithdrawn returns Invalid for negative`() {
        val result = service.validateAmountWithdrawn(-100L)
        assertTrue(result is CashWithdrawalValidationService.ValidationResult.Invalid)
    }

    @Test
    fun `validateDeductedBaseAmount returns Valid for positive amount`() {
        val result = service.validateDeductedBaseAmount(27000L)
        assertTrue(result is CashWithdrawalValidationService.ValidationResult.Valid)
    }

    @Test
    fun `validateDeductedBaseAmount returns Invalid for zero`() {
        val result = service.validateDeductedBaseAmount(0L)
        assertTrue(result is CashWithdrawalValidationService.ValidationResult.Invalid)
        val invalid = result as CashWithdrawalValidationService.ValidationResult.Invalid
        assertTrue(invalid.error == CashWithdrawalValidationService.ValidationError.DEDUCTED_AMOUNT_MUST_BE_POSITIVE)
    }

    @Test
    fun `validateCurrency returns Valid for non-blank currency`() {
        val result = service.validateCurrency("THB")
        assertTrue(result is CashWithdrawalValidationService.ValidationResult.Valid)
    }

    @Test
    fun `validateCurrency returns Invalid for blank currency`() {
        val result = service.validateCurrency("")
        assertTrue(result is CashWithdrawalValidationService.ValidationResult.Invalid)
        val invalid = result as CashWithdrawalValidationService.ValidationResult.Invalid
        assertTrue(invalid.error == CashWithdrawalValidationService.ValidationError.CURRENCY_REQUIRED)
    }

    @Test
    fun `validateExchangeRate returns Valid for positive rate`() {
        val result = service.validateExchangeRate(BigDecimal("37.037"))
        assertTrue(result is CashWithdrawalValidationService.ValidationResult.Valid)
    }

    @Test
    fun `validateExchangeRate returns Invalid for zero rate`() {
        val result = service.validateExchangeRate(BigDecimal.ZERO)
        assertTrue(result is CashWithdrawalValidationService.ValidationResult.Invalid)
        val invalid = result as CashWithdrawalValidationService.ValidationResult.Invalid
        assertTrue(invalid.error == CashWithdrawalValidationService.ValidationError.EXCHANGE_RATE_MUST_BE_POSITIVE)
    }

    @Test
    fun `validateExchangeRate returns Invalid for negative rate`() {
        val result = service.validateExchangeRate(BigDecimal("-1.5"))
        assertTrue(result is CashWithdrawalValidationService.ValidationResult.Invalid)
    }
}


package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType
import es.pedrazamiguez.expenseshareapp.domain.model.AddOn
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

class ExpenseCalculatorServiceTest {

    private val service = ExpenseCalculatorService()
    private val exchangeRateService = ExchangeRateCalculationService()
    private val addOnService = AddOnCalculationService()

    // BigDecimal-based method tests
    @Test
    fun `calculateGroupAmount multiplies source by rate`() {
        val result = exchangeRateService.calculateGroupAmount(
            sourceAmount = BigDecimal("100.00"),
            rate = BigDecimal("0.027")
        )
        assertEquals(BigDecimal("2.70"), result)
    }

    @Test
    fun `calculateGroupAmount returns zero when rate is zero`() {
        val result = exchangeRateService.calculateGroupAmount(
            sourceAmount = BigDecimal("100.00"),
            rate = BigDecimal.ZERO
        )
        assertEquals(BigDecimal.ZERO, result)
    }

    @Test
    fun `calculateImpliedRate divides target by source`() {
        val result = exchangeRateService.calculateImpliedRate(
            sourceAmount = BigDecimal("1000.00"),
            groupAmount = BigDecimal("27.35")
        )
        assertEquals(BigDecimal("0.027350"), result)
    }

    @Test
    fun `calculateImpliedRate returns zero when source is zero`() {
        val result = exchangeRateService.calculateImpliedRate(
            sourceAmount = BigDecimal.ZERO,
            groupAmount = BigDecimal("27.35")
        )
        assertEquals(BigDecimal.ZERO, result)
    }

    // String-based method tests
    @Test
    fun `calculateGroupAmountFromStrings handles valid inputs`() {
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "100.00",
            exchangeRateString = "1.5"
        )
        assertEquals("150.00", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings handles empty source amount`() {
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "",
            exchangeRateString = "1.5"
        )
        assertEquals("0.00", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings handles invalid rate defaults to one`() {
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "100.00",
            exchangeRateString = "invalid"
        )
        assertEquals("100.00", result)
    }

    @Test
    fun `calculateImpliedRateFromStrings handles valid inputs`() {
        val result = exchangeRateService.calculateImpliedRateFromStrings(
            sourceAmountString = "1000.00",
            groupAmountString = "27.35"
        )
        assertEquals("0.02735", result)
    }

    @Test
    fun `calculateImpliedRateFromStrings handles empty source amount`() {
        val result = exchangeRateService.calculateImpliedRateFromStrings(
            sourceAmountString = "",
            groupAmountString = "27.35"
        )
        assertEquals("0", result)
    }

    @Test
    fun `calculateImpliedRateFromStrings handles invalid group amount`() {
        val result = exchangeRateService.calculateImpliedRateFromStrings(
            sourceAmountString = "100.00",
            groupAmountString = "invalid"
        )
        assertEquals("0", result)
    }

    // centsToBigDecimal tests
    @Test
    fun `centsToBigDecimal converts cents to decimal for standard currency`() {
        val result = service.centsToBigDecimal(12345L)
        assertEquals(BigDecimal("123.45"), result)
    }

    @Test
    fun `centsToBigDecimal handles zero decimal places currency like JPY`() {
        val result = service.centsToBigDecimal(12345L, decimalPlaces = 0)
        assertEquals(BigDecimal("12345"), result)
    }

    @Test
    fun `centsToBigDecimal handles three decimal places currency like TND`() {
        val result = service.centsToBigDecimal(12345L, decimalPlaces = 3)
        assertEquals(BigDecimal("12.345"), result)
    }

    // Variable decimal places tests
    @Test
    fun `calculateGroupAmount respects target decimal places for JPY`() {
        val result = exchangeRateService.calculateGroupAmount(
            sourceAmount = BigDecimal("100.00"),
            rate = BigDecimal("157.25"),
            targetDecimalPlaces = 0
        )
        assertEquals(BigDecimal("15725"), result)
    }

    @Test
    fun `calculateGroupAmount respects target decimal places for TND`() {
        val result = exchangeRateService.calculateGroupAmount(
            sourceAmount = BigDecimal("100.00"),
            rate = BigDecimal("3.12345"),
            targetDecimalPlaces = 3
        )
        assertEquals(BigDecimal("312.345"), result)
    }

    @Test
    fun `calculateGroupAmountFromStrings respects source and target decimal places`() {
        // Converting from JPY (0 decimals) to EUR (2 decimals)
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "15725",
            exchangeRateString = "0.00636",
            sourceDecimalPlaces = 0,
            targetDecimalPlaces = 2
        )
        assertEquals("100.01", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings handles TND to EUR conversion`() {
        // Converting from TND (3 decimals) to EUR (2 decimals)
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "312.345",
            exchangeRateString = "0.32",
            sourceDecimalPlaces = 3,
            targetDecimalPlaces = 2
        )
        assertEquals("99.95", result)
    }

    @Test
    fun `calculateImpliedRateFromStrings respects source decimal places`() {
        // Source is JPY (0 decimals)
        val result = exchangeRateService.calculateImpliedRateFromStrings(
            sourceAmountString = "15725",
            groupAmountString = "100.00",
            sourceDecimalPlaces = 0
        )
        assertEquals("0.006359", result)
    }

    // Locale normalization tests (parseAmount via string methods)
    @Test
    fun `calculateGroupAmountFromStrings handles European format with comma as decimal`() {
        // European format: 1.234,56 (dot as thousand separator, comma as decimal)
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "1.234,56",
            exchangeRateString = "1.0"
        )
        assertEquals("1234.56", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings handles US format with dot as decimal`() {
        // US format: 1,234.56 (comma as thousand separator, dot as decimal)
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "1,234.56",
            exchangeRateString = "1.0"
        )
        assertEquals("1234.56", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings handles amount without thousand separators`() {
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "1234.56",
            exchangeRateString = "1.0"
        )
        assertEquals("1234.56", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings handles whole number without decimals`() {
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "15725",
            exchangeRateString = "1.0"
        )
        assertEquals("15725.00", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings preserves precision for TND with 3 decimals`() {
        // TND has 3 decimal places - precision should be preserved
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "12.345",
            exchangeRateString = "1.0",
            sourceDecimalPlaces = 3,
            targetDecimalPlaces = 3
        )
        assertEquals("12.345", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings handles European format for TND`() {
        // European format with 3 decimal places: 12,345 means 12.345 in TND
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "12,345",
            exchangeRateString = "1.0",
            sourceDecimalPlaces = 3,
            targetDecimalPlaces = 3
        )
        assertEquals("12.345", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings handles large European format amount`() {
        // 1.234.567,89 in European format = 1234567.89
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "1.234.567,89",
            exchangeRateString = "1.0"
        )
        assertEquals("1234567.89", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings handles large US format amount`() {
        // 1,234,567.89 in US format = 1234567.89
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "1,234,567.89",
            exchangeRateString = "1.0"
        )
        assertEquals("1234567.89", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings handles whitespace in input`() {
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "  100.50  ",
            exchangeRateString = "1.0"
        )
        assertEquals("100.50", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings returns zero for invalid input`() {
        val result = exchangeRateService.calculateGroupAmountFromStrings(
            sourceAmountString = "invalid",
            exchangeRateString = "1.0"
        )
        assertEquals("0.00", result)
    }

    @Test
    fun `calculateImpliedRateFromStrings handles European format source amount`() {
        val result = exchangeRateService.calculateImpliedRateFromStrings(
            sourceAmountString = "1.000,00",
            groupAmountString = "27.35"
        )
        assertEquals("0.02735", result)
    }

    // Display rate methods tests (user-friendly format: 1 GroupCurrency = X SourceCurrency)
    @Test
    fun `calculateGroupAmountFromDisplayRate converts THB to EUR correctly`() {
        // User enters: 1000 THB, rate: 37 (meaning 1 EUR = 37 THB)
        // Expected: 1000 / 37 = 27.03 EUR
        val result = exchangeRateService.calculateGroupAmountFromDisplayRate(
            sourceAmountString = "1000.00",
            displayRateString = "37.0",
            sourceDecimalPlaces = 2,
            targetDecimalPlaces = 2
        )
        assertEquals("27.03", result)
    }

    @Test
    fun `calculateGroupAmountFromDisplayRate handles rate of 1`() {
        // Same currency, rate is 1
        val result = exchangeRateService.calculateGroupAmountFromDisplayRate(
            sourceAmountString = "100.00",
            displayRateString = "1.0"
        )
        assertEquals("100.00", result)
    }

    @Test
    fun `calculateGroupAmountFromDisplayRate returns zero when rate is zero`() {
        val result = exchangeRateService.calculateGroupAmountFromDisplayRate(
            sourceAmountString = "100.00",
            displayRateString = "0"
        )
        assertEquals("0", result)
    }

    @Test
    fun `calculateGroupAmountFromDisplayRate handles empty source amount`() {
        val result = exchangeRateService.calculateGroupAmountFromDisplayRate(
            sourceAmountString = "",
            displayRateString = "37.0"
        )
        assertEquals("0.00", result)
    }

    @Test
    fun `calculateImpliedDisplayRateFromStrings calculates correct display rate`() {
        // If 1000 THB = 27.03 EUR, the display rate should be ~37 (1 EUR = 37 THB)
        // 1000 / 27.03 = 36.996671
        val result = exchangeRateService.calculateImpliedDisplayRateFromStrings(
            sourceAmountString = "1000.00",
            groupAmountString = "27.03"
        )
        // The result should start with 36.99 (the exact precision may vary)
        assert(result.startsWith("36.99")) { "Expected result starting with 36.99, but got: $result" }
    }

    @Test
    fun `calculateImpliedDisplayRateFromStrings returns zero when target is zero`() {
        val result = exchangeRateService.calculateImpliedDisplayRateFromStrings(
            sourceAmountString = "1000.00",
            groupAmountString = "0"
        )
        assertEquals("0", result)
    }

    @Test
    fun `displayRateToCalculationRate inverts rate correctly`() {
        // Display rate: 37 (1 EUR = 37 THB)
        // Calculation rate should be: 1/37 = 0.027027
        val result = exchangeRateService.displayRateToCalculationRate("37.0")
        assertEquals("0.027027", result.stripTrailingZeros().toPlainString())
    }

    @Test
    fun `displayRateToCalculationRate returns zero when display rate is zero`() {
        val result = exchangeRateService.displayRateToCalculationRate("0")
        assertEquals(BigDecimal.ZERO, result)
    }

    @Test
    fun `displayRateToCalculationRate handles invalid input defaults to one`() {
        val result = exchangeRateService.displayRateToCalculationRate("invalid")
        assertEquals(BigDecimal.ONE.setScale(6), result)
    }

    // Locale-specific rate parsing tests (Spanish locale uses comma as decimal separator)
    @Test
    fun `calculateGroupAmountFromDisplayRate handles Spanish locale rate with comma`() {
        // User enters rate with comma as decimal separator: 37,220844 (Spanish format)
        val result = exchangeRateService.calculateGroupAmountFromDisplayRate(
            sourceAmountString = "1000.00",
            displayRateString = "37,220844",
            sourceDecimalPlaces = 2,
            targetDecimalPlaces = 2
        )
        // 1000 / 37.220844 = 26.87 EUR
        assertEquals("26.87", result)
    }

    @Test
    fun `calculateGroupAmountFromDisplayRate handles rate with dot decimal separator`() {
        // User enters rate with dot as decimal separator: 37.220844 (US/UK format)
        val result = exchangeRateService.calculateGroupAmountFromDisplayRate(
            sourceAmountString = "1000.00",
            displayRateString = "37.220844",
            sourceDecimalPlaces = 2,
            targetDecimalPlaces = 2
        )
        // 1000 / 37.220844 = 26.87 EUR
        assertEquals("26.87", result)
    }

    @Test
    fun `displayRateToCalculationRate handles Spanish locale rate with comma`() {
        // Display rate with comma: 37,22 (Spanish format for 37.22)
        val result = exchangeRateService.displayRateToCalculationRate("37,22")
        // 1 / 37.22 = 0.0268672... rounds to 0.026867 with HALF_UP at 6 decimals
        assertEquals("0.026867", result.stripTrailingZeros().toPlainString())
    }

    @Test
    fun `calculateImpliedDisplayRateFromStrings handles Spanish locale amounts`() {
        // Source amount with comma: 1.000,00 (Spanish format for 1000.00)
        val result = exchangeRateService.calculateImpliedDisplayRateFromStrings(
            sourceAmountString = "1.000,00",
            groupAmountString = "27,03"
        )
        // Should calculate: 1000 / 27.03 = ~36.99
        assert(result.startsWith("36.99")) { "Expected result starting with 36.99, but got: $result" }
    }

    // ===================== FIFO Cash Calculation Tests =====================

    @Test
    fun `hasInsufficientCash returns false when sufficient cash available`() {
        val withdrawals = listOf(
            createWithdrawal(remainingAmount = 10000L)
        )
        assertFalse(service.hasInsufficientCash(5000L, withdrawals))
    }

    @Test
    fun `hasInsufficientCash returns true when insufficient cash`() {
        val withdrawals = listOf(
            createWithdrawal(remainingAmount = 3000L)
        )
        assertTrue(service.hasInsufficientCash(5000L, withdrawals))
    }

    @Test
    fun `hasInsufficientCash returns false when exact match`() {
        val withdrawals = listOf(
            createWithdrawal(remainingAmount = 5000L)
        )
        assertFalse(service.hasInsufficientCash(5000L, withdrawals))
    }

    @Test
    fun `calculateFifoCashAmount single withdrawal exact match`() {
        // 10000 THB withdrawn at rate: deductedBase=27000 EUR cents for 1000000 THB cents
        // rate = 27000/1000000 = 0.027
        val withdrawals = listOf(
            createWithdrawal(
                id = "w1",
                amountWithdrawn = 1000000L, // 10000 THB in cents
                remainingAmount = 1000000L,
                deductedBaseAmount = 27000L // 270 EUR in cents
            )
        )
        val result = service.calculateFifoCashAmount(1000000L, withdrawals)

        assertEquals(1, result.tranches.size)
        assertEquals("w1", result.tranches[0].withdrawalId)
        assertEquals(1000000L, result.tranches[0].amountConsumed)
        assertEquals(27000L, result.groupAmountCents)
    }

    @Test
    fun `calculateFifoCashAmount multi-withdrawal FIFO consumption`() {
        val w1 = createWithdrawal(
            id = "w1",
            amountWithdrawn = 1000000L,
            remainingAmount = 5000L,
            deductedBaseAmount = 26400L
        )
        val w2 = createWithdrawal(
            id = "w2",
            amountWithdrawn = 500000L,
            remainingAmount = 500000L,
            deductedBaseAmount = 13587L
        )

        val result = service.calculateFifoCashAmount(23000L, listOf(w1, w2))

        assertEquals(2, result.tranches.size)
        assertEquals("w1", result.tranches[0].withdrawalId)
        assertEquals(5000L, result.tranches[0].amountConsumed)
        assertEquals("w2", result.tranches[1].withdrawalId)
        assertEquals(18000L, result.tranches[1].amountConsumed)
        assertTrue(result.groupAmountCents > 0)
    }

    @Test
    fun `calculateFifoCashAmount single withdrawal partial consumption`() {
        val w1 = createWithdrawal(
            id = "w1",
            amountWithdrawn = 1000000L,
            remainingAmount = 1000000L,
            deductedBaseAmount = 27000L
        )

        val result = service.calculateFifoCashAmount(50000L, listOf(w1))

        assertEquals(1, result.tranches.size)
        assertEquals("w1", result.tranches[0].withdrawalId)
        assertEquals(50000L, result.tranches[0].amountConsumed)
        assertEquals(1350L, result.groupAmountCents)
    }

    @Test
    fun `calculateFifoCashAmount throws on insufficient cash`() {
        val withdrawals = listOf(
            createWithdrawal(remainingAmount = 3000L, amountWithdrawn = 3000L, deductedBaseAmount = 100L)
        )
        assertThrows<IllegalStateException> {
            service.calculateFifoCashAmount(5000L, withdrawals)
        }
    }

    @Test
    fun `calculateFifoCashAmount throws on zero amount`() {
        val withdrawals = listOf(
            createWithdrawal(remainingAmount = 5000L, amountWithdrawn = 5000L, deductedBaseAmount = 100L)
        )
        assertThrows<IllegalArgumentException> {
            service.calculateFifoCashAmount(0L, withdrawals)
        }
    }

    @Test
    fun `calculateFifoCashAmount same currency same rate`() {
        val w1 = createWithdrawal(
            id = "w1",
            amountWithdrawn = 50000L,
            remainingAmount = 50000L,
            deductedBaseAmount = 50000L
        )

        val result = service.calculateFifoCashAmount(30000L, listOf(w1))

        assertEquals(1, result.tranches.size)
        assertEquals(30000L, result.tranches[0].amountConsumed)
        assertEquals(30000L, result.groupAmountCents)
    }

    // ===================== calculateExchangeRate Tests =====================

    @Test
    fun `calculateExchangeRate computes correct rate from withdrawal amounts`() {
        // 1000000 THB cents / 27000 EUR cents = 37.037037
        val result = exchangeRateService.calculateExchangeRate(
            amountWithdrawn = 1000000L,
            deductedBaseAmount = 27000L
        )
        assertEquals(0, BigDecimal("37.037037").compareTo(result))
    }

    @Test
    fun `calculateExchangeRate returns ONE for same currency (1-to-1)`() {
        val result = exchangeRateService.calculateExchangeRate(
            amountWithdrawn = 50000L,
            deductedBaseAmount = 50000L
        )
        assertEquals(0, BigDecimal.ONE.compareTo(result))
    }

    @Test
    fun `calculateExchangeRate returns ONE when deductedBaseAmount is zero`() {
        val result = exchangeRateService.calculateExchangeRate(
            amountWithdrawn = 1000000L,
            deductedBaseAmount = 0L
        )
        assertEquals(0, BigDecimal.ONE.compareTo(result))
    }

    @Test
    fun `calculateExchangeRate returns ONE when deductedBaseAmount is negative`() {
        val result = exchangeRateService.calculateExchangeRate(
            amountWithdrawn = 1000000L,
            deductedBaseAmount = -100L
        )
        assertEquals(0, BigDecimal.ONE.compareTo(result))
    }

    // ===================== distributeAmount Tests =====================

    data class DistributeTestCase(
        val description: String,
        val totalAmount: BigDecimal,
        val numberOfUsers: Int,
        val decimalPlaces: Int,
        val expectedAllocations: List<BigDecimal>
    ) {
        override fun toString(): String = description
    }

    companion object {
        @JvmStatic
        fun distributeAmountTestCases(): Stream<DistributeTestCase> = Stream.of(
            DistributeTestCase(
                description = "100 divided by 3 users (classic remainder)",
                totalAmount = BigDecimal("100.00"),
                numberOfUsers = 3,
                decimalPlaces = 2,
                expectedAllocations = listOf(
                    BigDecimal("33.34"),
                    BigDecimal("33.33"),
                    BigDecimal("33.33")
                )
            ),
            DistributeTestCase(
                description = "10.00 divided by 2 users (even split)",
                totalAmount = BigDecimal("10.00"),
                numberOfUsers = 2,
                decimalPlaces = 2,
                expectedAllocations = listOf(
                    BigDecimal("5.00"),
                    BigDecimal("5.00")
                )
            ),
            DistributeTestCase(
                description = "100.00 divided by 1 user (single user)",
                totalAmount = BigDecimal("100.00"),
                numberOfUsers = 1,
                decimalPlaces = 2,
                expectedAllocations = listOf(BigDecimal("100.00"))
            ),
            // Large group: 10.00 / 7 = floor 1.42, remainder 0.06 (6 cents to first 6 users)
            DistributeTestCase(
                description = "10.00 divided by 7 users (large group remainder)",
                totalAmount = BigDecimal("10.00"),
                numberOfUsers = 7,
                decimalPlaces = 2,
                expectedAllocations = listOf(
                    BigDecimal("1.43"),
                    BigDecimal("1.43"),
                    BigDecimal("1.43"),
                    BigDecimal("1.43"),
                    BigDecimal("1.43"),
                    BigDecimal("1.43"),
                    BigDecimal("1.42")
                )
            ),
            DistributeTestCase(
                description = "1000 JPY divided by 3 users (zero decimals)",
                totalAmount = BigDecimal("1000"),
                numberOfUsers = 3,
                decimalPlaces = 0,
                expectedAllocations = listOf(
                    BigDecimal("334"),
                    BigDecimal("333"),
                    BigDecimal("333")
                )
            ),
            DistributeTestCase(
                description = "10.000 TND divided by 3 users (three decimals)",
                totalAmount = BigDecimal("10.000"),
                numberOfUsers = 3,
                decimalPlaces = 3,
                expectedAllocations = listOf(
                    BigDecimal("3.334"),
                    BigDecimal("3.333"),
                    BigDecimal("3.333")
                )
            ),
            DistributeTestCase(
                description = "0.01 divided by 3 users (minimum unit remainder)",
                totalAmount = BigDecimal("0.01"),
                numberOfUsers = 3,
                decimalPlaces = 2,
                expectedAllocations = listOf(
                    BigDecimal("0.01"),
                    BigDecimal("0.00"),
                    BigDecimal("0.00")
                )
            ),
            DistributeTestCase(
                description = "0.05 divided by 3 users (2-cent remainder)",
                totalAmount = BigDecimal("0.05"),
                numberOfUsers = 3,
                decimalPlaces = 2,
                expectedAllocations = listOf(
                    BigDecimal("0.02"),
                    BigDecimal("0.02"),
                    BigDecimal("0.01")
                )
            ),
            // Edge case: totalAmount has more fractional digits than decimalPlaces
            // 10.005 normalized to 2 decimals = 10.01 (HALF_UP), then split among 3
            DistributeTestCase(
                description = "10.005 divided by 3 users (excess precision normalized)",
                totalAmount = BigDecimal("10.005"),
                numberOfUsers = 3,
                decimalPlaces = 2,
                expectedAllocations = listOf(
                    BigDecimal("3.34"),
                    BigDecimal("3.34"),
                    BigDecimal("3.33")
                )
            )
        )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("distributeAmountTestCases")
    fun `distributeAmount allocates correctly and conserves total`(testCase: DistributeTestCase) {
        val result = service.distributeAmount(
            totalAmount = testCase.totalAmount,
            numberOfUsers = testCase.numberOfUsers,
            decimalPlaces = testCase.decimalPlaces
        )

        assertEquals(
            testCase.expectedAllocations.size,
            result.size,
            "Expected ${testCase.expectedAllocations.size} allocations but got ${result.size}"
        )
        testCase.expectedAllocations.forEachIndexed { index, expected ->
            assertEquals(
                expected,
                result[index],
                "Allocation at index $index: expected $expected but got ${result[index]}"
            )
        }

        // Conservation invariant: sum of allocations must equal the normalized total
        // (totalAmount rounded to decimalPlaces, since the method normalizes internally)
        val normalizedTotal = testCase.totalAmount.setScale(testCase.decimalPlaces, java.math.RoundingMode.HALF_UP)
        val sum = result.fold(BigDecimal.ZERO) { acc, bd -> acc.add(bd) }
        assertEquals(
            0,
            normalizedTotal.compareTo(sum),
            "Sum of allocations ($sum) must equal normalized total ($normalizedTotal)"
        )
    }

    @ParameterizedTest(name = "Total={0}, Users={1}")
    @CsvSource(
        "100.00, 3",
        "99.99, 7",
        "1.00, 11",
        "0.01, 2",
        "10000.00, 13",
        "1.00, 100"
    )
    fun `distributeAmount sum always equals total (conservation invariant)`(totalStr: String, numberOfUsers: Int) {
        val total = BigDecimal(totalStr)
        val result = service.distributeAmount(total, numberOfUsers)

        val sum = result.fold(BigDecimal.ZERO) { acc, bd -> acc.add(bd) }
        assertEquals(
            0,
            total.compareTo(sum),
            "Conservation violated: sum=$sum != total=$total for $numberOfUsers users"
        )
        assertEquals(numberOfUsers, result.size)
    }

    @Test
    fun `distributeAmount throws on zero users`() {
        assertThrows<IllegalArgumentException> {
            service.distributeAmount(BigDecimal("100.00"), 0)
        }
    }

    @Test
    fun `distributeAmount throws on negative users`() {
        assertThrows<IllegalArgumentException> {
            service.distributeAmount(BigDecimal("100.00"), -1)
        }
    }

    // ===================== Helpers =====================

    private fun createWithdrawal(
        id: String = "w-test",
        amountWithdrawn: Long = 1000000L,
        remainingAmount: Long = 1000000L,
        deductedBaseAmount: Long = 27000L,
        currency: String = "THB"
    ) = CashWithdrawal(
        id = id,
        groupId = "group-1",
        withdrawnBy = "user-1",
        amountWithdrawn = amountWithdrawn,
        remainingAmount = remainingAmount,
        currency = currency,
        deductedBaseAmount = deductedBaseAmount,
        exchangeRate = if (deductedBaseAmount > 0) {
            BigDecimal(amountWithdrawn).divide(BigDecimal(deductedBaseAmount), 6, java.math.RoundingMode.HALF_UP)
        } else {
            BigDecimal.ONE
        },
        createdAt = LocalDateTime.of(2026, 1, 15, 12, 0)
    )

    // ── Blended Rate Tests ────────────────────────────────────────────────────

    @Test
    fun `calculateBlendedRate returns correct internal rate`() {
        // 1000 THB (100000 cents) = 27 EUR (2700 cents) → internal rate = 2700 / 100000 = 0.027
        val result = exchangeRateService.calculateBlendedRate(
            sourceAmountCents = 100000L,
            groupAmountCents = 2700L
        )
        assertEquals(BigDecimal("0.027000"), result)
    }

    @Test
    fun `calculateBlendedRate returns ONE when source is zero`() {
        val result = exchangeRateService.calculateBlendedRate(sourceAmountCents = 0L, groupAmountCents = 2700L)
        assertEquals(BigDecimal.ONE, result)
    }

    @Test
    fun `calculateBlendedRate returns ONE when group is zero`() {
        val result = exchangeRateService.calculateBlendedRate(sourceAmountCents = 100000L, groupAmountCents = 0L)
        assertEquals(BigDecimal.ONE, result)
    }

    @Test
    fun `calculateBlendedRate returns ONE when both are negative`() {
        val result = exchangeRateService.calculateBlendedRate(sourceAmountCents = -1L, groupAmountCents = -1L)
        assertEquals(BigDecimal.ONE, result)
    }

    @Test
    fun `calculateBlendedDisplayRate returns correct display rate`() {
        // 1000 THB (100000 cents) = 27 EUR (2700 cents) → display rate = 100000 / 2700 ≈ 37.037037
        val result = exchangeRateService.calculateBlendedDisplayRate(
            sourceAmountCents = 100000L,
            groupAmountCents = 2700L
        )
        assertEquals(BigDecimal("37.037037"), result)
    }

    @Test
    fun `calculateBlendedDisplayRate returns ONE when source is zero`() {
        val result = exchangeRateService.calculateBlendedDisplayRate(sourceAmountCents = 0L, groupAmountCents = 2700L)
        assertEquals(BigDecimal.ONE, result)
    }

    @Test
    fun `calculateBlendedDisplayRate returns ONE when group is zero`() {
        val result = exchangeRateService.calculateBlendedDisplayRate(sourceAmountCents = 100000L, groupAmountCents = 0L)
        assertEquals(BigDecimal.ONE, result)
    }

    @Test
    fun `blended rates are inverses of each other`() {
        val sourceAmountCents = 175000L // 1750 THB
        val groupAmountCents = 4752L // 47.52 EUR

        val internalRate = exchangeRateService.calculateBlendedRate(sourceAmountCents, groupAmountCents)
        val displayRate = exchangeRateService.calculateBlendedDisplayRate(sourceAmountCents, groupAmountCents)

        // internal * display ≈ 1.0 (within rounding tolerance)
        val product = internalRate.multiply(displayRate)
        assertTrue(
            product.subtract(BigDecimal.ONE).abs() < BigDecimal("0.001"),
            "Expected internal * display ≈ 1.0, got $product"
        )
    }

    // ── Add-On Calculation Tests ─────────────────────────────────────────

    @Test
    fun `calculateTotalOnTopAddOns sums non-discount ON_TOP add-ons`() {
        val addOns = listOf(
            AddOn(type = AddOnType.FEE, mode = AddOnMode.ON_TOP, groupAmountCents = 250),
            AddOn(type = AddOnType.TIP, mode = AddOnMode.ON_TOP, groupAmountCents = 500),
            AddOn(type = AddOnType.SURCHARGE, mode = AddOnMode.ON_TOP, groupAmountCents = 100)
        )
        assertEquals(850, addOnService.calculateTotalOnTopAddOns(addOns))
    }

    @Test
    fun `calculateTotalOnTopAddOns excludes INCLUDED add-ons`() {
        val addOns = listOf(
            AddOn(type = AddOnType.TIP, mode = AddOnMode.INCLUDED, groupAmountCents = 500),
            AddOn(type = AddOnType.FEE, mode = AddOnMode.ON_TOP, groupAmountCents = 250)
        )
        assertEquals(250, addOnService.calculateTotalOnTopAddOns(addOns))
    }

    @Test
    fun `calculateTotalOnTopAddOns excludes DISCOUNT add-ons`() {
        val addOns = listOf(
            AddOn(type = AddOnType.DISCOUNT, mode = AddOnMode.ON_TOP, groupAmountCents = 300),
            AddOn(type = AddOnType.FEE, mode = AddOnMode.ON_TOP, groupAmountCents = 100)
        )
        assertEquals(100, addOnService.calculateTotalOnTopAddOns(addOns))
    }

    @Test
    fun `calculateTotalOnTopAddOns returns zero for empty list`() {
        assertEquals(0, addOnService.calculateTotalOnTopAddOns(emptyList()))
    }

    @Test
    fun `calculateTotalAddOnExtras sums ON_TOP and INCLUDED non-discount add-ons`() {
        val addOns = listOf(
            AddOn(type = AddOnType.FEE, mode = AddOnMode.ON_TOP, groupAmountCents = 250),
            AddOn(type = AddOnType.TIP, mode = AddOnMode.INCLUDED, groupAmountCents = 500),
            AddOn(type = AddOnType.SURCHARGE, mode = AddOnMode.ON_TOP, groupAmountCents = 100)
        )
        // 250 + 500 + 100 = 850
        assertEquals(850, addOnService.calculateTotalAddOnExtras(addOns))
    }

    @Test
    fun `calculateTotalAddOnExtras excludes DISCOUNT add-ons regardless of mode`() {
        val addOns = listOf(
            AddOn(type = AddOnType.DISCOUNT, mode = AddOnMode.ON_TOP, groupAmountCents = 300),
            AddOn(type = AddOnType.DISCOUNT, mode = AddOnMode.INCLUDED, groupAmountCents = 200),
            AddOn(type = AddOnType.FEE, mode = AddOnMode.ON_TOP, groupAmountCents = 100)
        )
        // Only FEE counts
        assertEquals(100, addOnService.calculateTotalAddOnExtras(addOns))
    }

    @Test
    fun `calculateTotalAddOnExtras returns zero for empty list`() {
        assertEquals(0, addOnService.calculateTotalAddOnExtras(emptyList()))
    }

    @Test
    fun `calculateTotalAddOnExtras INCLUDED-only returns their sum`() {
        val addOns = listOf(
            AddOn(type = AddOnType.TIP, mode = AddOnMode.INCLUDED, groupAmountCents = 727),
            AddOn(type = AddOnType.FEE, mode = AddOnMode.INCLUDED, groupAmountCents = 100)
        )
        assertEquals(827, addOnService.calculateTotalAddOnExtras(addOns))
    }

    @Test
    fun `calculateEffectiveGroupAmount returns base when no add-ons`() {
        assertEquals(10000L, addOnService.calculateEffectiveGroupAmount(10000L, emptyList()))
    }

    @Test
    fun `calculateEffectiveGroupAmount adds ON_TOP fee to base`() {
        val addOns = listOf(
            AddOn(type = AddOnType.FEE, mode = AddOnMode.ON_TOP, groupAmountCents = 250)
        )
        // 10000 + 250 = 10250
        assertEquals(10250L, addOnService.calculateEffectiveGroupAmount(10000L, addOns))
    }

    @Test
    fun `calculateEffectiveGroupAmount subtracts DISCOUNT from base`() {
        val addOns = listOf(
            AddOn(type = AddOnType.DISCOUNT, mode = AddOnMode.ON_TOP, groupAmountCents = 500)
        )
        // 10000 - 500 = 9500
        assertEquals(9500L, addOnService.calculateEffectiveGroupAmount(10000L, addOns))
    }

    @Test
    fun `calculateEffectiveGroupAmount INCLUDED tip adds back to base`() {
        // Stored base cost = 9000, INCLUDED tip = 1000 → effective = 10000 (reconstructs original total)
        val addOns = listOf(
            AddOn(type = AddOnType.TIP, mode = AddOnMode.INCLUDED, groupAmountCents = 1000)
        )
        assertEquals(10000L, addOnService.calculateEffectiveGroupAmount(9000L, addOns))
    }

    @Test
    fun `calculateEffectiveGroupAmount handles mixed add-ons correctly`() {
        // Scenario: 100.00 EUR base + 10 EUR tip on top + 2.50 EUR fee + 8 EUR tip included − 5 EUR discount
        val addOns = listOf(
            AddOn(type = AddOnType.TIP, mode = AddOnMode.ON_TOP, groupAmountCents = 1000),
            AddOn(type = AddOnType.FEE, mode = AddOnMode.ON_TOP, groupAmountCents = 250),
            AddOn(type = AddOnType.DISCOUNT, mode = AddOnMode.ON_TOP, groupAmountCents = 500),
            AddOn(type = AddOnType.TIP, mode = AddOnMode.INCLUDED, groupAmountCents = 800)
        )
        // 10000 + 1000 + 250 + 800 - 500 = 11550
        assertEquals(11550L, addOnService.calculateEffectiveGroupAmount(10000L, addOns))
    }

    @Test
    fun `calculateEffectiveGroupAmount scenario E1 - boat with foreign fee`() {
        // E1: 4000 MXN boat, bank fee 2.50 EUR
        // Base group amount: 200.00 EUR (4000 MXN converted)
        // Fee add-on: 250 cents (2.50 EUR, already in group currency)
        val addOns = listOf(
            AddOn(
                type = AddOnType.FEE,
                mode = AddOnMode.ON_TOP,
                amountCents = 250,
                currency = "EUR",
                groupAmountCents = 250
            )
        )
        assertEquals(20250L, addOnService.calculateEffectiveGroupAmount(20000L, addOns))
    }

    @Test
    fun `calculateEffectiveGroupAmount scenario E3a - tip already included`() {
        // E3a: User entered 80 USD total with 10% tip included
        // Base cost: 80 / 1.10 = 72.73 USD → groupAmount = 6545 (at 0.9 rate)
        // Tip: 72.73 * 10% = 7.27 USD → groupAmountCents = 655
        // Effective reconstructs the original total: 6545 + 655 = 7200
        val addOns = listOf(
            AddOn(
                type = AddOnType.TIP,
                mode = AddOnMode.INCLUDED,
                amountCents = 727,
                groupAmountCents = 655
            )
        )
        assertEquals(7200L, addOnService.calculateEffectiveGroupAmount(6545L, addOns))
    }

    @Test
    fun `calculateEffectiveGroupAmount scenario E3b - tip on top`() {
        // E3b: 72 USD dinner + 10% tip = 7.20 USD → total 79.20 USD
        // groupAmount for base = 6480 (72 * 0.9), tip groupAmount = 648
        val addOns = listOf(
            AddOn(
                type = AddOnType.TIP,
                mode = AddOnMode.ON_TOP,
                amountCents = 720,
                groupAmountCents = 648
            )
        )
        assertEquals(7128L, addOnService.calculateEffectiveGroupAmount(6480L, addOns))
    }

    @Test
    fun `calculateEffectiveDeductedAmount returns base when no add-ons`() {
        assertEquals(27000L, addOnService.calculateEffectiveDeductedAmount(27000L, emptyList()))
    }

    @Test
    fun `calculateEffectiveDeductedAmount adds ATM fee`() {
        // E2: 5000 THB withdrawal, ATM charges 260 THB fee
        // deductedBaseAmount = 135.87 EUR (withdrawal), fee groupAmount = 7.06 EUR = 706 cents
        val addOns = listOf(
            AddOn(
                type = AddOnType.FEE,
                mode = AddOnMode.ON_TOP,
                amountCents = 26000, // 260 THB
                currency = "THB",
                groupAmountCents = 706
            )
        )
        assertEquals(14293L, addOnService.calculateEffectiveDeductedAmount(13587L, addOns))
    }

    @Test
    fun `calculateEffectiveDeductedAmount ignores INCLUDED add-ons`() {
        val addOns = listOf(
            AddOn(type = AddOnType.FEE, mode = AddOnMode.INCLUDED, groupAmountCents = 500)
        )
        assertEquals(27000L, addOnService.calculateEffectiveDeductedAmount(27000L, addOns))
    }

    // ── calculateIncludedBaseCost ───────────────────────────────────────

    @Test
    fun `calculateIncludedBaseCost returns total when no included amounts`() {
        assertEquals(
            8000L,
            addOnService.calculateIncludedBaseCost(8000L, 0L, BigDecimal.ZERO)
        )
    }

    @Test
    fun `calculateIncludedBaseCost subtracts exact included amount`() {
        // 80 EUR − 10 EUR included fee = 70 EUR base → 7000 cents
        assertEquals(
            7000L,
            addOnService.calculateIncludedBaseCost(8000L, 1000L, BigDecimal.ZERO)
        )
    }

    @Test
    fun `calculateIncludedBaseCost extracts percentage included amount`() {
        // 80 EUR includes 20% tip → base = 80 / 1.20 = 66.67 EUR → 6667 cents
        assertEquals(
            6667L,
            addOnService.calculateIncludedBaseCost(8000L, 0L, BigDecimal("20"))
        )
    }

    @Test
    fun `calculateIncludedBaseCost handles mixed exact and percentage`() {
        // 100 EUR − 5 EUR fee (exact) = 95 EUR, then 95 / 1.10 (10% tip) ≈ 86.36 → 8636 cents
        assertEquals(
            8636L,
            addOnService.calculateIncludedBaseCost(10000L, 500L, BigDecimal("10"))
        )
    }

    @Test
    fun `calculateIncludedBaseCost never returns negative`() {
        // Exact included exceeds total → coerced to 0
        assertEquals(
            0L,
            addOnService.calculateIncludedBaseCost(1000L, 5000L, BigDecimal.ZERO)
        )
    }

    @Test
    fun `calculateIncludedBaseCost with small percentage`() {
        // 50 EUR includes 5% surcharge → base = 50 / 1.05 = 47.62 → 4762 cents
        assertEquals(
            4762L,
            addOnService.calculateIncludedBaseCost(5000L, 0L, BigDecimal("5"))
        )
    }

    @Test
    fun `calculateIncludedBaseCost does not crash on minus 100 percent`() {
        // -100% → divisor = 0 → guarded, falls back to afterExact
        assertEquals(
            8000L,
            addOnService.calculateIncludedBaseCost(8000L, 0L, BigDecimal("-100"))
        )
    }

    @Test
    fun `calculateIncludedBaseCost does not crash on percentage below minus 100`() {
        // -200% → divisor = -1 → guarded, falls back to afterExact
        assertEquals(
            8000L,
            addOnService.calculateIncludedBaseCost(8000L, 0L, BigDecimal("-200"))
        )
    }

    // ── resolveAddOnAmountCents ─────────────────────────────────────────

    @Nested
    inner class ResolveAddOnAmountCents {

        @Test
        fun `EXACT converts decimal amount to cents`() {
            // 6.21 EUR → 621 cents
            val result = addOnService.resolveAddOnAmountCents(
                normalizedInput = BigDecimal("6.21"),
                valueType = AddOnValueType.EXACT,
                decimalDigits = 2,
                sourceAmountCents = 0L
            )
            assertEquals(621L, result)
        }

        @Test
        fun `EXACT handles zero-decimal currency`() {
            // 100 JPY → 100 cents (decimalDigits=0)
            val result = addOnService.resolveAddOnAmountCents(
                normalizedInput = BigDecimal("100"),
                valueType = AddOnValueType.EXACT,
                decimalDigits = 0,
                sourceAmountCents = 0L
            )
            assertEquals(100L, result)
        }

        @Test
        fun `EXACT rounds half-up`() {
            // 1.005 with 2 decimal digits → 1.005 * 100 = 100.5 → rounds to 101
            val result = addOnService.resolveAddOnAmountCents(
                normalizedInput = BigDecimal("1.005"),
                valueType = AddOnValueType.EXACT,
                decimalDigits = 2,
                sourceAmountCents = 0L
            )
            assertEquals(101L, result)
        }

        @Test
        fun `PERCENTAGE computes cents from source amount`() {
            // 10% of 6831 cents = 683.1 → rounds to 683
            val result = addOnService.resolveAddOnAmountCents(
                normalizedInput = BigDecimal("10"),
                valueType = AddOnValueType.PERCENTAGE,
                decimalDigits = 2,
                sourceAmountCents = 6831L
            )
            assertEquals(683L, result)
        }

        @Test
        fun `PERCENTAGE returns zero when sourceAmountCents is zero`() {
            val result = addOnService.resolveAddOnAmountCents(
                normalizedInput = BigDecimal("15"),
                valueType = AddOnValueType.PERCENTAGE,
                decimalDigits = 2,
                sourceAmountCents = 0L
            )
            assertEquals(0L, result)
        }

        @Test
        fun `PERCENTAGE returns zero when sourceAmountCents is negative`() {
            val result = addOnService.resolveAddOnAmountCents(
                normalizedInput = BigDecimal("10"),
                valueType = AddOnValueType.PERCENTAGE,
                decimalDigits = 2,
                sourceAmountCents = -500L
            )
            assertEquals(0L, result)
        }

        @Test
        fun `PERCENTAGE 50 percent of 10000 cents`() {
            val result = addOnService.resolveAddOnAmountCents(
                normalizedInput = BigDecimal("50"),
                valueType = AddOnValueType.PERCENTAGE,
                decimalDigits = 2,
                sourceAmountCents = 10000L
            )
            assertEquals(5000L, result)
        }
    }

    // ── computeProportionalAmount ───────────────────────────────────────

    @Nested
    inner class ComputeProportionalAmount {

        @Test
        fun `scales proportionally`() {
            // 5000 * 4000 / 10000 = 2000
            val result = service.computeProportionalAmount(
                amount = 5000L,
                targetAmount = 4000L,
                totalAmount = 10000L
            )
            assertEquals(2000L, result)
        }

        @Test
        fun `returns targetAmount when amount equals totalAmount`() {
            val result = service.computeProportionalAmount(
                amount = 10000L,
                targetAmount = 8000L,
                totalAmount = 10000L
            )
            assertEquals(8000L, result)
        }

        @Test
        fun `returns targetAmount when totalAmount is zero`() {
            val result = service.computeProportionalAmount(
                amount = 5000L,
                targetAmount = 3000L,
                totalAmount = 0L
            )
            assertEquals(3000L, result)
        }

        @Test
        fun `rounds half-up`() {
            // 3333 * 5000 / 10000 = 1666.5 → rounds to 1667
            val result = service.computeProportionalAmount(
                amount = 3333L,
                targetAmount = 5000L,
                totalAmount = 10000L
            )
            assertEquals(1667L, result)
        }

        @Test
        fun `handles single cent amount`() {
            // 1 * 5000 / 10000 = 0.5 → rounds to 1
            val result = service.computeProportionalAmount(
                amount = 1L,
                targetAmount = 5000L,
                totalAmount = 10000L
            )
            assertEquals(1L, result)
        }
    }

    // ── convertGroupToSourceCents ───────────────────────────────────────

    @Nested
    inner class ConvertGroupToSourceCents {

        @Test
        fun `converts with exchange rate of 1`() {
            val result = addOnService.convertGroupToSourceCents(
                groupAmountCents = 5000L,
                exchangeRate = BigDecimal.ONE
            )
            assertEquals(5000L, result)
        }

        @Test
        fun `converts with fractional exchange rate`() {
            // 1000 / 0.027 = 37037.037... → rounds to 37037
            val result = addOnService.convertGroupToSourceCents(
                groupAmountCents = 1000L,
                exchangeRate = BigDecimal("0.027")
            )
            assertEquals(37037L, result)
        }

        @Test
        fun `converts with exchange rate greater than 1`() {
            // 5000 / 2.5 = 2000
            val result = addOnService.convertGroupToSourceCents(
                groupAmountCents = 5000L,
                exchangeRate = BigDecimal("2.5")
            )
            assertEquals(2000L, result)
        }

        @Test
        fun `returns groupAmountCents when exchangeRate is zero`() {
            val result = addOnService.convertGroupToSourceCents(
                groupAmountCents = 5000L,
                exchangeRate = BigDecimal.ZERO
            )
            assertEquals(5000L, result)
        }

        @Test
        fun `rounds half-up`() {
            // 100 / 3 = 33.333... → rounds to 33
            val result = addOnService.convertGroupToSourceCents(
                groupAmountCents = 100L,
                exchangeRate = BigDecimal("3")
            )
            assertEquals(33L, result)
        }
    }

    // ── centsToBigDecimalString ───────────────────────────────────────────

    @Nested
    inner class CentsToBigDecimalString {

        @Test
        fun `converts 1550 cents with 2 decimal places to 15_50`() {
            assertEquals("15.50", service.centsToBigDecimalString(1550L, 2))
        }

        @Test
        fun `converts 0 cents to 0_00`() {
            assertEquals("0.00", service.centsToBigDecimalString(0L, 2))
        }

        @Test
        fun `converts 500 cents with 0 decimal places (JPY) to 500`() {
            assertEquals("500", service.centsToBigDecimalString(500L, 0))
        }

        @Test
        fun `converts 1234 cents with 3 decimal places (TND) to 1_234`() {
            assertEquals("1.234", service.centsToBigDecimalString(1234L, 3))
        }

        @Test
        fun `uses default 2 decimal places when not specified`() {
            assertEquals("25.00", service.centsToBigDecimalString(2500L))
        }
    }

    // ── sumPercentagesFromInputs ─────────────────────────────────────────

    @Nested
    inner class SumPercentagesFromInputs {

        @Test
        fun `sums valid percentage strings`() {
            val result = addOnService.sumPercentagesFromInputs(listOf("33.33", "33.33", "33.34"))
            assertEquals(BigDecimal("100.00"), result)
        }

        @Test
        fun `returns zero for empty list`() {
            assertEquals(BigDecimal.ZERO, addOnService.sumPercentagesFromInputs(emptyList()))
        }

        @Test
        fun `treats unparseable inputs as zero`() {
            val result = addOnService.sumPercentagesFromInputs(listOf("25", "abc", "25"))
            assertEquals(BigDecimal("50"), result)
        }

        @Test
        fun `handles blank and whitespace-only strings`() {
            val result = addOnService.sumPercentagesFromInputs(listOf("50", "  ", "50"))
            assertEquals(BigDecimal("100"), result)
        }

        @Test
        fun `handles single element`() {
            val result = addOnService.sumPercentagesFromInputs(listOf("42.5"))
            assertEquals(BigDecimal("42.5"), result)
        }
    }
}

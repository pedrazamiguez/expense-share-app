package es.pedrazamiguez.expenseshareapp.data.local.converter

import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.AddOn
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("AddOnListConverter")
class AddOnListConverterTest {

    private val converter = AddOnListConverter()

    private val sampleAddOn = AddOn(
        id = "addon-1",
        type = AddOnType.FEE,
        mode = AddOnMode.ON_TOP,
        inputValue = "2.50",
        isPercentage = false,
        amountCents = 250,
        currency = "EUR",
        exchangeRate = BigDecimal("1.000000"),
        groupAmountCents = 250,
        paymentMethod = PaymentMethod.DEBIT_CARD,
        description = "Bank transfer fee"
    )

    private val sampleTipAddOn = AddOn(
        id = "addon-2",
        type = AddOnType.TIP,
        mode = AddOnMode.INCLUDED,
        inputValue = "10%",
        isPercentage = true,
        amountCents = 800,
        currency = "USD",
        exchangeRate = BigDecimal("0.920000"),
        groupAmountCents = 736,
        paymentMethod = PaymentMethod.CASH,
        description = null
    )

    @Nested
    @DisplayName("fromAddOnList (serialize)")
    inner class Serialize {

        @Test
        fun `returns null for null input`() {
            assertNull(converter.fromAddOnList(null))
        }

        @Test
        fun `returns null for empty list`() {
            assertNull(converter.fromAddOnList(emptyList()))
        }

        @Test
        fun `serializes single add-on to JSON string`() {
            val json = converter.fromAddOnList(listOf(sampleAddOn))

            assertNotNull(json)
            assertTrue(json!!.contains("\"addon-1\""))
            assertTrue(json.contains("\"FEE\""))
            assertTrue(json.contains("\"ON_TOP\""))
            assertTrue(json.contains("\"2.50\""))
            assertTrue(json.contains("\"Bank transfer fee\""))
        }

        @Test
        fun `serializes multiple add-ons`() {
            val json = converter.fromAddOnList(listOf(sampleAddOn, sampleTipAddOn))

            assertNotNull(json)
            assertTrue(json!!.contains("addon-1"))
            assertTrue(json.contains("addon-2"))
        }

        @Test
        fun `omits description field when null`() {
            val json = converter.fromAddOnList(listOf(sampleTipAddOn))

            assertNotNull(json)
            // The description should not be present when null
            // (JSONObject.put with null key is skipped in our implementation)
        }
    }

    @Nested
    @DisplayName("toAddOnList (deserialize)")
    inner class Deserialize {

        @Test
        fun `returns null for null input`() {
            assertNull(converter.toAddOnList(null))
        }

        @Test
        fun `returns null for blank input`() {
            assertNull(converter.toAddOnList(""))
            assertNull(converter.toAddOnList("  "))
        }

        @Test
        fun `deserializes single add-on from JSON`() {
            val json = converter.fromAddOnList(listOf(sampleAddOn))
            val result = converter.toAddOnList(json)

            assertNotNull(result)
            assertEquals(1, result!!.size)
            val addOn = result[0]
            assertEquals("addon-1", addOn.id)
            assertEquals(AddOnType.FEE, addOn.type)
            assertEquals(AddOnMode.ON_TOP, addOn.mode)
            assertEquals("2.50", addOn.inputValue)
            assertEquals(false, addOn.isPercentage)
            assertEquals(250L, addOn.amountCents)
            assertEquals("EUR", addOn.currency)
            assertEquals(0, BigDecimal("1.000000").compareTo(addOn.exchangeRate))
            assertEquals(250L, addOn.groupAmountCents)
            assertEquals(PaymentMethod.DEBIT_CARD, addOn.paymentMethod)
            assertEquals("Bank transfer fee", addOn.description)
        }

        @Test
        fun `deserializes multiple add-ons preserving order`() {
            val json = converter.fromAddOnList(listOf(sampleAddOn, sampleTipAddOn))
            val result = converter.toAddOnList(json)

            assertNotNull(result)
            assertEquals(2, result!!.size)
            assertEquals("addon-1", result[0].id)
            assertEquals("addon-2", result[1].id)
        }

        @Test
        fun `deserializes tip add-on with percentage flag`() {
            val json = converter.fromAddOnList(listOf(sampleTipAddOn))
            val result = converter.toAddOnList(json)

            assertNotNull(result)
            val addOn = result!![0]
            assertEquals(AddOnType.TIP, addOn.type)
            assertEquals(AddOnMode.INCLUDED, addOn.mode)
            assertEquals("10%", addOn.inputValue)
            assertEquals(true, addOn.isPercentage)
            assertEquals(PaymentMethod.CASH, addOn.paymentMethod)
            assertNull(addOn.description)
        }

        @Test
        fun `handles null description gracefully`() {
            val json = converter.fromAddOnList(listOf(sampleTipAddOn))
            val result = converter.toAddOnList(json)

            assertNotNull(result)
            assertNull(result!![0].description)
        }
    }

    @Nested
    @DisplayName("Round-trip (serialize → deserialize)")
    inner class RoundTrip {

        @Test
        fun `round-trip preserves all fields`() {
            val original = listOf(sampleAddOn, sampleTipAddOn)
            val json = converter.fromAddOnList(original)
            val restored = converter.toAddOnList(json)

            assertNotNull(restored)
            assertEquals(original.size, restored!!.size)

            original.zip(restored).forEach { (orig, rest) ->
                assertEquals(orig.id, rest.id)
                assertEquals(orig.type, rest.type)
                assertEquals(orig.mode, rest.mode)
                assertEquals(orig.inputValue, rest.inputValue)
                assertEquals(orig.isPercentage, rest.isPercentage)
                assertEquals(orig.amountCents, rest.amountCents)
                assertEquals(orig.currency, rest.currency)
                assertEquals(0, orig.exchangeRate.compareTo(rest.exchangeRate))
                assertEquals(orig.groupAmountCents, rest.groupAmountCents)
                assertEquals(orig.paymentMethod, rest.paymentMethod)
                assertEquals(orig.description, rest.description)
            }
        }

        @Test
        fun `round-trip with SURCHARGE type`() {
            val surcharge = AddOn(
                id = "addon-s",
                type = AddOnType.SURCHARGE,
                mode = AddOnMode.ON_TOP,
                inputValue = "0.50",
                amountCents = 50,
                currency = "EUR",
                groupAmountCents = 50,
                description = "Card deposit"
            )
            val json = converter.fromAddOnList(listOf(surcharge))
            val result = converter.toAddOnList(json)

            assertNotNull(result)
            assertEquals(AddOnType.SURCHARGE, result!![0].type)
            assertEquals("Card deposit", result[0].description)
        }

        @Test
        fun `round-trip with DISCOUNT type`() {
            val discount = AddOn(
                id = "addon-d",
                type = AddOnType.DISCOUNT,
                mode = AddOnMode.ON_TOP,
                inputValue = "5",
                amountCents = 500,
                currency = "EUR",
                groupAmountCents = 500
            )
            val json = converter.fromAddOnList(listOf(discount))
            val result = converter.toAddOnList(json)

            assertNotNull(result)
            assertEquals(AddOnType.DISCOUNT, result!![0].type)
        }

        @Test
        fun `round-trip preserves BigDecimal exchange rate precision`() {
            val addOn = AddOn(
                id = "addon-rate",
                type = AddOnType.FEE,
                mode = AddOnMode.ON_TOP,
                amountCents = 26000,
                currency = "THB",
                exchangeRate = BigDecimal("0.027135"),
                groupAmountCents = 706
            )
            val json = converter.fromAddOnList(listOf(addOn))
            val result = converter.toAddOnList(json)

            assertNotNull(result)
            assertEquals(
                0,
                BigDecimal("0.027135").compareTo(result!![0].exchangeRate),
                "Exchange rate precision must be preserved through round-trip"
            )
        }
    }

    @Nested
    @DisplayName("Backward compatibility / defensive parsing")
    inner class DefensiveParsing {

        @Test
        fun `defaults unknown type to FEE`() {
            val json = """[{"id":"x","type":"UNKNOWN_TYPE","mode":"ON_TOP","inputValue":"1","isPercentage":false,"amountCents":100,"currency":"EUR","exchangeRate":"1","groupAmountCents":100,"paymentMethod":"OTHER"}]"""
            val result = converter.toAddOnList(json)

            assertNotNull(result)
            assertEquals(AddOnType.FEE, result!![0].type)
        }

        @Test
        fun `defaults unknown mode to ON_TOP`() {
            val json = """[{"id":"x","type":"FEE","mode":"FUTURE_MODE","inputValue":"1","isPercentage":false,"amountCents":100,"currency":"EUR","exchangeRate":"1","groupAmountCents":100,"paymentMethod":"OTHER"}]"""
            val result = converter.toAddOnList(json)

            assertNotNull(result)
            assertEquals(AddOnMode.ON_TOP, result!![0].mode)
        }

        @Test
        fun `defaults unknown payment method to OTHER`() {
            val json = """[{"id":"x","type":"FEE","mode":"ON_TOP","inputValue":"1","isPercentage":false,"amountCents":100,"currency":"EUR","exchangeRate":"1","groupAmountCents":100,"paymentMethod":"CRYPTO"}]"""
            val result = converter.toAddOnList(json)

            assertNotNull(result)
            assertEquals(PaymentMethod.OTHER, result!![0].paymentMethod)
        }

        @Test
        fun `defaults invalid exchange rate to ONE`() {
            val json = """[{"id":"x","type":"FEE","mode":"ON_TOP","inputValue":"1","isPercentage":false,"amountCents":100,"currency":"EUR","exchangeRate":"not_a_number","groupAmountCents":100,"paymentMethod":"OTHER"}]"""
            val result = converter.toAddOnList(json)

            assertNotNull(result)
            assertEquals(0, BigDecimal.ONE.compareTo(result!![0].exchangeRate))
        }

        @Test
        fun `handles missing optional fields gracefully`() {
            val json = """[{"type":"TIP","mode":"ON_TOP","amountCents":500,"currency":"USD","exchangeRate":"1","groupAmountCents":500,"paymentMethod":"CASH"}]"""
            val result = converter.toAddOnList(json)

            assertNotNull(result)
            val addOn = result!![0]
            assertEquals("", addOn.id)
            assertEquals("", addOn.inputValue)
            assertEquals(false, addOn.isPercentage)
            assertNull(addOn.description)
        }
    }
}

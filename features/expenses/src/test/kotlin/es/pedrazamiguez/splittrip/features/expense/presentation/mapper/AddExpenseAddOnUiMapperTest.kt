package es.pedrazamiguez.splittrip.features.expense.presentation.mapper

import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.splittrip.domain.enums.AddOnMode
import es.pedrazamiguez.splittrip.domain.enums.AddOnType
import es.pedrazamiguez.splittrip.domain.enums.AddOnValueType
import es.pedrazamiguez.splittrip.features.expense.presentation.model.AddOnUiModel
import es.pedrazamiguez.splittrip.features.expense.presentation.model.PaymentMethodUiModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddExpenseAddOnUiMapperTest {

    private lateinit var mapper: AddExpenseAddOnUiMapper
    private val eurUi = CurrencyUiModel(code = "EUR", displayText = "EUR (€)", decimalDigits = 2)
    private val cashPaymentMethod = PaymentMethodUiModel(id = "CASH", displayText = "Cash")

    @BeforeEach
    fun setup() {
        mapper = AddExpenseAddOnUiMapper()
    }

    @Test
    fun `mapAddOnsToDomain_trimsDescriptionBeforePersistence`() {
        val addOns = listOf(
            AddOnUiModel(
                id = "addon-1",
                type = AddOnType.TIP,
                mode = AddOnMode.ON_TOP,
                valueType = AddOnValueType.EXACT,
                resolvedAmountCents = 200,
                groupAmountCents = 200,
                currency = eurUi,
                paymentMethod = cashPaymentMethod,
                description = "  Tip  "
            )
        )

        val result = mapper.mapAddOnsToDomain(addOns, "EUR")

        assertEquals(1, result.size)
        assertEquals("Tip", result[0].description)
    }

    @Test
    fun `mapAddOnsToDomain_treatsWhitespaceOnlyDescriptionAsNull`() {
        val addOns = listOf(
            AddOnUiModel(
                id = "addon-2",
                type = AddOnType.FEE,
                mode = AddOnMode.ON_TOP,
                valueType = AddOnValueType.EXACT,
                resolvedAmountCents = 150,
                groupAmountCents = 150,
                currency = eurUi,
                paymentMethod = cashPaymentMethod,
                description = "   "
            )
        )

        val result = mapper.mapAddOnsToDomain(addOns, "EUR")

        assertEquals(1, result.size)
        assertNull(result[0].description)
    }
}

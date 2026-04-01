package es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AddCashWithdrawalUiMapperTest {

    private lateinit var resourceProvider: ResourceProvider
    private lateinit var mapper: AddCashWithdrawalUiMapper

    @BeforeEach
    fun setUp() {
        resourceProvider = mockk(relaxed = true)
        mapper = AddCashWithdrawalUiMapper(resourceProvider)
    }

    @Nested
    inner class MapCurrency {

        @Test
        fun `maps single currency to CurrencyUiModel`() {
            val currency = Currency(
                code = "EUR",
                symbol = "€",
                defaultName = "Euro",
                decimalDigits = 2
            )

            val result = mapper.mapCurrency(currency)

            assertEquals("EUR", result.code)
            assertEquals(2, result.decimalDigits)
        }

        @Test
        fun `maps currency list to ImmutableList of CurrencyUiModel`() {
            val currencies = listOf(
                Currency(code = "EUR", symbol = "€", defaultName = "Euro", decimalDigits = 2),
                Currency(code = "JPY", symbol = "¥", defaultName = "Japanese Yen", decimalDigits = 0)
            )

            val result = mapper.mapCurrencies(currencies)

            assertEquals(2, result.size)
            assertEquals("EUR", result[0].code)
            assertEquals("JPY", result[1].code)
            assertEquals(0, result[1].decimalDigits)
        }
    }

    @Nested
    inner class LabelBuilding {

        private val eurModel = CurrencyUiModel(code = "EUR", displayText = "EUR (€)", decimalDigits = 2)
        private val thbModel = CurrencyUiModel(code = "THB", displayText = "THB (฿)", decimalDigits = 2)

        @Test
        fun `buildExchangeRateLabel delegates to resource provider`() {
            every { resourceProvider.getString(any(), any(), any()) } returns "1 EUR (€) = ? THB (฿)"

            val result = mapper.buildExchangeRateLabel(eurModel, thbModel)

            assertEquals("1 EUR (€) = ? THB (฿)", result)
        }

        @Test
        fun `buildDeductedAmountLabel delegates to resource provider`() {
            every { resourceProvider.getString(any(), any()) } returns "Deducted in EUR (€)"

            val result = mapper.buildDeductedAmountLabel(eurModel)

            assertEquals("Deducted in EUR (€)", result)
        }

        @Test
        fun `buildFeeConvertedLabel delegates to resource provider`() {
            every { resourceProvider.getString(any(), any()) } returns "Fee converted to EUR (€)"

            val result = mapper.buildFeeConvertedLabel(eurModel)

            assertEquals("Fee converted to EUR (€)", result)
        }
    }
}

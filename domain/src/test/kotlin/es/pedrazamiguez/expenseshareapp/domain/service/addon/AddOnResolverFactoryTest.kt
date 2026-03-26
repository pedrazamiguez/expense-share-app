package es.pedrazamiguez.expenseshareapp.domain.service.addon

import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AddOnResolverFactoryTest {

    private val factory = AddOnResolverFactory()

    @Test
    fun `creates ExactAddOnResolver for EXACT value type`() {
        val resolver = factory.create(AddOnValueType.EXACT)
        assertTrue(resolver is ExactAddOnResolver)
    }

    @Test
    fun `creates PercentageAddOnResolver for PERCENTAGE value type`() {
        val resolver = factory.create(AddOnValueType.PERCENTAGE)
        assertTrue(resolver is PercentageAddOnResolver)
    }
}

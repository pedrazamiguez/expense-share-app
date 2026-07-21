package es.pedrazamiguez.splittrip.features.balance.presentation.screen.impl

import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BalancesScreenUiProviderImplTest {

    private val uiProvider = BalancesScreenUiProviderImpl()

    @Test
    fun `route matches Routes BALANCES`() {
        assertEquals(Routes.BALANCES, uiProvider.route)
    }

    @Test
    fun `provider configuration has expected route`() {
        val customRouteProvider = BalancesScreenUiProviderImpl(route = "custom_route")
        assertEquals("custom_route", customRouteProvider.route)
    }

    @Test
    fun `mainAction sharedTransitionKey matches MY_POSITION key`() {
        assertEquals(
            es.pedrazamiguez.splittrip.core.designsystem.navigation.SharedElementKeys.MY_POSITION,
            "my_position_container"
        )
    }
}

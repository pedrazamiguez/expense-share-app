package es.pedrazamiguez.splittrip.features.contribution.presentation.screen.impl

import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ContributionDetailScreenUiProviderImplTest {

    private val uiProvider = ContributionDetailScreenUiProviderImpl()

    @Test
    fun `route matches Routes CONTRIBUTION_DETAIL`() {
        assertEquals(Routes.CONTRIBUTION_DETAIL, uiProvider.route)
    }

    @Test
    fun `provider configuration has expected route`() {
        val customRouteProvider = ContributionDetailScreenUiProviderImpl(route = "custom_route")
        assertEquals("custom_route", customRouteProvider.route)
    }
}

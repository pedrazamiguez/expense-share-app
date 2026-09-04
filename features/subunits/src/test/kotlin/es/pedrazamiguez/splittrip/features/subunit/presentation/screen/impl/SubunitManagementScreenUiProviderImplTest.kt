package es.pedrazamiguez.splittrip.features.subunit.presentation.screen.impl

import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.domain.service.featuregate.FeatureGateService
import es.pedrazamiguez.splittrip.domain.usecase.group.ObserveSelectedGroupUseCase
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SubunitManagementScreenUiProviderImplTest {

    private val observeSelectedGroupUseCase: ObserveSelectedGroupUseCase = mockk()
    private val featureGateService: FeatureGateService = mockk()

    private val uiProvider = SubunitManagementScreenUiProviderImpl(
        observeSelectedGroupUseCase = observeSelectedGroupUseCase,
        featureGateService = featureGateService
    )

    @Test
    fun `route matches Routes MANAGE_SUBUNITS`() {
        assertEquals(Routes.MANAGE_SUBUNITS, uiProvider.route)
    }

    @Test
    fun `provider configuration has expected custom route`() {
        val customRouteProvider = SubunitManagementScreenUiProviderImpl(
            observeSelectedGroupUseCase = observeSelectedGroupUseCase,
            featureGateService = featureGateService,
            route = "custom_route"
        )
        assertEquals("custom_route", customRouteProvider.route)
    }
}

package es.pedrazamiguez.splittrip.features.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.features.onboarding.presentation.feature.OnboardingFeature

fun NavGraphBuilder.onboardingGraph(onOnboardingComplete: () -> Unit) {
    composable(Routes.ONBOARDING) {
        OnboardingFeature(
            onOnboardingComplete = onOnboardingComplete
        )
    }
}

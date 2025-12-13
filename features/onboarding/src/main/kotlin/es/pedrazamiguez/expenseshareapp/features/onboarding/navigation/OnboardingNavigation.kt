package es.pedrazamiguez.expenseshareapp.features.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.onboarding.presentation.feature.OnboardingFeature

fun NavGraphBuilder.onboardingGraph(onOnboardingComplete: () -> Unit) {
    composable(Routes.ONBOARDING) {
        OnboardingFeature(
            onOnboardingComplete = onOnboardingComplete
        )
    }
}

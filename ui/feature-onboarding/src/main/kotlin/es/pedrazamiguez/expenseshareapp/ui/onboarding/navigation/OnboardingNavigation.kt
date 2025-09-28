package es.pedrazamiguez.expenseshareapp.ui.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.onboarding.presentation.feature.OnboardingFeature

fun NavGraphBuilder.onboardingGraph(onOnboardingComplete: () -> Unit) {
    composable(Routes.ONBOARDING) {
        OnboardingFeature(
            onOnboardingComplete = onOnboardingComplete
        )
    }
}

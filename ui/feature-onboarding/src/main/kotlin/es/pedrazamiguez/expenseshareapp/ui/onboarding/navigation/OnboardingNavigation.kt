package es.pedrazamiguez.expenseshareapp.ui.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.ui.onboarding.presentation.feature.OnboardingFeature

const val ONBOARDING_ROUTE = "onboarding"

fun NavGraphBuilder.onboardingGraph(onOnboardingComplete: () -> Unit) {
    composable(ONBOARDING_ROUTE) {
        OnboardingFeature(
            onOnboardingComplete = onOnboardingComplete
        )
    }
}

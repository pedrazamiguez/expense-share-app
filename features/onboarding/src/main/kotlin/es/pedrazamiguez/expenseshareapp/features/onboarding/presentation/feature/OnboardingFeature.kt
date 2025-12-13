package es.pedrazamiguez.expenseshareapp.features.onboarding.presentation.feature

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.features.onboarding.presentation.screen.OnboardingScreen

@Composable
fun OnboardingFeature(onOnboardingComplete: () -> Unit = {}) {
    OnboardingScreen(onOnboardingComplete = onOnboardingComplete)
}

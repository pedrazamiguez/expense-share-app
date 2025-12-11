package es.pedrazamiguez.expenseshareapp.ui.onboarding.presentation.feature

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.ui.onboarding.presentation.screen.OnboardingScreen

@Composable
fun OnboardingFeature(onOnboardingComplete: () -> Unit = {}) {
    OnboardingScreen(onOnboardingComplete = onOnboardingComplete)
}

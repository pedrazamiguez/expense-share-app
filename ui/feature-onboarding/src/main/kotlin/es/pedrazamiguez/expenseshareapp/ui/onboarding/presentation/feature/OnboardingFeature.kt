package es.pedrazamiguez.expenseshareapp.ui.onboarding.presentation.feature

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun OnboardingFeature(onOnboardingComplete: () -> Unit = {}) {
    Button(onClick = {
        onOnboardingComplete()
    }) {
        Text("Complete Onboarding")
    }
}

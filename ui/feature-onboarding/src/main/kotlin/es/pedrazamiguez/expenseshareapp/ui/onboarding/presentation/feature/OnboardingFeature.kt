package es.pedrazamiguez.expenseshareapp.ui.onboarding.presentation.feature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder

@Composable
fun OnboardingFeature(onOnboardingComplete: () -> Unit = {}) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = {
            onOnboardingComplete()
        }) {
            Text("Complete Onboarding".placeholder)
        }
    }

}

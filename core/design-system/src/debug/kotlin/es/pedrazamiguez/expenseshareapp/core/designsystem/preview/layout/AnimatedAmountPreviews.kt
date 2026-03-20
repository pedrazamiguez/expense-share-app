package es.pedrazamiguez.expenseshareapp.core.designsystem.preview.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.AnimatedAmount
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemes

@PreviewThemes
@Composable
private fun AnimatedAmountStaticPreview() {
    PreviewThemeWrapper {
        Column(modifier = Modifier.padding(16.dp)) {
            AnimatedAmount(
                formattedAmount = "€1,250.00",
                shouldAnimate = false,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@PreviewThemes
@Composable
private fun AnimatedAmountAnimatingPreview() {
    PreviewThemeWrapper {
        Column(modifier = Modifier.padding(16.dp)) {
            AnimatedAmount(
                formattedAmount = "€1,250.00",
                shouldAnimate = true,
                previousAmount = "€980.50",
                rollingUp = true,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@PreviewThemes
@Composable
private fun AnimatedAmountRollingDownPreview() {
    PreviewThemeWrapper {
        Column(modifier = Modifier.padding(16.dp)) {
            AnimatedAmount(
                formattedAmount = "€320.75",
                shouldAnimate = true,
                previousAmount = "€1,250.00",
                rollingUp = false,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

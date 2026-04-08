package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.GradientButton

private val WIZARD_BUTTON_HEIGHT = 56.dp
private val WIZARD_NEXT_ELEVATION = 4.dp
private const val DISABLED_CONTAINER_ALPHA = 0.12f
private const val DISABLED_CONTENT_ALPHA = 0.38f
private const val SHADOW_ALPHA = 0.28f

/**
 * Navigation bar for a multi-step wizard.
 *
 * Domain-agnostic: all configuration is passed via [WizardNavigationBarConfig] so this
 * component can be reused across any feature (AddCashWithdrawal, AddExpense, etc.).
 *
 * Adapts automatically between Back/Next and Back/Submit on the last step.
 * Positioned above the scrollable step content so it is never hidden by the keyboard.
 *
 * @param config    Combined state and labels for the navigation bar.
 * @param onBack    Called when the Back button is tapped.
 * @param onNext    Called when the Next button is tapped.
 * @param onSubmit  Called when the Submit button is tapped.
 */
@Composable
fun WizardNavigationBar(
    config: WizardNavigationBarConfig,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WizardBackButton(
                label = config.backLabel,
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            WizardForwardButton(
                config = config,
                onNext = onNext,
                onSubmit = onSubmit,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WizardBackButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(WIZARD_BUTTON_HEIGHT),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTAINER_ALPHA),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WizardForwardButton(
    config: WizardNavigationBarConfig,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (config.isOnLastStep) {
        GradientButton(
            text = config.submitLabel,
            onClick = onSubmit,
            enabled = config.isCurrentStepValid,
            isLoading = config.isLoading,
            modifier = modifier
        )
    } else {
        WizardNextButton(
            label = config.nextLabel,
            onClick = onNext,
            enabled = config.isCurrentStepValid && config.canGoNext,
            modifier = modifier
        )
    }
}

@Composable
private fun WizardNextButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    val shadowElevation = if (enabled) WIZARD_NEXT_ELEVATION else 0.dp
    val shadowColor = primary.copy(alpha = SHADOW_ALPHA)

    val backgroundModifier = if (enabled) {
        Modifier.background(
            brush = Brush.linearGradient(colors = listOf(primary, primaryContainer)),
            shape = CircleShape
        )
    } else {
        Modifier
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .height(WIZARD_BUTTON_HEIGHT)
            .shadow(
                elevation = shadowElevation,
                shape = CircleShape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .then(backgroundModifier),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTAINER_ALPHA),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}

package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalBottomPadding

/**
 * Bottom navigation bar for a multi-step wizard.
 *
 * Domain-agnostic: all configuration is passed via [WizardNavigationBarConfig] so this
 * component can be reused across any feature (AddCashWithdrawal, AddExpense, etc.).
 *
 * Adapts automatically between Back/Next and Back/Submit on the last step.
 * Also respects [LocalBottomPadding] so the bar clears the floating bottom nav,
 * and collapses that extra padding when the IME is visible.
 *
 * @param config    Combined state and labels for the navigation bar.
 * @param onBack    Called when the Back button is tapped.
 * @param onNext    Called when the Next button is tapped.
 * @param onSubmit  Called when the Submit button is tapped.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WizardNavigationBar(
    config: WizardNavigationBarConfig,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomNavPadding = LocalBottomPadding.current
    val isKeyboardVisible = WindowInsets.isImeVisible
    val effectiveBottomPadding = if (isKeyboardVisible) 12.dp else 12.dp + bottomNavPadding

    Surface(
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = effectiveBottomPadding),
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
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = MaterialTheme.shapes.large
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
        Button(
            onClick = onSubmit,
            modifier = modifier.height(56.dp),
            enabled = config.isCurrentStepValid && !config.isLoading,
            shape = MaterialTheme.shapes.large
        ) {
            if (config.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = config.submitLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        Button(
            onClick = onNext,
            modifier = modifier.height(56.dp),
            enabled = config.isCurrentStepValid && config.canGoNext,
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = config.nextLabel,
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
}

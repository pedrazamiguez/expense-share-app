package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ArrowLeft
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ArrowRight
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.GradientButton
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.SecondaryButton

/**
 * Navigation bar for a multi-step wizard.
 *
 * Domain-agnostic: all configuration is passed via [WizardNavigationBarConfig] so this
 * component can be reused across any feature (AddCashWithdrawal, AddExpense, etc.).
 *
 * Adapts automatically between Back/Next and Back/Submit on the last step.
 * Positioned above the scrollable step content so it is never hidden by the keyboard.
 *
 * All buttons delegate to the design-system's canonical button components
 * ([SecondaryButton], [GradientButton]) which provide consistent height, pill shape,
 * and platform shadow across the app.
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
            SecondaryButton(
                text = config.backLabel,
                onClick = onBack,
                leadingIcon = TablerIcons.Outline.ArrowLeft,
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
        GradientButton(
            text = config.nextLabel,
            onClick = onNext,
            enabled = config.isCurrentStepValid && config.canGoNext,
            trailingIcon = TablerIcons.Outline.ArrowRight,
            modifier = modifier
        )
    }
}

package es.pedrazamiguez.splittrip.features.group.presentation.component.step.leave

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.BodyText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveCashResolutionUiModel

@Composable
fun LeaveCashResolutionStep(
    cashResolution: LeaveCashResolutionUiModel,
    modifier: Modifier = Modifier
) {
    WizardStepLayout(modifier = modifier) {
        Text(
            text = stringResource(R.string.leave_wizard_cash_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        BodyText(
            text = stringResource(R.string.leave_wizard_cash_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlatCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.Medium)
            ) {
                val text = when {
                    cashResolution.requiresDeposit ->
                        stringResource(R.string.leave_wizard_cash_deposit, cashResolution.formattedAmount)
                    cashResolution.requiresReimbursement ->
                        stringResource(R.string.leave_wizard_cash_reimbursement, cashResolution.formattedAmount)
                    else -> stringResource(R.string.leave_wizard_cash_subtitle)
                }
                BodyText(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

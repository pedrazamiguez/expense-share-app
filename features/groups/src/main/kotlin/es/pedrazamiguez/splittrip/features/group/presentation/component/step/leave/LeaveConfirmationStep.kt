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
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.SectionCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.BodyText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveSubunitImpactUiModel

@Composable
fun LeaveConfirmationStep(
    groupName: String,
    subunitImpact: LeaveSubunitImpactUiModel,
    modifier: Modifier = Modifier
) {
    WizardStepLayout(modifier = modifier) {
        Text(
            text = stringResource(R.string.leave_wizard_confirm_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        BodyText(
            text = stringResource(R.string.leave_wizard_confirm_subtitle, groupName),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SectionCard(
            title = stringResource(R.string.leave_wizard_subunit_impact_title),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(top = MaterialTheme.spacing.Small)) {
                BodyText(
                    text = subunitImpact.message,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

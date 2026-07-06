package es.pedrazamiguez.splittrip.features.group.presentation.component.step.leave

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.FormErrorBanner
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.BodyText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.component.leave.LeaveSettlementItemCard
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveSettlementUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
fun LeaveSettlementStep(
    settlements: ImmutableList<LeaveSettlementUiModel>,
    onConfirmSettlement: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasUnresolved = settlements.any { !it.isConfirmed }

    WizardStepLayout(modifier = modifier) {
        Text(
            text = stringResource(R.string.leave_wizard_settlement_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        BodyText(
            text = stringResource(R.string.leave_wizard_settlement_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (hasUnresolved) {
            FormErrorBanner(
                error = UiText.StringResource(R.string.leave_wizard_settlement_warning_banner)
            )
        }

        if (settlements.isEmpty()) {
            BodyText(
                text = stringResource(R.string.leave_wizard_settlement_none),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
            ) {
                settlements.forEach { settlement ->
                    LeaveSettlementItemCard(
                        settlement = settlement,
                        onConfirmClicked = onConfirmSettlement
                    )
                }
            }
        }
    }
}

package es.pedrazamiguez.splittrip.features.group.presentation.component.step.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.BodyText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.component.GroupSettlementItem
import es.pedrazamiguez.splittrip.features.group.presentation.model.SettlementRowUiModel
import kotlinx.collections.immutable.ImmutableList

@Suppress("LongMethod")
@Composable
fun ArchiveActionStep(
    pendingSettlements: ImmutableList<SettlementRowUiModel>,
    onConfirmSettlement: (String) -> Unit,
    onDisputeSettlement: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val yourActionRequired = pendingSettlements.filter { it.canCurrentUserConfirm }
    val waitingOnOthers = pendingSettlements.filter { !it.canCurrentUserConfirm }

    WizardStepLayout(modifier = modifier) {
        Text(
            text = stringResource(R.string.archive_wizard_action_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        BodyText(
            text = stringResource(R.string.archive_wizard_action_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (yourActionRequired.isNotEmpty()) {
            Text(
                text = stringResource(R.string.leave_wizard_settlement_section_your_action),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
            ) {
                yourActionRequired.forEach { settlement ->
                    GroupSettlementItem(
                        settlement = settlement,
                        onConfirm = { onConfirmSettlement(settlement.settlementId) },
                        onDispute = { onDisputeSettlement(settlement.settlementId) }
                    )
                }
            }
        }

        if (waitingOnOthers.isNotEmpty()) {
            Text(
                text = stringResource(R.string.leave_wizard_settlement_section_waiting),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
            ) {
                waitingOnOthers.forEach { settlement ->
                    GroupSettlementItem(
                        settlement = settlement,
                        onConfirm = { onConfirmSettlement(settlement.settlementId) },
                        onDispute = { onDisputeSettlement(settlement.settlementId) }
                    )
                }
            }
        }
    }
}

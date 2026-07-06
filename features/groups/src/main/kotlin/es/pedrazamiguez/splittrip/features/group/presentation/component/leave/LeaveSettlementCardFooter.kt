package es.pedrazamiguez.splittrip.features.group.presentation.component.leave

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.SecondaryButton
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveSettlementStatusType

@Composable
fun LeaveSettlementCardFooter(
    formattedAmount: String,
    canCurrentUserConfirm: Boolean,
    statusType: LeaveSettlementStatusType,
    statusLabel: String,
    settlementId: String,
    onConfirmClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formattedAmount,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (canCurrentUserConfirm) {
            SecondaryButton(
                text = stringResource(R.string.leave_wizard_confirm_settlement),
                onClick = { onConfirmClicked(settlementId) }
            )
        } else {
            val (statusBg, statusFg) = getStatusColors(statusType, MaterialTheme.colorScheme)
            Surface(
                color = statusBg,
                contentColor = statusFg,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun getStatusColors(
    statusType: LeaveSettlementStatusType,
    colorScheme: ColorScheme
) = when (statusType) {
    LeaveSettlementStatusType.CONFIRMED ->
        colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer
    LeaveSettlementStatusType.ACTION_REQUIRED_BY_USER ->
        colorScheme.primaryContainer to colorScheme.onPrimaryContainer
    LeaveSettlementStatusType.WAITING_FOR_OTHER ->
        colorScheme.surfaceVariant to colorScheme.onSurfaceVariant
}

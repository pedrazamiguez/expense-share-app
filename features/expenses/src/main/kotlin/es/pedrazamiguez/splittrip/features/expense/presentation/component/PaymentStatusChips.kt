package es.pedrazamiguez.splittrip.features.expense.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.chip.PassportChip
import es.pedrazamiguez.splittrip.features.expense.presentation.model.PaymentStatusUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentStatusChips(
    paymentStatuses: List<PaymentStatusUiModel>,
    selectedPaymentStatus: PaymentStatusUiModel?,
    onPaymentStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        paymentStatuses.forEach { status ->
            val isSelected = selectedPaymentStatus?.id == status.id
            PassportChip(
                label = status.displayText,
                selected = isSelected,
                onClick = { onPaymentStatusSelected(status.id) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else {
                    null
                }
            )
        }
    }
}

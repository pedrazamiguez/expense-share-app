package es.pedrazamiguez.splittrip.features.expense.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.features.expense.presentation.model.PaymentMethodUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentMethodChips(
    paymentMethods: List<PaymentMethodUiModel>,
    selectedPaymentMethod: PaymentMethodUiModel?,
    onPaymentMethodSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        paymentMethods.forEach { method ->
            val isSelected = selectedPaymentMethod?.id == method.id
            FilterChip(
                selected = isSelected,
                onClick = { onPaymentMethodSelected(method.id) },
                label = {
                    Text(
                        text = method.displayText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, null) }
                } else {
                    null
                }
            )
        }
    }
}

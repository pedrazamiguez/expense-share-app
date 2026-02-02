package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions.toStringRes

@Composable
fun PaymentMethodChips(
    paymentMethods: List<PaymentMethod>,
    selectedPaymentMethod: PaymentMethod,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    // Simple wrapping: split into rows if needed
    val chunked = paymentMethods.chunked(3)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chunked.forEach { rowMethods ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowMethods.forEach { method ->
                    FilterChip(
                        selected = selectedPaymentMethod == method,
                        onClick = { onPaymentMethodSelected(method) },
                        label = { Text(stringResource(method.toStringRes())) },
                        leadingIcon = if (selectedPaymentMethod == method) {
                            { Icon(Icons.Default.Check, null) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Add empty spacers if row has fewer than 3 items
                repeat(3 - rowMethods.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

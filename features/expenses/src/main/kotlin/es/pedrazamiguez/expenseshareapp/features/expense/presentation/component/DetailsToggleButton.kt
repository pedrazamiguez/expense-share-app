package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.expense.R

/**
 * Toggle button for the progressive disclosure section.
 * Switches between "More details" and "Less details" with an expand/collapse icon.
 */
@Composable
internal fun DetailsToggleButton(
    showDetails: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = if (showDetails) {
                stringResource(R.string.add_expense_less_details)
            } else {
                stringResource(R.string.add_expense_more_details)
            },
            style = MaterialTheme.typography.labelLarge
        )
    }
}

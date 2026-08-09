package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.sheet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.R
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.LabelText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.CashBreakdownItemUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun CashBreakdownItemList(
    breakdown: ImmutableList<CashBreakdownItemUiModel>,
    modifier: Modifier = Modifier
) {
    var lastScopeLabel = ""
    breakdown.forEach { item ->
        // Render a section header whenever the scope group changes
        if (item.scopeLabel != lastScopeLabel) {
            if (lastScopeLabel.isNotEmpty()) Spacer(Modifier.height(MaterialTheme.spacing.Default))
            LabelText(
                text = item.scopeLabel,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.ExtraSmall)
            )
            if (item.isEstimatedShare) {
                CaptionText(
                    text = stringResource(R.string.core_cash_breakdown_estimated_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = Int.MAX_VALUE,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            lastScopeLabel = item.scopeLabel
        }
        CashBreakdownEntryRow(item = item, modifier = modifier)
    }
}

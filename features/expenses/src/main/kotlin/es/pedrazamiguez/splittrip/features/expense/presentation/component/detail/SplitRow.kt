package es.pedrazamiguez.splittrip.features.expense.presentation.component.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.AmountText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.MemberDisplay
import es.pedrazamiguez.splittrip.features.expense.R
import es.pedrazamiguez.splittrip.features.expense.presentation.model.SplitDetailUiModel

private val CHEVRON_SIZE = 18.dp

@Composable
internal fun SplitRow(split: SplitDetailUiModel) {
    val isFormer = split.memberDisplay is MemberDisplay.Former
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isFormer) Modifier.alpha(0.6f) else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
            ) {
                Text(
                    text = if (split.isCurrentUser) {
                        stringResource(R.string.expense_detail_split_you_badge)
                    } else {
                        split.displayName
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isFormer) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text(
                            text = stringResource(
                                es.pedrazamiguez.splittrip.core.designsystem.R.string.member_left_group_badge
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            if (split.shareText != null) {
                CaptionText(
                    text = split.shareText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // Reserve trailing space to align with header's chevron icon (ExtraSmall spacing + 18dp icon)
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(end = MaterialTheme.spacing.ExtraSmall + CHEVRON_SIZE)
        ) {
            AmountText(
                text = split.formattedAmount,
                color = if (split.isExcluded) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            if (split.formattedSourceAmount != null) {
                CaptionText(
                    text = split.formattedSourceAmount,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

package es.pedrazamiguez.splittrip.features.settings.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.AlertTriangle
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Shield
import es.pedrazamiguez.splittrip.features.settings.R

private val STATUS_ICON_SIZE = 24.dp
private const val ICON_CONTAINER_ALPHA = 0.4f

@Composable
fun AuthStatusHeaderRow(
    isAnonymous: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = ICON_CONTAINER_ALPHA))
        ) {
            Icon(
                imageVector = if (isAnonymous) {
                    TablerIcons.Outline.AlertTriangle
                } else {
                    TablerIcons.Outline.Shield
                },
                contentDescription = null,
                tint = if (isAnonymous) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(STATUS_ICON_SIZE)
            )
        }

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.Medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.account_security_auth_status_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.account_security_auth_status_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

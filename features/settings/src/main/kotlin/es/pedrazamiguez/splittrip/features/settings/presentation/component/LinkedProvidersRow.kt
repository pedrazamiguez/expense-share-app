package es.pedrazamiguez.splittrip.features.settings.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.extension.debouncedClickable
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ChevronRight
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Link
import es.pedrazamiguez.splittrip.features.settings.R

private val ROW_ICON_SIZE = 24.dp
private val CHEVRON_ICON_SIZE = 20.dp

@Composable
fun LinkedProvidersRow(
    onManageProvidersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .debouncedClickable(onClick = onManageProvidersClick)
    ) {
        Icon(
            imageVector = TablerIcons.Outline.Link,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(ROW_ICON_SIZE)
        )

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.Medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.account_security_linked_providers_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.account_security_linked_providers_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.Small))

        Icon(
            imageVector = TablerIcons.Outline.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(CHEVRON_ICON_SIZE)
        )
    }
}

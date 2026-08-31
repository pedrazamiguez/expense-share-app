package es.pedrazamiguez.splittrip.features.settings.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.extension.debouncedClickable
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.features.settings.R

@Composable
internal fun OpenSourceLibraryItem(
    nameRes: Int,
    descriptionRes: Int,
    license: String,
    url: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        supportingContent = {
            Column {
                Text(
                    text = stringResource(descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.open_source_lib_license, license),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.Small)
                )
            }
        },
        trailingContent = {
            Text(
                text = stringResource(R.string.open_source_view_source),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .debouncedClickable { onClick(url) }
    ) {
        Text(
            text = stringResource(nameRes),
            fontWeight = FontWeight.SemiBold
        )
    }
}

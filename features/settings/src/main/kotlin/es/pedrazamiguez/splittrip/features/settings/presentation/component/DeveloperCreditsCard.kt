package es.pedrazamiguez.splittrip.features.settings.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.SectionCard
import es.pedrazamiguez.splittrip.features.settings.R

@Composable
fun DeveloperCreditsCard(credits: String) {
    SectionCard(title = stringResource(R.string.developer_info_section_credits)) {
        Text(
            text = credits,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

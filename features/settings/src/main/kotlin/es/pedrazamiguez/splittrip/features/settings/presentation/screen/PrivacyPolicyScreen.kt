package es.pedrazamiguez.splittrip.features.settings.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.SectionCard
import es.pedrazamiguez.splittrip.features.settings.R

@Composable
fun PrivacyPolicyScreen() {
    val sections = listOf(
        R.string.privacy_section_1_title to R.string.privacy_section_1_body,
        R.string.privacy_section_2_title to R.string.privacy_section_2_body,
        R.string.privacy_section_3_title to R.string.privacy_section_3_body,
        R.string.privacy_section_4_title to R.string.privacy_section_4_body
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.Default,
            vertical = MaterialTheme.spacing.Default
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Default)
    ) {
        items(sections.size) { index ->
            val (titleRes, bodyRes) = sections[index]
            SectionCard(title = stringResource(id = titleRes)) {
                Text(
                    text = stringResource(id = bodyRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

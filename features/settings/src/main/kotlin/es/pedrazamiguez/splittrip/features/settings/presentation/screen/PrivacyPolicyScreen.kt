package es.pedrazamiguez.splittrip.features.settings.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
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
            horizontal = MaterialTheme.spacing.ExtraLarge,
            vertical = MaterialTheme.spacing.ExtraLarge
        )
    ) {
        item {
            FlatCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.Large),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Large)
                ) {
                    sections.forEach { (titleRes, bodyRes) ->
                        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)) {
                            Text(
                                text = stringResource(id = titleRes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(id = bodyRes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

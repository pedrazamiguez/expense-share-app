package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.PersonalPositionUiModel

@Composable
internal fun MyPositionContent(
    personalPosition: PersonalPositionUiModel,
    modifier: Modifier = Modifier
) {
    val bottomPadding = LocalBottomPadding.current

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = MaterialTheme.spacing.Default,
            end = MaterialTheme.spacing.Default,
            top = MaterialTheme.spacing.Default,
            bottom = bottomPadding + MaterialTheme.spacing.Default
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Default)
    ) {
        item(key = "personal_position_card") {
            PersonalPositionCard(personalPosition = personalPosition)
        }
    }
}

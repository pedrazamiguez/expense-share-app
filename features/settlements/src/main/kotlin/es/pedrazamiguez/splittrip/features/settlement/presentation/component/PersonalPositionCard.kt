package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.SectionHeadingText
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.PersonalPositionUiModel

@Composable
internal fun PersonalPositionCard(
    personalPosition: PersonalPositionUiModel,
    modifier: Modifier = Modifier
) {
    FlatCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.Default),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
        ) {
            SectionHeadingText(
                text = stringResource(R.string.my_position_section_personal_position)
            )

            PersonalPositionHeroBanner(personalPosition = personalPosition)

            PersonalPositionPocketCashRow(personalPosition = personalPosition)

            PersonalPositionActivityBreakdown(personalPosition = personalPosition)

            if (personalPosition.hasNegativeCashInHand) {
                CaptionText(
                    text = stringResource(R.string.my_position_negative_cash_hint),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

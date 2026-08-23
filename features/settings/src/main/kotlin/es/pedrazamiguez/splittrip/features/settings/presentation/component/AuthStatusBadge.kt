package es.pedrazamiguez.splittrip.features.settings.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.features.settings.R

private val STATUS_BADGE_PADDING_HORIZONTAL = 8.dp
private val STATUS_BADGE_PADDING_VERTICAL = 4.dp
private val STATUS_BADGE_CORNER_RADIUS = 12.dp
private const val BADGE_CONTAINER_ALPHA = 0.12f

@Composable
fun AuthStatusBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(STATUS_BADGE_CORNER_RADIUS))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = BADGE_CONTAINER_ALPHA))
            .padding(
                horizontal = STATUS_BADGE_PADDING_HORIZONTAL,
                vertical = STATUS_BADGE_PADDING_VERTICAL
            )
    ) {
        Text(
            text = stringResource(R.string.account_security_status_verified),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

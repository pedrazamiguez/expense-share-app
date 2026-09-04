package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.pedrazamiguez.splittrip.core.designsystem.R

/**
 * Reusable Pro badge pill component indicating a gated or premium tier feature.
 */
@Composable
fun ProBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiary)
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = stringResource(R.string.badge_pro),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 10.sp
            ),
            color = MaterialTheme.colorScheme.onTertiary
        )
    }
}

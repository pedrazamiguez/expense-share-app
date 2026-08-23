package es.pedrazamiguez.splittrip.features.settings.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.UserPin
import es.pedrazamiguez.splittrip.features.settings.R

private val AVATAR_SIZE = 96.dp

@Composable
fun DeveloperAvatar(avatarUrl: String) {
    if (avatarUrl.isNotBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.developer_info_avatar_cd),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(AVATAR_SIZE)
                .clip(CircleShape)
        )
    } else {
        Surface(
            modifier = Modifier.size(AVATAR_SIZE),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = TablerIcons.Outline.UserPin,
                contentDescription = stringResource(R.string.developer_info_avatar_cd),
                modifier = Modifier.padding(MaterialTheme.spacing.Large),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

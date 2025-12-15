package es.pedrazamiguez.expenseshareapp.features.settings.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.settings.R

/**
 * A dynamic LargeTopAppBar that animates its title color and subtitle
 * based on scroll position.
 *
 * Features:
 * - Title color transitions from primary to onSurface as it collapses
 * - Subtitle fades out and shrinks as the app bar collapses
 * - Smooth, scroll-synchronized animations (no lag)
 *
 * @param scrollBehavior The scroll behavior to control collapse animations
 * @param onBack Callback when back button is pressed
 * @param actions Optional actions to display in the app bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    // Use the derived state directly - no animation delay needed
    // The scroll itself provides smooth interpolation
    val collapseFraction by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction }
    }

    // Dynamic title color that transitions from primary to onSurface as it collapses
    val expandedTitleColor = MaterialTheme.colorScheme.primary
    val collapsedTitleColor = MaterialTheme.colorScheme.onSurface
    val titleColor = lerp(expandedTitleColor, collapsedTitleColor, collapseFraction)

    // Subtitle alpha - fades out as the app bar collapses
    val subtitleAlpha = 1f - collapseFraction

    // Subtitle height - shrinks as the app bar collapses
    val subtitleMaxHeight = 20.dp
    val subtitleHeight = subtitleMaxHeight * subtitleAlpha

    LargeTopAppBar(
        title = {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                // Animate both alpha and height for smooth collapse
                // The height animation ensures the layout actually shrinks
                Text(
                    text = stringResource(R.string.settings_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .height(subtitleHeight)
                        .graphicsLayer {
                            alpha = subtitleAlpha
                        }
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.settings_back)
                )
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    )
}


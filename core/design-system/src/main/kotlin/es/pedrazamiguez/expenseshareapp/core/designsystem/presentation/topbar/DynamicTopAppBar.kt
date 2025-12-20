package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar

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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A dynamic LargeTopAppBar that animates its title color and optional subtitle
 * based on scroll position.
 *
 * Features:
 * - Title color transitions from primary to onSurface as it collapses
 * - Optional subtitle that fades out and shrinks as the app bar collapses
 * - Smooth, scroll-synchronized animations (no lag)
 * - Automatic fallback to standard TopAppBar if no scroll behavior is provided
 *
 * @param title The main title text
 * @param subtitle Optional subtitle that fades out on scroll (only for LargeTopAppBar)
 * @param onBack Optional callback for back navigation. If null, no back button is shown.
 * @param actions Optional actions to display in the app bar
 * @param scrollBehavior Optional scroll behavior. If null, tries LocalTopAppBarState,
 *                       then LocalTopAppBarScrollBehavior. Falls back to non-collapsing TopAppBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicTopAppBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    // Try to get scroll behavior from: parameter -> state holder -> direct local
    val effectiveScrollBehavior = scrollBehavior ?: LocalTopAppBarState.current.scrollBehavior
    ?: LocalTopAppBarScrollBehavior.current

    if (effectiveScrollBehavior != null) {
        DynamicLargeTopAppBar(
            title = title,
            subtitle = subtitle,
            onBack = onBack,
            actions = actions,
            scrollBehavior = effectiveScrollBehavior
        )
    } else {
        // Fallback to standard TopAppBar when no scroll behavior is available
        StandardTopAppBar(
            title = title, onBack = onBack, actions = actions
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DynamicLargeTopAppBar(
    title: String,
    subtitle: String?,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    // Use the derived state directly - no animation delay needed
    // The scroll itself provides smooth interpolation
    val collapseFraction by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction }
    }

    // Dynamic title color that transitions from primary to onPrimary as it collapses
    val expandedTitleColor = MaterialTheme.colorScheme.primary
    val collapsedTitleColor = MaterialTheme.colorScheme.onPrimary
    val titleColor = lerp(expandedTitleColor, collapsedTitleColor, collapseFraction)

    // Subtitle alpha and height - fades out as the app bar collapses
    val subtitleAlpha = 1f - collapseFraction
    val subtitleMaxHeight = 20.dp
    val subtitleHeight = subtitleMaxHeight * subtitleAlpha

    LargeTopAppBar(
        title = {
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title, fontWeight = FontWeight.Bold, color = titleColor
            )
            // Animate both alpha and height for smooth collapse
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .height(subtitleHeight)
                        .graphicsLayer {
                            alpha = subtitleAlpha
                        })
            }
        }
    },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = titleColor
                    )
                }
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StandardTopAppBar(
    title: String, onBack: (() -> Unit)?, actions: @Composable RowScope.() -> Unit
) {
    TopAppBar(
        title = {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }, navigationIcon = {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }, actions = actions, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
    )
}


package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.R


/**
 * A dynamic LargeTopAppBar that animates its title color and optional subtitle
 * based on scroll position.
 *
 * Features:
 * - Title color transitions from primary to onPrimary as it collapses
 * - Optional subtitle that fades out quickly (before 30% collapse) to avoid visual glitches
 * - Smooth, scroll-synchronized animations (no lag)
 * - Automatic fallback to standard TopAppBar if no scroll behavior is provided
 *
 * @param title The main title text
 * @param subtitle Optional subtitle that fades out on scroll (only for LargeTopAppBar)
 * @param onBack Optional callback for back navigation. If null, no back button is shown.
 * @param actions Optional actions to display in the app bar
 * @param scrollBehavior Optional scroll behavior. If null, tries LocalTopAppBarState,
 * then LocalTopAppBarScrollBehavior. Falls back to non-collapsing TopAppBar.
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
    // CRITICAL FIX: Added 'scrollBehavior' as a key to remember.
    // Without this, if the scrollBehavior instance changes (e.g. list update),
    // this state becomes stale and the bar gets stuck in the expanded state.
    val collapseFraction by remember(scrollBehavior) {
        derivedStateOf { scrollBehavior.state.collapsedFraction }
    }

    // Dynamic title color that transitions from primary to onPrimary as it collapses
    val expandedTitleColor = MaterialTheme.colorScheme.primary
    val collapsedTitleColor = MaterialTheme.colorScheme.onPrimary
    val titleColor = lerp(expandedTitleColor, collapsedTitleColor, collapseFraction)

    // Calculate background color manually to ensure it stays in sync with text.
    // This prevents the "Invisible Title" bug where the background turns blue
    // before the text turns white.
    val expandedContainerColor = MaterialTheme.colorScheme.background
    val collapsedContainerColor = MaterialTheme.colorScheme.primary
    val containerColor = lerp(expandedContainerColor, collapsedContainerColor, collapseFraction)

    val navigationIconBgColor = lerp(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
        collapseFraction
    )

    // Subtitle visibility - fade out very quickly when scrolling starts
    val subtitleVisibilityThreshold = 0.1f
    val subtitleAlpha = if (collapseFraction < subtitleVisibilityThreshold) {
        val linearAlpha = 1f - (collapseFraction / subtitleVisibilityThreshold)
        // Use squared curve for faster fade-out at the start of scrolling
        linearAlpha * linearAlpha
    } else {
        0f
    }

    LargeTopAppBar(
        title = {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title, fontWeight = FontWeight.Bold, color = titleColor
                )
                if (subtitle != null) {
                    val subtitleHeight = 20.dp * subtitleAlpha
                    val displaySubtitle = if (subtitleAlpha > 0.01f) subtitle else ""
                    Text(
                        text = displaySubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = subtitleAlpha),
                        modifier = Modifier
                            .heightIn(max = subtitleHeight)
                            .graphicsLayer {
                                alpha = subtitleAlpha
                            })
                }
            }
        }, navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(navigationIconBgColor), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_description_back),
                            tint = titleColor
                        )
                    }
                }
            }
        }, actions = {
            CompositionLocalProvider(LocalContentColor provides titleColor) {
                actions()
            }
        }, scrollBehavior = scrollBehavior, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = containerColor // Force both to match our calculated color
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_description_back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }, actions = {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
                actions()
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

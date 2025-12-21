package es.pedrazamiguez.expenseshareapp.core.designsystem.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CompositionLocal providing the bottom padding value for screens inside a floating bottom bar layout.
 * This allows content (lists, FABs) to properly account for the bottom navigation bar height.
 *
 * When using a floating bottom bar design, screens should use this value to:
 * - Add contentPadding to scrollable content (LazyColumn, LazyGrid) so the last item is visible
 * - Position FABs above the bottom bar
 */
val LocalBottomPadding = compositionLocalOf<Dp> { 0.dp }


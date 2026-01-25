package es.pedrazamiguez.expenseshareapp.core.designsystem.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalAnimatedVisibilityScope

/**
 * A wrapper around [composable] that automatically provides [LocalAnimatedVisibilityScope].
 * This allows screens to use shared element transitions without manual boilerplate.
 */
fun NavGraphBuilder.sharedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = route, arguments = arguments, deepLinks = deepLinks
    ) { backStackEntry ->
        // "this" here is the AnimatedContentScope (which is an AnimatedVisibilityScope)
        CompositionLocalProvider(
            LocalAnimatedVisibilityScope provides this
        ) {
            content(backStackEntry)
        }
    }
}

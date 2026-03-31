package es.pedrazamiguez.expenseshareapp.core.designsystem.navigation

import androidx.navigation.NavGraphBuilder

/**
 * Allows a feature module to contribute navigation routes into another tab's [NavHost]
 * **without** a compile-time Gradle dependency between the two feature modules.
 *
 * ## Use case
 * When a write-flow (e.g., "Add Contribution") is extracted into its own feature module
 * but must remain reachable via `LocalTabNavController` from a tab screen (e.g., Balances),
 * the originating tab's [NavigationProvider] injects all [TabGraphContributor]
 * instances via Koin and calls [contributeGraph] inside its [NavigationProvider.buildGraph].
 *
 * ## Example
 * ```kotlin
 * class BalancesNavigationProviderImpl(
 *     private val graphContributors: List<TabGraphContributor> = emptyList()
 * ) : NavigationProvider {
 *     override fun buildGraph(builder: NavGraphBuilder) {
 *         builder.balancesGraph()
 *         graphContributors.forEach { it.contributeGraph(builder) }
 *     }
 * }
 * ```
 */
fun interface TabGraphContributor {
    fun contributeGraph(builder: NavGraphBuilder)
}

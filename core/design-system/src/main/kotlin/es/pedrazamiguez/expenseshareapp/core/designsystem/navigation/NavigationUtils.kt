package es.pedrazamiguez.expenseshareapp.core.designsystem.navigation

/**
 * Pure utility functions for navigation decision-making.
 *
 * Extracted from composables ([AppNavHost], [MainScreen]) so the critical
 * business logic can be unit-tested without Compose or instrumentation.
 */
object NavigationUtils {

    /**
     * Resolves the initial start destination for the root navigation graph.
     *
     * Returns `null` while auth or onboarding state is still loading, which
     * signals the caller to show a splash/loading indicator. Once both values
     * are known the appropriate route is returned.
     *
     * @param isUserLoggedIn `null` while loading, `true`/`false` once known.
     * @param onboardingCompleted `null` while loading, `true`/`false` once known.
     * @return The route to use as [NavHost] startDestination, or `null` if undetermined.
     */
    fun resolveStartDestination(
        isUserLoggedIn: Boolean?,
        onboardingCompleted: Boolean?,
    ): String? = when {
        isUserLoggedIn == null || onboardingCompleted == null -> null
        isUserLoggedIn == false -> Routes.LOGIN
        onboardingCompleted == false -> Routes.ONBOARDING
        else -> Routes.MAIN
    }

    /**
     * Resolves the destination to navigate to after a successful login.
     *
     * @param onboardingCompleted Whether the user has completed onboarding.
     * @return [Routes.MAIN] if onboarding is done, [Routes.ONBOARDING] otherwise.
     */
    fun resolvePostLoginDestination(onboardingCompleted: Boolean?): String =
        if (onboardingCompleted == true) Routes.MAIN else Routes.ONBOARDING

    /**
     * Filters and sorts [NavigationProvider]s to only include tabs that should
     * be visible given the current group-selection state.
     *
     * Providers with [NavigationProvider.requiresSelectedGroup] == `true` are
     * hidden when no group is selected (`selectedGroupId == null`).
     *
     * @param providers All registered navigation providers.
     * @param selectedGroupId The currently selected group ID, or `null`.
     * @return Visible providers sorted by [NavigationProvider.order].
     */
    fun filterVisibleProviders(
        providers: List<NavigationProvider>,
        selectedGroupId: String?,
    ): List<NavigationProvider> = providers
        .filter { !it.requiresSelectedGroup || selectedGroupId != null }
        .sortedBy { it.order }
}


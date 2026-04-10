package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.local.datastore.UserPreferences
import es.pedrazamiguez.splittrip.domain.repository.GroupPreferenceRepository
import es.pedrazamiguez.splittrip.domain.repository.OnboardingPreferenceRepository
import es.pedrazamiguez.splittrip.domain.repository.UserPreferenceRepository

/**
 * Deprecated: split into [OnboardingPreferenceRepositoryImpl], [GroupPreferenceRepositoryImpl],
 * and [UserPreferenceRepositoryImpl] following the Interface Segregation Principle.
 * Uses Kotlin delegation so no functions are re-declared (avoids TooManyFunctions).
 */
@Deprecated(
    message = "Use OnboardingPreferenceRepositoryImpl, GroupPreferenceRepositoryImpl, or UserPreferenceRepositoryImpl.",
    level = DeprecationLevel.ERROR
)
internal class PreferenceRepositoryImpl(userPreferences: UserPreferences) :
    OnboardingPreferenceRepository by OnboardingPreferenceRepositoryImpl(userPreferences),
    GroupPreferenceRepository by GroupPreferenceRepositoryImpl(userPreferences),
    UserPreferenceRepository by UserPreferenceRepositoryImpl(userPreferences)

package es.pedrazamiguez.splittrip.domain.repository

/**
 * Deprecated: split into [OnboardingPreferenceRepository], [GroupPreferenceRepository],
 * and [UserPreferenceRepository] following the Interface Segregation Principle.
 */
@Deprecated(
    message = "Use OnboardingPreferenceRepository, GroupPreferenceRepository, or UserPreferenceRepository.",
    level = DeprecationLevel.ERROR
)
internal interface PreferenceRepository :
    OnboardingPreferenceRepository,
    GroupPreferenceRepository,
    UserPreferenceRepository

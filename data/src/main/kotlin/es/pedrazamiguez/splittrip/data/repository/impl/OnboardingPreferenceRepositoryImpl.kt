package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.local.datastore.UserPreferences
import es.pedrazamiguez.splittrip.domain.repository.OnboardingPreferenceRepository
import kotlinx.coroutines.flow.Flow

class OnboardingPreferenceRepositoryImpl(
    private val userPreferences: UserPreferences
) : OnboardingPreferenceRepository {

    override fun isOnboardingComplete(): Flow<Boolean> = userPreferences.isOnboardingComplete

    override suspend fun setOnboardingComplete() {
        userPreferences.setOnboardingComplete()
    }
}

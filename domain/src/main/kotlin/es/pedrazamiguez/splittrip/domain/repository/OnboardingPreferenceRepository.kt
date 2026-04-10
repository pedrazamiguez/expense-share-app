package es.pedrazamiguez.splittrip.domain.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingPreferenceRepository {

    fun isOnboardingComplete(): Flow<Boolean>
    suspend fun setOnboardingComplete()
}

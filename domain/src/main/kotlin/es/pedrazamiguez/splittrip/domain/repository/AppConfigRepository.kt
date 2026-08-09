package es.pedrazamiguez.splittrip.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface AppConfigRepository {
    val defaultCurrencyCode: StateFlow<String>
    val balanceComputationDebounceMs: StateFlow<Long>
    val maxMembersPerGroup: StateFlow<Int>
    val extractedDateMaxFutureDays: StateFlow<Int>
    val supportEmailAddress: StateFlow<String>
    val settlementNudgeRateLimitHours: StateFlow<Long>
    val ocrSafetyFalsePositivesBlacklist: StateFlow<List<String>>

    suspend fun fetchConfiguration(): Boolean
}

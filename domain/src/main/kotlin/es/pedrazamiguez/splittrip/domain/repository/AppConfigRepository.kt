package es.pedrazamiguez.splittrip.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface AppConfigRepository {
    val defaultCurrencyCode: StateFlow<String>
    val balanceComputationDebounceMs: StateFlow<Long>
    val maxMembersPerGroup: StateFlow<Int>
    val extractedDateMaxFutureDays: StateFlow<Int>

    suspend fun fetchConfiguration(): Boolean
}

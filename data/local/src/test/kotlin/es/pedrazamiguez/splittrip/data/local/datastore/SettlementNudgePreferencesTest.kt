package es.pedrazamiguez.splittrip.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SettlementNudgePreferencesTest {

    private lateinit var context: Context
    private lateinit var authenticationService: AuthenticationService

    private companion object {
        private const val USER_A_ID = "user-a-123"
        private const val USER_B_ID = "user-b-456"
        private const val SETTLEMENT_ID_1 = "settlement-001"
        private const val SETTLEMENT_ID_2 = "settlement-002"
    }

    @Before
    fun setUp() = runTest {
        context = ApplicationProvider.getApplicationContext()
        authenticationService = mockk()
        context.dataStore.edit { it.clear() }
    }

    @After
    fun tearDown() = runTest {
        context.dataStore.edit { it.clear() }
    }

    private fun createPreferences(
        userId: String? = USER_A_ID,
        authStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(
            userId != null
        )
    ): SettlementNudgePreferences {
        every { authenticationService.currentUserId() } returns userId
        every { authenticationService.authState } returns authStateFlow
        return SettlementNudgePreferences(context, authenticationService)
    }

    @Test
    fun `recordNudgeTimestamp_persistsUserScopedTimestamp`() = runTest {
        val prefs = createPreferences(USER_A_ID)
        val timestamp = 1700000000000L

        prefs.recordNudgeTimestamp(SETTLEMENT_ID_1, timestamp)

        val retrievedTs = prefs.getLastNudgeTimestamp(SETTLEMENT_ID_1)
        assertEquals(timestamp, retrievedTs)
    }

    @Test
    fun `nudgeTimestampsFlow_emitsUpdatedMapWhenTimestampRecorded`() = runTest {
        val prefs = createPreferences(USER_A_ID)
        val timestamp1 = 1700000000000L
        val timestamp2 = 1700000500000L

        prefs.recordNudgeTimestamp(SETTLEMENT_ID_1, timestamp1)
        prefs.recordNudgeTimestamp(SETTLEMENT_ID_2, timestamp2)

        val map = prefs.nudgeTimestampsFlow.first()
        assertEquals(2, map.size)
        assertEquals(timestamp1, map[SETTLEMENT_ID_1])
        assertEquals(timestamp2, map[SETTLEMENT_ID_2])
    }

    @Test
    fun `nudgeTimestampsFlow_whenAuthChanges_switchesUserScope`() = runTest {
        val authStateFlow = MutableStateFlow(true)
        every { authenticationService.authState } returns authStateFlow

        // User A writes a timestamp
        every { authenticationService.currentUserId() } returns USER_A_ID
        val prefs = SettlementNudgePreferences(context, authenticationService)
        prefs.recordNudgeTimestamp(SETTLEMENT_ID_1, 12345L)

        // Switch user scope to User B
        every { authenticationService.currentUserId() } returns USER_B_ID
        authStateFlow.value = false
        authStateFlow.value = true

        val mapB = prefs.nudgeTimestampsFlow.first()
        assertTrue(mapB.isEmpty())
    }
}

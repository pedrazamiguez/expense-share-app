package es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.model.GroupExpenseConfig
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsUseCase
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.mapper.AddCashWithdrawalUiMapper
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.action.AddCashWithdrawalUiAction
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.state.AddCashWithdrawalUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WithdrawalConfigHandlerTest {

    private lateinit var handler: WithdrawalConfigHandler
    private lateinit var getGroupExpenseConfigUseCase: GetGroupExpenseConfigUseCase
    private lateinit var getGroupSubunitsUseCase: GetGroupSubunitsUseCase
    private lateinit var authenticationService: AuthenticationService
    private lateinit var addCashWithdrawalUiMapper: AddCashWithdrawalUiMapper

    private lateinit var uiState: MutableStateFlow<AddCashWithdrawalUiState>
    private lateinit var actions: MutableSharedFlow<AddCashWithdrawalUiAction>

    private val eurCurrency = Currency(code = "EUR", symbol = "€", defaultName = "Euro", decimalDigits = 2)
    private val usdCurrency = Currency(code = "USD", symbol = "$", defaultName = "US Dollar", decimalDigits = 2)
    private val testGroup = Group(id = "group-1", name = "Trip to Paris", currency = "EUR")
    private val testConfig = GroupExpenseConfig(
        group = testGroup,
        groupCurrency = eurCurrency,
        availableCurrencies = listOf(eurCurrency, usdCurrency),
        subunits = emptyList()
    )

    @BeforeEach
    fun setUp() {
        getGroupExpenseConfigUseCase = mockk()
        getGroupSubunitsUseCase = mockk()
        authenticationService = mockk()
        addCashWithdrawalUiMapper = mockk(relaxed = true)

        uiState = MutableStateFlow(AddCashWithdrawalUiState())
        actions = MutableSharedFlow(extraBufferCapacity = 1)

        handler = WithdrawalConfigHandler(
            getGroupExpenseConfigUseCase = getGroupExpenseConfigUseCase,
            getGroupSubunitsUseCase = getGroupSubunitsUseCase,
            authenticationService = authenticationService,
            addCashWithdrawalUiMapper = addCashWithdrawalUiMapper
        )

        // Default stubs
        coEvery { getGroupSubunitsUseCase(any()) } returns emptyList()
        every { authenticationService.currentUserId() } returns "user-1"
    }

    @Nested
    inner class LoadGroupConfig {

        @Test
        fun `does nothing when groupId is null`() = runTest {
            handler.bind(uiState, actions, this)
            handler.loadGroupConfig(null)
            advanceUntilIdle()

            assertFalse(uiState.value.isConfigLoaded)
            assertFalse(uiState.value.isLoading)
        }

        @Test
        fun `sets isConfigLoaded to true on success`() = runTest {
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertTrue(uiState.value.isConfigLoaded)
        }

        @Test
        fun `sets isLoading to false after successful load`() = runTest {
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertFalse(uiState.value.isLoading)
        }

        @Test
        fun `stores the loaded group ID`() = runTest {
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertEquals("group-1", uiState.value.loadedGroupId)
        }

        @Test
        fun `sets groupName from config on success`() = runTest {
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertEquals("Trip to Paris", uiState.value.groupName)
        }

        @Test
        fun `clears configLoadFailed on success`() = runTest {
            uiState.value = AddCashWithdrawalUiState(configLoadFailed = true)
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertFalse(uiState.value.configLoadFailed)
        }

        @Test
        fun `sets configLoadFailed to true on failure`() = runTest {
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns
                Result.failure(RuntimeException("network error"))

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertTrue(uiState.value.configLoadFailed)
        }

        @Test
        fun `sets isLoading to false on failure`() = runTest {
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns
                Result.failure(RuntimeException("error"))

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertFalse(uiState.value.isLoading)
        }

        @Test
        fun `sets isConfigLoaded to false on failure`() = runTest {
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns
                Result.failure(RuntimeException("error"))

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertFalse(uiState.value.isConfigLoaded)
        }

        @Test
        fun `sets error UiText on failure`() = runTest {
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns
                Result.failure(RuntimeException("error"))

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertNotNull(uiState.value.error)
        }

        @Test
        fun `skips load when config already loaded for the same group`() = runTest {
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            // Second call for the same group — should not invoke the use case again
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            coVerify(exactly = 1) { getGroupExpenseConfigUseCase("group-1", any()) }
        }

        @Test
        fun `reloads when forceRefresh is true even if already loaded`() = runTest {
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            handler.loadGroupConfig("group-1", forceRefresh = true)
            advanceUntilIdle()

            coVerify(exactly = 2) { getGroupExpenseConfigUseCase("group-1", any()) }
        }

        @Test
        fun `reloads when group ID changes`() = runTest {
            val group2 = Group(id = "group-2", name = "Weekend Trip", currency = "EUR")
            val config2 = testConfig.copy(group = group2)
            coEvery { getGroupExpenseConfigUseCase("group-1", any()) } returns Result.success(testConfig)
            coEvery { getGroupExpenseConfigUseCase("group-2", any()) } returns Result.success(config2)

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()
            assertEquals("group-1", uiState.value.loadedGroupId)

            handler.loadGroupConfig("group-2")
            advanceUntilIdle()
            assertEquals("group-2", uiState.value.loadedGroupId)
        }

        @Test
        fun `resets state when loading a different group`() = runTest {
            val group2 = Group(id = "group-2", name = "Second Group", currency = "EUR")
            val config2 = testConfig.copy(group = group2)
            coEvery { getGroupExpenseConfigUseCase("group-1", any()) } returns Result.success(testConfig)
            coEvery { getGroupExpenseConfigUseCase("group-2", any()) } returns Result.success(config2)

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()
            // Advance the title
            uiState.value = uiState.value.copy(title = "Some title")

            // Load a new group — state should be reset
            handler.loadGroupConfig("group-2")
            advanceUntilIdle()

            assertEquals("group-2", uiState.value.loadedGroupId)
        }

        @Test
        fun `loads only subunits that include the current user`() = runTest {
            val currentUserId = "user-1"
            val memberSubunit = Subunit(
                id = "sub-1",
                groupId = "group-1",
                name = "Couple",
                memberIds = listOf(currentUserId, "user-2")
            )
            val otherSubunit = Subunit(
                id = "sub-2",
                groupId = "group-1",
                name = "Other Team",
                memberIds = listOf("user-3", "user-4")
            )
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)
            coEvery { getGroupSubunitsUseCase("group-1") } returns listOf(memberSubunit, otherSubunit)
            every { authenticationService.currentUserId() } returns currentUserId

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertEquals(1, uiState.value.subunitOptions.size)
            assertEquals("sub-1", uiState.value.subunitOptions[0].id)
            assertEquals("Couple", uiState.value.subunitOptions[0].name)
        }

        @Test
        fun `loads no subunit options when user is in no subunits`() = runTest {
            val otherSubunit = Subunit(
                id = "sub-1",
                groupId = "group-1",
                name = "Other Team",
                memberIds = listOf("user-99")
            )
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)
            coEvery { getGroupSubunitsUseCase("group-1") } returns listOf(otherSubunit)
            every { authenticationService.currentUserId() } returns "user-1"

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertTrue(uiState.value.subunitOptions.isEmpty())
        }

        @Test
        fun `loads no subunit options when currentUserId is null`() = runTest {
            val subunit = Subunit(
                id = "sub-1",
                groupId = "group-1",
                name = "Team",
                memberIds = listOf("user-1")
            )
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)
            coEvery { getGroupSubunitsUseCase("group-1") } returns listOf(subunit)
            every { authenticationService.currentUserId() } returns null

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertTrue(uiState.value.subunitOptions.isEmpty())
        }

        @Test
        fun `loads all subunit options when user is in all subunits`() = runTest {
            val currentUserId = "user-1"
            val sub1 = Subunit(
                id = "sub-1",
                groupId = "group-1",
                name = "Alpha",
                memberIds = listOf(currentUserId)
            )
            val sub2 = Subunit(
                id = "sub-2",
                groupId = "group-1",
                name = "Beta",
                memberIds = listOf(currentUserId, "user-2")
            )
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)
            coEvery { getGroupSubunitsUseCase("group-1") } returns listOf(sub1, sub2)
            every { authenticationService.currentUserId() } returns currentUserId

            handler.bind(uiState, actions, this)
            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertEquals(2, uiState.value.subunitOptions.size)
        }
    }
}

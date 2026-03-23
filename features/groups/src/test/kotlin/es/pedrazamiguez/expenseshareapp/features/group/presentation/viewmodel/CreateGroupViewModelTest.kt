package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.service.EmailValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetSupportedCurrenciesUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.CreateGroupUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetUserDefaultCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.SearchUsersByEmailUseCase
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.GroupUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.CreateGroupUiAction
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateGroupUiEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateGroupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var createGroupUseCase: CreateGroupUseCase
    private lateinit var getSupportedCurrenciesUseCase: GetSupportedCurrenciesUseCase
    private lateinit var getUserDefaultCurrencyUseCase: GetUserDefaultCurrencyUseCase
    private lateinit var searchUsersByEmailUseCase: SearchUsersByEmailUseCase
    private lateinit var emailValidationService: EmailValidationService
    private lateinit var groupUiMapper: GroupUiMapper
    private lateinit var viewModel: CreateGroupViewModel

    private val testUser1 = User(
        userId = "user-1",
        email = "alice@example.com",
        displayName = "Alice"
    )

    private val testUser2 = User(
        userId = "user-2",
        email = "bob@example.com",
        displayName = "Bob"
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        createGroupUseCase = mockk(relaxed = true)
        getSupportedCurrenciesUseCase = mockk(relaxed = true)
        getUserDefaultCurrencyUseCase = mockk(relaxed = true)
        searchUsersByEmailUseCase = mockk(relaxed = true)
        groupUiMapper = mockk(relaxed = true)
        emailValidationService = EmailValidationService()

        every { getUserDefaultCurrencyUseCase() } returns flowOf("EUR")

        viewModel = CreateGroupViewModel(
            createGroupUseCase = createGroupUseCase,
            getSupportedCurrenciesUseCase = getSupportedCurrenciesUseCase,
            getUserDefaultCurrencyUseCase = getUserDefaultCurrencyUseCase,
            searchUsersByEmailUseCase = searchUsersByEmailUseCase,
            emailValidationService = emailValidationService,
            groupUiMapper = groupUiMapper
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun onEvent(event: CreateGroupUiEvent) {
        viewModel.onEvent(event) {}
    }

    @Nested
    inner class MemberSearch {

        @Test
        fun `does not trigger search for partial email`() = runTest(testDispatcher) {
            // Given — not a valid email
            onEvent(CreateGroupUiEvent.MemberSearchQueryChanged("john@gma"))
            advanceUntilIdle()

            // Then — use case never called
            coVerify(exactly = 0) { searchUsersByEmailUseCase(any()) }
            assertTrue(viewModel.uiState.value.memberSearchResults.isEmpty())
        }

        @Test
        fun `does not trigger search for short query`() = runTest(testDispatcher) {
            onEvent(CreateGroupUiEvent.MemberSearchQueryChanged("ab"))
            advanceUntilIdle()

            coVerify(exactly = 0) { searchUsersByEmailUseCase(any()) }
        }

        @Test
        fun `triggers search for valid email after debounce`() = runTest(testDispatcher) {
            // Given
            coEvery { searchUsersByEmailUseCase("alice@example.com") } returns Result.success(listOf(testUser1))

            // When
            onEvent(CreateGroupUiEvent.MemberSearchQueryChanged("alice@example.com"))

            // Then — not yet called (debounce)
            advanceTimeBy(200)
            coVerify(exactly = 0) { searchUsersByEmailUseCase(any()) }

            // After debounce
            advanceTimeBy(200)
            coVerify(exactly = 1) { searchUsersByEmailUseCase("alice@example.com") }
            assertEquals(1, viewModel.uiState.value.memberSearchResults.size)
            assertEquals("user-1", viewModel.uiState.value.memberSearchResults[0].userId)
        }

        @Test
        fun `debounce cancels previous search when query changes rapidly`() = runTest(testDispatcher) {
            // Given
            coEvery { searchUsersByEmailUseCase("alice@example.com") } returns Result.success(listOf(testUser1))
            coEvery { searchUsersByEmailUseCase("bob@example.com") } returns Result.success(listOf(testUser2))

            // When — type first email, then quickly change to second
            onEvent(CreateGroupUiEvent.MemberSearchQueryChanged("alice@example.com"))
            advanceTimeBy(100)
            onEvent(CreateGroupUiEvent.MemberSearchQueryChanged("bob@example.com"))
            advanceUntilIdle()

            // Then — only second search was executed
            coVerify(exactly = 0) { searchUsersByEmailUseCase("alice@example.com") }
            coVerify(exactly = 1) { searchUsersByEmailUseCase("bob@example.com") }
            assertEquals("user-2", viewModel.uiState.value.memberSearchResults[0].userId)
        }

        @Test
        fun `filters out already selected users from search results`() = runTest(testDispatcher) {
            // Given — user-1 is already selected
            onEvent(CreateGroupUiEvent.MemberSelected(testUser1))

            coEvery { searchUsersByEmailUseCase("alice@example.com") } returns
                Result.success(listOf(testUser1))

            // When
            onEvent(CreateGroupUiEvent.MemberSearchQueryChanged("alice@example.com"))
            advanceUntilIdle()

            // Then — search result is filtered because user-1 is already selected
            assertTrue(viewModel.uiState.value.memberSearchResults.isEmpty())
        }

        @Test
        fun `search failure clears results`() = runTest(testDispatcher) {
            // Given
            coEvery { searchUsersByEmailUseCase(any()) } returns
                Result.failure(RuntimeException("Network error"))

            // When
            onEvent(CreateGroupUiEvent.MemberSearchQueryChanged("alice@example.com"))
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value.memberSearchResults.isEmpty())
            assertFalse(viewModel.uiState.value.isSearchingMembers)
        }
    }

    @Nested
    inner class MemberSelection {

        @Test
        fun `MemberSelected adds user to selectedMembers`() = runTest(testDispatcher) {
            onEvent(CreateGroupUiEvent.MemberSelected(testUser1))

            assertEquals(1, viewModel.uiState.value.selectedMembers.size)
            assertEquals("user-1", viewModel.uiState.value.selectedMembers[0].userId)
        }

        @Test
        fun `MemberSelected clears search results`() = runTest(testDispatcher) {
            // Given — populate search results first
            coEvery { searchUsersByEmailUseCase(any()) } returns Result.success(listOf(testUser1))
            onEvent(CreateGroupUiEvent.MemberSearchQueryChanged("alice@example.com"))
            advanceUntilIdle()
            assertEquals(1, viewModel.uiState.value.memberSearchResults.size)

            // When
            onEvent(CreateGroupUiEvent.MemberSelected(testUser1))

            // Then
            assertTrue(viewModel.uiState.value.memberSearchResults.isEmpty())
        }

        @Test
        fun `MemberSelected does not duplicate already selected user`() = runTest(testDispatcher) {
            onEvent(CreateGroupUiEvent.MemberSelected(testUser1))
            onEvent(CreateGroupUiEvent.MemberSelected(testUser1))

            assertEquals(1, viewModel.uiState.value.selectedMembers.size)
        }

        @Test
        fun `MemberRemoved removes user from selectedMembers`() = runTest(testDispatcher) {
            onEvent(CreateGroupUiEvent.MemberSelected(testUser1))
            onEvent(CreateGroupUiEvent.MemberSelected(testUser2))
            assertEquals(2, viewModel.uiState.value.selectedMembers.size)

            onEvent(CreateGroupUiEvent.MemberRemoved(testUser1))

            assertEquals(1, viewModel.uiState.value.selectedMembers.size)
            assertEquals("user-2", viewModel.uiState.value.selectedMembers[0].userId)
        }
    }

    @Nested
    inner class SubmitCreateGroup {

        @Test
        fun `passes selected member userIds to CreateGroupUseCase`() = runTest(testDispatcher) {
            // Given
            val groupSlot = slot<Group>()
            coEvery { createGroupUseCase(capture(groupSlot)) } returns Result.success("group-id")

            onEvent(CreateGroupUiEvent.NameChanged("Trip"))
            onEvent(CreateGroupUiEvent.MemberSelected(testUser1))
            onEvent(CreateGroupUiEvent.MemberSelected(testUser2))

            // When
            viewModel.onEvent(CreateGroupUiEvent.SubmitCreateGroup) {}
            advanceUntilIdle()

            // Then
            val capturedGroup = groupSlot.captured
            assertTrue("user-1" in capturedGroup.members)
            assertTrue("user-2" in capturedGroup.members)
            assertEquals(2, capturedGroup.members.size)
        }

        @Test
        fun `creates group with empty members when none selected`() = runTest(testDispatcher) {
            // Given
            val groupSlot = slot<Group>()
            coEvery { createGroupUseCase(capture(groupSlot)) } returns Result.success("group-id")

            onEvent(CreateGroupUiEvent.NameChanged("Solo Trip"))

            // When
            viewModel.onEvent(CreateGroupUiEvent.SubmitCreateGroup) {}
            advanceUntilIdle()

            // Then
            assertTrue(groupSlot.captured.members.isEmpty())
        }

        @Test
        fun `emits ShowSuccess action on successful creation`() = runTest(testDispatcher) {
            // Given
            coEvery { createGroupUseCase(any()) } returns Result.success("group-id")
            onEvent(CreateGroupUiEvent.NameChanged("Trip"))

            // When
            val actions = mutableListOf<CreateGroupUiAction>()
            val collectJob = launch { viewModel.actions.collect { actions.add(it) } }

            viewModel.onEvent(CreateGroupUiEvent.SubmitCreateGroup) {}
            advanceUntilIdle()

            // Then
            assertEquals(1, actions.size)
            assertTrue(actions[0] is CreateGroupUiAction.ShowSuccess)
            collectJob.cancel()
        }

        @Test
        fun `emits ShowError action on creation failure`() = runTest(testDispatcher) {
            // Given
            coEvery { createGroupUseCase(any()) } returns Result.failure(RuntimeException("Failed"))
            onEvent(CreateGroupUiEvent.NameChanged("Trip"))

            // When
            val actions = mutableListOf<CreateGroupUiAction>()
            val collectJob = launch { viewModel.actions.collect { actions.add(it) } }

            viewModel.onEvent(CreateGroupUiEvent.SubmitCreateGroup) {}
            advanceUntilIdle()

            // Then
            assertEquals(1, actions.size)
            assertTrue(actions[0] is CreateGroupUiAction.ShowError)
            collectJob.cancel()
        }

        @Test
        fun `does not submit when name is blank`() = runTest(testDispatcher) {
            // Given — name not set (blank)
            viewModel.onEvent(CreateGroupUiEvent.SubmitCreateGroup) {}
            advanceUntilIdle()

            // Then
            assertFalse(viewModel.uiState.value.isNameValid)
            coVerify(exactly = 0) { createGroupUseCase(any()) }
        }
    }
}

package es.pedrazamiguez.expenseshareapp.features.contribution.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.ContributionValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddContributionUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsUseCase
import es.pedrazamiguez.expenseshareapp.features.contribution.presentation.mapper.AddContributionUiMapper
import es.pedrazamiguez.expenseshareapp.features.contribution.presentation.viewmodel.action.AddContributionUiAction
import es.pedrazamiguez.expenseshareapp.features.contribution.presentation.viewmodel.event.AddContributionUiEvent
import es.pedrazamiguez.expenseshareapp.features.contribution.presentation.viewmodel.state.AddContributionStep
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("AddContributionViewModel")
class AddContributionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var addContributionUseCase: AddContributionUseCase
    private lateinit var getGroupByIdUseCase: GetGroupByIdUseCase
    private lateinit var getGroupSubunitsUseCase: GetGroupSubunitsUseCase
    private lateinit var authenticationService: AuthenticationService
    private lateinit var contributionValidationService: ContributionValidationService
    private lateinit var addContributionUiMapper: AddContributionUiMapper
    private lateinit var viewModel: AddContributionViewModel

    private val testGroup = Group(
        id = "group-1",
        name = "Trip",
        currency = "EUR",
        members = listOf("user-1", "user-2")
    )

    private val testSubunit = Subunit(
        id = "subunit-1",
        groupId = "group-1",
        name = "Couple A",
        memberIds = listOf("user-1", "user-2")
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        addContributionUseCase = mockk(relaxed = true)
        getGroupByIdUseCase = mockk()
        getGroupSubunitsUseCase = mockk()
        authenticationService = mockk()
        contributionValidationService = ContributionValidationService()
        addContributionUiMapper = mockk(relaxed = true)

        every { addContributionUiMapper.resolveCurrencySymbol(any()) } returns "€"
        every { addContributionUiMapper.formatInputAmountWithCurrency(any(), any()) } returns "100,00 €"

        viewModel = AddContributionViewModel(
            addContributionUseCase = addContributionUseCase,
            getGroupByIdUseCase = getGroupByIdUseCase,
            getGroupSubunitsUseCase = getGroupSubunitsUseCase,
            authenticationService = authenticationService,
            contributionValidationService = contributionValidationService,
            addContributionUiMapper = addContributionUiMapper
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── LoadSubunitOptions ──────────────────────────────────────────────────

    @Nested
    @DisplayName("LoadSubunitOptions")
    inner class LoadSubunitOptions {

        @Test
        fun `happy path populates subunitOptions and resets form`() =
            runTest(testDispatcher) {
                coEvery { getGroupByIdUseCase("group-1") } returns testGroup
                coEvery { authenticationService.currentUserId() } returns "user-1"
                coEvery { getGroupSubunitsUseCase("group-1") } returns listOf(testSubunit)

                viewModel.onEvent(AddContributionUiEvent.LoadSubunitOptions("group-1"))
                advanceUntilIdle()

                val state = viewModel.uiState.value
                assertEquals(1, state.subunitOptions.size)
                assertEquals("subunit-1", state.subunitOptions[0].id)
                assertEquals("EUR", state.groupCurrencyCode)
                assertEquals("€", state.groupCurrencySymbol)
                assertEquals(PayerType.USER, state.contributionScope)
                assertNull(state.selectedSubunitId)
                assertEquals("", state.amountInput)
                assertFalse(state.amountError)
            }

        @Test
        fun `null groupId is a no-op`() = runTest(testDispatcher) {
            viewModel.onEvent(AddContributionUiEvent.LoadSubunitOptions(null))
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.subunitOptions.isEmpty())
            coVerify(exactly = 0) { getGroupByIdUseCase(any()) }
        }

        @Test
        fun `failure emits ShowError action`() = runTest(testDispatcher) {
            coEvery { getGroupByIdUseCase("group-1") } throws RuntimeException("Network")

            val emitted = mutableListOf<AddContributionUiAction>()
            val collectJob = launch {
                viewModel.actions.collect { emitted.add(it) }
            }

            viewModel.onEvent(AddContributionUiEvent.LoadSubunitOptions("group-1"))
            advanceUntilIdle()

            assertTrue(emitted.any { it is AddContributionUiAction.ShowError })
            collectJob.cancel()
        }

        @Test
        fun `filters subunits to only those containing current user`() =
            runTest(testDispatcher) {
                val otherSubunit = Subunit(
                    id = "subunit-2",
                    groupId = "group-1",
                    name = "Other Couple",
                    memberIds = listOf("user-3", "user-4")
                )
                coEvery { getGroupByIdUseCase("group-1") } returns testGroup
                coEvery { authenticationService.currentUserId() } returns "user-1"
                coEvery { getGroupSubunitsUseCase("group-1") } returns listOf(
                    testSubunit,
                    otherSubunit
                )

                viewModel.onEvent(AddContributionUiEvent.LoadSubunitOptions("group-1"))
                advanceUntilIdle()

                assertEquals(1, viewModel.uiState.value.subunitOptions.size)
                assertEquals("subunit-1", viewModel.uiState.value.subunitOptions[0].id)
            }
    }

    // ── UpdateAmount ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UpdateAmount")
    inner class UpdateAmount {

        @Test
        fun `updates amountInput and clears error`() = runTest(testDispatcher) {
            viewModel.onEvent(AddContributionUiEvent.UpdateAmount("50"))

            assertEquals("50", viewModel.uiState.value.amountInput)
            assertFalse(viewModel.uiState.value.amountError)
        }
    }

    // ── NextStep / PreviousStep ─────────────────────────────────────────────

    @Nested
    @DisplayName("NextStep")
    inner class NextStep {

        @Test
        fun `advances from AMOUNT to SCOPE when amount is valid`() =
            runTest(testDispatcher) {
                seedGroup()
                viewModel.onEvent(AddContributionUiEvent.UpdateAmount("100"))
                viewModel.onEvent(AddContributionUiEvent.NextStep)

                assertEquals(AddContributionStep.SCOPE, viewModel.uiState.value.currentStep)
            }

        @Test
        fun `sets amountError when amount is invalid on AMOUNT step`() =
            runTest(testDispatcher) {
                viewModel.onEvent(AddContributionUiEvent.NextStep)

                assertTrue(viewModel.uiState.value.amountError)
                assertEquals(AddContributionStep.AMOUNT, viewModel.uiState.value.currentStep)
            }

        @Test
        fun `advancing to REVIEW formats amount with currency`() =
            runTest(testDispatcher) {
                seedGroup()
                viewModel.onEvent(AddContributionUiEvent.UpdateAmount("100"))
                viewModel.onEvent(AddContributionUiEvent.NextStep) // → SCOPE
                viewModel.onEvent(AddContributionUiEvent.NextStep) // → REVIEW

                assertEquals(AddContributionStep.REVIEW, viewModel.uiState.value.currentStep)
                assertEquals(
                    "100,00 €",
                    viewModel.uiState.value.formattedAmountWithCurrency
                )
            }

        private fun kotlinx.coroutines.test.TestScope.seedGroup() {
            coEvery { getGroupByIdUseCase("group-1") } returns testGroup
            coEvery { authenticationService.currentUserId() } returns "user-1"
            coEvery { getGroupSubunitsUseCase("group-1") } returns emptyList()
            viewModel.onEvent(AddContributionUiEvent.LoadSubunitOptions("group-1"))
            advanceUntilIdle()
        }
    }

    @Nested
    @DisplayName("PreviousStep")
    inner class PreviousStep {

        @Test
        fun `goes back from SCOPE to AMOUNT`() = runTest(testDispatcher) {
            coEvery { getGroupByIdUseCase("group-1") } returns testGroup
            coEvery { authenticationService.currentUserId() } returns "user-1"
            coEvery { getGroupSubunitsUseCase("group-1") } returns emptyList()
            viewModel.onEvent(AddContributionUiEvent.LoadSubunitOptions("group-1"))
            advanceUntilIdle()

            viewModel.onEvent(AddContributionUiEvent.UpdateAmount("100"))
            viewModel.onEvent(AddContributionUiEvent.NextStep) // → SCOPE
            viewModel.onEvent(AddContributionUiEvent.PreviousStep) // → AMOUNT

            assertEquals(AddContributionStep.AMOUNT, viewModel.uiState.value.currentStep)
        }

        @Test
        fun `emits NavigateBack on first step`() = runTest(testDispatcher) {
            val emitted = mutableListOf<AddContributionUiAction>()
            val collectJob = launch {
                viewModel.actions.collect { emitted.add(it) }
            }

            viewModel.onEvent(AddContributionUiEvent.PreviousStep)
            advanceUntilIdle()

            assertTrue(emitted.any { it is AddContributionUiAction.NavigateBack })
            collectJob.cancel()
        }
    }

    // ── ContributionScopeSelected ───────────────────────────────────────────

    @Nested
    @DisplayName("ContributionScopeSelected")
    inner class ContributionScopeSelected {

        @Test
        fun `USER scope clears subunitId`() = runTest(testDispatcher) {
            viewModel.onEvent(
                AddContributionUiEvent.ContributionScopeSelected(
                    PayerType.USER,
                    subunitId = "subunit-1"
                )
            )

            assertEquals(PayerType.USER, viewModel.uiState.value.contributionScope)
            assertNull(viewModel.uiState.value.selectedSubunitId)
        }

        @Test
        fun `SUBUNIT scope sets subunitId`() = runTest(testDispatcher) {
            viewModel.onEvent(
                AddContributionUiEvent.ContributionScopeSelected(
                    PayerType.SUBUNIT,
                    subunitId = "subunit-1"
                )
            )

            assertEquals(PayerType.SUBUNIT, viewModel.uiState.value.contributionScope)
            assertEquals("subunit-1", viewModel.uiState.value.selectedSubunitId)
        }
    }

    // ── Submit ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Submit")
    inner class Submit {

        @Test
        fun `null groupId is a no-op`() = runTest(testDispatcher) {
            viewModel.onEvent(AddContributionUiEvent.Submit(null))
            advanceUntilIdle()

            coVerify(exactly = 0) { addContributionUseCase(any(), any()) }
        }

        @Test
        fun `invalid amount sets amountError`() = runTest(testDispatcher) {
            seedGroupWithAmount(null)

            viewModel.onEvent(AddContributionUiEvent.Submit("group-1"))
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.amountError)
            coVerify(exactly = 0) { addContributionUseCase(any(), any()) }
        }

        @Test
        fun `invalid subunitId emits ShowError`() = runTest(testDispatcher) {
            seedGroupWithAmount("100")

            viewModel.onEvent(
                AddContributionUiEvent.ContributionScopeSelected(
                    PayerType.SUBUNIT,
                    subunitId = "invalid-subunit"
                )
            )

            val emitted = mutableListOf<AddContributionUiAction>()
            val collectJob = launch {
                viewModel.actions.collect { emitted.add(it) }
            }

            viewModel.onEvent(AddContributionUiEvent.Submit("group-1"))
            advanceUntilIdle()

            assertTrue(emitted.any { it is AddContributionUiAction.ShowError })
            collectJob.cancel()
        }

        @Test
        fun `happy path calls use case and emits ShowSuccess`() =
            runTest(testDispatcher) {
                seedGroupWithAmount("100")
                coEvery { addContributionUseCase(any(), any()) } just Runs

                val emitted = mutableListOf<AddContributionUiAction>()
                val collectJob = launch {
                    viewModel.actions.collect { emitted.add(it) }
                }

                var callbackInvoked = false
                viewModel.onEvent(AddContributionUiEvent.Submit("group-1")) {
                    callbackInvoked = true
                }
                advanceUntilIdle()

                assertTrue(emitted.any { it is AddContributionUiAction.ShowSuccess })
                assertTrue(callbackInvoked)
                assertFalse(viewModel.uiState.value.isLoading)
                coVerify { addContributionUseCase("group-1", any()) }
                collectJob.cancel()
            }

        @Test
        fun `use case failure emits ShowError`() = runTest(testDispatcher) {
            seedGroupWithAmount("100")
            coEvery { addContributionUseCase(any(), any()) } throws RuntimeException("Boom")

            val emitted = mutableListOf<AddContributionUiAction>()
            val collectJob = launch {
                viewModel.actions.collect { emitted.add(it) }
            }

            viewModel.onEvent(AddContributionUiEvent.Submit("group-1"))
            advanceUntilIdle()

            assertTrue(emitted.any { it is AddContributionUiAction.ShowError })
            assertFalse(viewModel.uiState.value.isLoading)
            collectJob.cancel()
        }

        private fun kotlinx.coroutines.test.TestScope.seedGroupWithAmount(amount: String?) {
            coEvery { getGroupByIdUseCase("group-1") } returns testGroup
            coEvery { authenticationService.currentUserId() } returns "user-1"
            coEvery { getGroupSubunitsUseCase("group-1") } returns listOf(testSubunit)
            viewModel.onEvent(AddContributionUiEvent.LoadSubunitOptions("group-1"))
            advanceUntilIdle()
            if (amount != null) {
                viewModel.onEvent(AddContributionUiEvent.UpdateAmount(amount))
            }
        }
    }
}


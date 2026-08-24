package es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.domain.usecase.auth.IsUserAnonymousUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.GetCurrentUserProfileUseCase
import es.pedrazamiguez.splittrip.features.settings.R
import es.pedrazamiguez.splittrip.features.settings.presentation.mapper.SubscriptionsUiMapper
import es.pedrazamiguez.splittrip.features.settings.presentation.model.BillingInterval
import es.pedrazamiguez.splittrip.features.settings.presentation.model.SubscriptionPlanUiModel
import es.pedrazamiguez.splittrip.features.settings.presentation.model.SubscriptionTier
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.action.SubscriptionsUiAction
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.event.SubscriptionsUiEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("SubscriptionsViewModel")
class SubscriptionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getCurrentUserProfileUseCase: GetCurrentUserProfileUseCase
    private lateinit var isUserAnonymousUseCase: IsUserAnonymousUseCase
    private lateinit var subscriptionsUiMapper: SubscriptionsUiMapper

    private val testUser = User(
        userId = "user_123",
        email = "test@example.com",
        displayName = "Test User"
    )

    private val mockFreePlan = SubscriptionPlanUiModel(
        tier = SubscriptionTier.FREE,
        title = UiText.StringResource(R.string.subscriptions_tier_free_title),
        description = UiText.StringResource(R.string.subscriptions_tier_free_description),
        price = UiText.StringResource(R.string.subscriptions_tier_free_price),
        period = UiText.StringResource(R.string.subscriptions_tier_free_period),
        badge = null,
        features = persistentListOf(),
        isCurrentPlan = true,
        ctaButtonText = UiText.StringResource(R.string.subscriptions_cta_current_plan),
        isCtaButtonEnabled = false,
        isHighlightedCard = false
    )

    private val mockProPlan = SubscriptionPlanUiModel(
        tier = SubscriptionTier.PRO,
        title = UiText.StringResource(R.string.subscriptions_tier_pro_title),
        description = UiText.StringResource(R.string.subscriptions_tier_pro_description),
        price = UiText.StringResource(R.string.subscriptions_tier_pro_price_annual),
        period = UiText.StringResource(R.string.subscriptions_period_annual_billed),
        badge = UiText.StringResource(R.string.subscriptions_badge_popular),
        features = persistentListOf(),
        isCurrentPlan = false,
        ctaButtonText = UiText.StringResource(R.string.subscriptions_cta_upgrade_pro),
        isCtaButtonEnabled = true,
        isHighlightedCard = true
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getCurrentUserProfileUseCase = mockk()
        isUserAnonymousUseCase = mockk()
        subscriptionsUiMapper = mockk()

        coEvery { getCurrentUserProfileUseCase() } returns testUser
        every { isUserAnonymousUseCase() } returns flowOf(false)
        every {
            subscriptionsUiMapper.mapPlans(any(), any())
        } returns persistentListOf(mockFreePlan, mockProPlan)
        every { subscriptionsUiMapper.formatUpgradeSuccessMessage(any()) } returns UiText.StringResource(
            R.string.subscriptions_upgrade_success,
            UiText.StringResource(R.string.subscriptions_tier_pro_title)
        )
        every { subscriptionsUiMapper.formatRestorePurchasesSuccessMessage() } returns UiText.StringResource(
            R.string.subscriptions_restore_success
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SubscriptionsViewModel = SubscriptionsViewModel(
        getCurrentUserProfileUseCase = getCurrentUserProfileUseCase,
        isUserAnonymousUseCase = isUserAnonymousUseCase,
        subscriptionsUiMapper = subscriptionsUiMapper
    )

    @Nested
    @DisplayName("Initial state loading")
    inner class InitialState {

        @Test
        fun `initial state loads profile, checks anonymous status, and maps plans`() = runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertFalse(state.isAnonymous)
            assertEquals(BillingInterval.ANNUAL, state.selectedInterval)
            assertEquals(SubscriptionTier.FREE, state.currentTier)
            assertEquals(2, state.plans.size)
            assertFalse(state.isProcessingAction)
        }

        @Test
        fun `guest user state sets isAnonymous to true`() = runTest(testDispatcher) {
            every { isUserAnonymousUseCase() } returns flowOf(true)
            coEvery { getCurrentUserProfileUseCase() } returns null

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.isAnonymous)
        }

        @Test
        fun `LoadSubscriptions event triggers reload`() = runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(SubscriptionsUiEvent.LoadSubscriptions)
            advanceUntilIdle()

            coVerify(atLeast = 2) { getCurrentUserProfileUseCase() }
        }

        @Test
        fun `loadSubscriptions handles exception gracefully`() = runTest(testDispatcher) {
            coEvery { getCurrentUserProfileUseCase() } throws RuntimeException("Network error")

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
        }
    }

    @Nested
    @DisplayName("SelectBillingInterval")
    inner class SelectBillingInterval {

        @Test
        fun `SelectBillingInterval updates interval and re-maps plans`() = runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(SubscriptionsUiEvent.SelectBillingInterval(BillingInterval.MONTHLY))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(BillingInterval.MONTHLY, state.selectedInterval)
            coVerify {
                subscriptionsUiMapper.mapPlans(
                    currentTier = SubscriptionTier.FREE,
                    selectedInterval = BillingInterval.MONTHLY
                )
            }
        }
    }

    @Nested
    @DisplayName("UpgradePlan")
    inner class UpgradePlan {

        @Test
        fun `UpgradePlan emits ShowTopPill with formatted upgrade success message`() = runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val actions = mutableListOf<SubscriptionsUiAction>()
            val job = launch { viewModel.actions.collect { actions.add(it) } }

            viewModel.onEvent(SubscriptionsUiEvent.UpgradePlan(SubscriptionTier.PRO))
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isProcessingAction)
            assertEquals(1, actions.size)
            val action = assertInstanceOf(SubscriptionsUiAction.ShowTopPill::class.java, actions.first())
            val message = assertInstanceOf(UiText.StringResource::class.java, action.message)
            assertEquals(R.string.subscriptions_upgrade_success, message.resId)

            job.cancel()
        }

        @Test
        fun `UpgradePlan handles exception gracefully`() = runTest(testDispatcher) {
            every { subscriptionsUiMapper.formatUpgradeSuccessMessage(any()) } throws RuntimeException("Error")

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(SubscriptionsUiEvent.UpgradePlan(SubscriptionTier.PRO))
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isProcessingAction)
        }
    }

    @Nested
    @DisplayName("RestorePurchases")
    inner class RestorePurchases {

        @Test
        fun `RestorePurchases emits ShowTopPill with formatted restore success message`() = runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val actions = mutableListOf<SubscriptionsUiAction>()
            val job = launch { viewModel.actions.collect { actions.add(it) } }

            viewModel.onEvent(SubscriptionsUiEvent.RestorePurchases)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isProcessingAction)
            assertEquals(1, actions.size)
            val action = assertInstanceOf(SubscriptionsUiAction.ShowTopPill::class.java, actions.first())
            val message = assertInstanceOf(UiText.StringResource::class.java, action.message)
            assertEquals(R.string.subscriptions_restore_success, message.resId)

            job.cancel()
        }

        @Test
        fun `RestorePurchases handles exception gracefully`() = runTest(testDispatcher) {
            every { subscriptionsUiMapper.formatRestorePurchasesSuccessMessage() } throws RuntimeException("Error")

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(SubscriptionsUiEvent.RestorePurchases)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isProcessingAction)
        }
    }
}

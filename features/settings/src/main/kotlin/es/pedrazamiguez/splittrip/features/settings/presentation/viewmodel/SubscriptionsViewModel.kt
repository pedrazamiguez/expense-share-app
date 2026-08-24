package es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.splittrip.domain.usecase.auth.IsUserAnonymousUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.GetCurrentUserProfileUseCase
import es.pedrazamiguez.splittrip.features.settings.presentation.mapper.SubscriptionsUiMapper
import es.pedrazamiguez.splittrip.features.settings.presentation.model.BillingInterval
import es.pedrazamiguez.splittrip.features.settings.presentation.model.SubscriptionTier
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.action.SubscriptionsUiAction
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.event.SubscriptionsUiEvent
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.state.SubscriptionsUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class SubscriptionsViewModel(
    private val getCurrentUserProfileUseCase: GetCurrentUserProfileUseCase,
    private val isUserAnonymousUseCase: IsUserAnonymousUseCase,
    private val subscriptionsUiMapper: SubscriptionsUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionsUiState())
    val uiState: StateFlow<SubscriptionsUiState> = _uiState.asStateFlow()

    private val _actions = Channel<SubscriptionsUiAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    init {
        loadSubscriptions()
    }

    fun onEvent(event: SubscriptionsUiEvent) {
        when (event) {
            SubscriptionsUiEvent.LoadSubscriptions -> loadSubscriptions()
            is SubscriptionsUiEvent.SelectBillingInterval -> handleSelectBillingInterval(event.interval)
            is SubscriptionsUiEvent.UpgradePlan -> handleUpgradePlan(event.tier)
            SubscriptionsUiEvent.RestorePurchases -> handleRestorePurchases()
        }
    }

    private fun loadSubscriptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val isAnon = isUserAnonymousUseCase().firstOrNull() ?: false
                getCurrentUserProfileUseCase()
                val currentTier = SubscriptionTier.FREE
                val selectedInterval = _uiState.value.selectedInterval
                val plans = subscriptionsUiMapper.mapPlans(
                    currentTier = currentTier,
                    selectedInterval = selectedInterval
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAnonymous = isAnon,
                        currentTier = currentTier,
                        plans = plans
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to load subscriptions")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun handleSelectBillingInterval(interval: BillingInterval) {
        val currentTier = _uiState.value.currentTier
        val updatedPlans = subscriptionsUiMapper.mapPlans(
            currentTier = currentTier,
            selectedInterval = interval
        )
        _uiState.update {
            it.copy(
                selectedInterval = interval,
                plans = updatedPlans
            )
        }
    }

    private fun handleUpgradePlan(tier: SubscriptionTier) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingAction = true) }
            try {
                val message = subscriptionsUiMapper.formatUpgradeSuccessMessage(tier)
                _actions.send(SubscriptionsUiAction.ShowTopPill(message))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to handle upgrade plan")
            } finally {
                _uiState.update { it.copy(isProcessingAction = false) }
            }
        }
    }

    private fun handleRestorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingAction = true) }
            try {
                val message = subscriptionsUiMapper.formatRestorePurchasesSuccessMessage()
                _actions.send(SubscriptionsUiAction.ShowTopPill(message))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to handle restore purchases")
            } finally {
                _uiState.update { it.copy(isProcessingAction = false) }
            }
        }
    }
}

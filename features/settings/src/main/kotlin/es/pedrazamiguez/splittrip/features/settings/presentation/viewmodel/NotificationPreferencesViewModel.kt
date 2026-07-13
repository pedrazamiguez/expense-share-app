package es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.splittrip.core.common.constant.AppConstants
import es.pedrazamiguez.splittrip.domain.usecase.notification.GetNotificationPreferencesUseCase
import es.pedrazamiguez.splittrip.domain.usecase.notification.UpdateNotificationPreferenceUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.ObserveCurrentUserProfileUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.UpdateUserReminderPreferencesUseCase
import es.pedrazamiguez.splittrip.features.settings.presentation.model.NotificationPreferencesUiEvent
import es.pedrazamiguez.splittrip.features.settings.presentation.model.NotificationPreferencesUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationPreferencesViewModel(
    private val getNotificationPreferencesUseCase: GetNotificationPreferencesUseCase,
    private val updateNotificationPreferenceUseCase: UpdateNotificationPreferenceUseCase,
    private val observeCurrentUserProfileUseCase: ObserveCurrentUserProfileUseCase,
    private val updateUserReminderPreferencesUseCase: UpdateUserReminderPreferencesUseCase
) : ViewModel() {

    val uiState: StateFlow<NotificationPreferencesUiState> = combine(
        getNotificationPreferencesUseCase(),
        observeCurrentUserProfileUseCase()
    ) { prefs, profile ->
        NotificationPreferencesUiState(
            membershipEnabled = prefs.membershipEnabled,
            expensesEnabled = prefs.expensesEnabled,
            financialEnabled = prefs.financialEnabled,
            timezone = profile?.timezone,
            preferredReminderTime = profile?.preferredReminderTime,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = AppConstants.FLOW_RETENTION_TIME,
            replayExpirationMillis = AppConstants.FLOW_REPLAY_EXPIRATION
        ),
        initialValue = NotificationPreferencesUiState()
    )

    fun onEvent(event: NotificationPreferencesUiEvent) {
        when (event) {
            is NotificationPreferencesUiEvent.ToggleCategory -> handleToggleCategory(event)
            is NotificationPreferencesUiEvent.UpdateReminderPreferences -> handleUpdateReminderPreferences(event)
        }
    }

    private fun handleToggleCategory(event: NotificationPreferencesUiEvent.ToggleCategory) {
        viewModelScope.launch {
            try {
                updateNotificationPreferenceUseCase(event.category, event.enabled)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to update notification preference")
            }
        }
    }

    private fun handleUpdateReminderPreferences(event: NotificationPreferencesUiEvent.UpdateReminderPreferences) {
        viewModelScope.launch {
            try {
                val profile = observeCurrentUserProfileUseCase().firstOrNull()
                if (profile != null) {
                    updateUserReminderPreferencesUseCase(profile.userId, event.timezone, event.time)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to update reminder preferences")
            }
        }
    }
}

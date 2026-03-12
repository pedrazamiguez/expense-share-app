package es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.GetNotificationPreferencesUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.UpdateNotificationPreferenceUseCase
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.model.NotificationPreferencesUiEvent
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.model.NotificationPreferencesUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationPreferencesViewModel(
    private val getNotificationPreferencesUseCase: GetNotificationPreferencesUseCase,
    private val updateNotificationPreferenceUseCase: UpdateNotificationPreferenceUseCase
) : ViewModel() {

    val uiState: StateFlow<NotificationPreferencesUiState> =
        getNotificationPreferencesUseCase().map { prefs ->
            NotificationPreferencesUiState(
                membershipEnabled = prefs.membershipEnabled,
                expensesEnabled = prefs.expensesEnabled,
                financialEnabled = prefs.financialEnabled,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConstants.FLOW_RETENTION_TIME),
            initialValue = NotificationPreferencesUiState()
        )

    fun onEvent(event: NotificationPreferencesUiEvent) {
        when (event) {
            is NotificationPreferencesUiEvent.ToggleCategory -> {
                viewModelScope.launch {
                    try {
                        updateNotificationPreferenceUseCase(event.category, event.enabled)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to update notification preference")
                    }
                }
            }
        }
    }
}


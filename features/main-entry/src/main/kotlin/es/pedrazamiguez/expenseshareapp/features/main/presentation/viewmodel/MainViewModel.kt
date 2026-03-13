package es.pedrazamiguez.expenseshareapp.features.main.presentation.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.SyncPendingTokenUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class MainViewModel(
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase,
    private val syncPendingTokenUseCase: SyncPendingTokenUseCase
) : ViewModel() {

    private val bundles = ConcurrentHashMap<String, Bundle?>()

    fun getBundle(route: String): Bundle? = bundles[route]

    fun setBundle(
        route: String, bundle: Bundle?
    ) {
        if (bundle != null) {
            bundles[route] = bundle
        } else {
            bundles.remove(route)
        }
    }

    fun clearInvisibleBundles(visibleRoutes: Set<String>) {
        bundles.entries.removeAll { it.key !in visibleRoutes }
    }

    fun clearAllBundles() {
        bundles.clear()
    }

    init {
        checkSessionAndRegisterDevice()
    }

    private fun checkSessionAndRegisterDevice() {
        viewModelScope.launch {
            // First, sync any pending token that was persisted due to a prior failed registration
            syncPendingTokenUseCase().onFailure {
                Timber.w(it, "Could not sync pending FCM token")
            }

            registerDeviceTokenUseCase().onFailure {
                Timber.w("Could not refresh FCM token on app start")
            }
        }
    }

}

package es.pedrazamiguez.expenseshareapp.features.main.presentation.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase
) : ViewModel() {

    private val bundles = ConcurrentHashMap<String, Bundle?>()

    fun getBundle(route: String): Bundle? = bundles[route]

    fun setBundle(route: String, bundle: Bundle?) {
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
        ensureDeviceTokenRegistered()
    }

    private fun ensureDeviceTokenRegistered() {
        viewModelScope.launch {
            Timber.d("MainViewModel: starting FCM token registration")
            registerDeviceTokenUseCase()
                .onSuccess {
                    Timber.i("MainViewModel: FCM token registration completed successfully")
                }
                .onFailure {
                    Timber.e(it, "MainViewModel: FCM token registration FAILED")
                }
        }
    }
}

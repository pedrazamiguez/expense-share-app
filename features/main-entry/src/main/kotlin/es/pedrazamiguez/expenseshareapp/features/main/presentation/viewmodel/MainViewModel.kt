package es.pedrazamiguez.expenseshareapp.features.main.presentation.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase,
    private val getGroupByIdUseCase: GetGroupByIdUseCase
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

    /**
     * Resolves the group name for a deep link group ID.
     *
     * Performs a Room-first lookup via [GetGroupByIdUseCase].
     * Returns `null` if the group is not found locally (e.g., not yet synced).
     *
     * @param groupId The group ID from the deep link URI.
     * @return The group name, or `null` if the group is not found.
     */
    suspend fun resolveGroupName(groupId: String): String? {
        return try {
            getGroupByIdUseCase(groupId)?.name
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to resolve group name for deep link groupId=%s", groupId)
            null
        }
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

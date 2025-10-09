package es.pedrazamiguez.expenseshareapp.ui.main.presentation.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import java.util.concurrent.ConcurrentHashMap

class MainViewModel : ViewModel() {

    private val bundles = ConcurrentHashMap<String, Bundle?>()

    fun getBundle(route: String): Bundle? = bundles[route]

    fun setBundle(
        route: String,
        bundle: Bundle?
    ) {
        bundles[route] = bundle
    }

    fun clearInvisibleBundles(visibleRoutes: Set<String>) {
        bundles.entries.removeAll { it.key !in visibleRoutes }
    }

    fun clearAllBundles() {
        bundles.clear()
    }

}

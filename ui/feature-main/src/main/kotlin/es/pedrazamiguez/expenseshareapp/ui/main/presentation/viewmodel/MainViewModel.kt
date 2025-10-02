package es.pedrazamiguez.expenseshareapp.ui.main.presentation.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val bundles = mutableMapOf<String, Bundle?>()

    fun getBundle(route: String): Bundle? = bundles[route]

    fun setBundle(
        route: String,
        bundle: Bundle?
    ) {
        bundles[route] = bundle
    }

}

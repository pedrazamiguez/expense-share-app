package es.pedrazamiguez.expenseshareapp.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * A Timber tree that forwards logs to Firebase Crashlytics.
 * Only logs warnings and errors are sent to Crashlytics to avoid noise.
 */
class CrashlyticsTree : Timber.Tree() {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Only log warnings and errors to Crashlytics
        if (priority < Log.WARN) return

        crashlytics.log("${priorityToString(priority)}/$tag: $message")

        t?.let {
            crashlytics.recordException(it)
        }
    }

    private fun priorityToString(priority: Int): String = when (priority) {
        Log.VERBOSE -> "V"
        Log.DEBUG -> "D"
        Log.INFO -> "I"
        Log.WARN -> "W"
        Log.ERROR -> "E"
        Log.ASSERT -> "A"
        else -> "?"
    }
}

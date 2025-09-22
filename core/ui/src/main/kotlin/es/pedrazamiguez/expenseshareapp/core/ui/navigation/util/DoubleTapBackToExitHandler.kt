package es.pedrazamiguez.expenseshareapp.core.ui.navigation.util

import java.util.concurrent.atomic.AtomicLong

/**
 * Handles the "press back twice to exit" pattern with thread safety.
 * @param intervalMillis The maximum time allowed between back presses
 * @param timeProvider Function that provides the current time (for better testability)
 */
class DoubleTapBackToExitHandler(
    private val intervalMillis: Long = 2000,
    private val timeProvider: () -> Long = System::currentTimeMillis
) {

    // Using AtomicLong for thread safety
    private val lastBackPressTime = AtomicLong(-intervalMillis)

    /**
     * Determines if the application should exit based on back press timing.
     * @return true if this is the second back press within the interval, false otherwise
     */
    fun shouldExit(): Boolean {
        val now = timeProvider()
        val last = lastBackPressTime.get()

        return if (now - last < intervalMillis) {
            true
        } else {
            lastBackPressTime.set(now)
            false
        }
    }

    /**
     * Resets the handler state, clearing the last recorded back press.
     */
    fun reset() {
        lastBackPressTime.set(0)
    }

}

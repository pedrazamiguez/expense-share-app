package es.pedrazamiguez.expenseshareapp.core.designsystem.constant

object UiConstants {
    const val NAV_FEEDBACK_DELAY = 200L
    const val SCROLL_POSITION_DEBOUNCE_MS = 300L

    /**
     * Delay (ms) before showing a loading indicator (e.g. shimmer skeleton).
     * If data arrives within this window, the loading UI is never shown at all,
     * preventing the "flash of loading state" flicker.
     */
    const val LOADING_SHOW_DELAY_MS = 150L

    /**
     * Minimum time (ms) a loading indicator stays visible once it appears.
     * Prevents the loading UI from flashing for just a frame or two when
     * data arrives shortly after the show delay expired.
     */
    const val LOADING_MIN_DISPLAY_TIME_MS = 500L
}

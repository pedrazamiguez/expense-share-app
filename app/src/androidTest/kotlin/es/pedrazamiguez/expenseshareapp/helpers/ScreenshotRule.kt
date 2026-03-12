package es.pedrazamiguez.expenseshareapp.helpers

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File

/**
 * A JUnit 4 [TestWatcher] that captures a full-screen screenshot after
 * every test — both passed and failed.
 *
 * Screenshots are saved to the app's external files directory under a
 * `screenshots/` folder, named as `ClassName.methodName.png`.
 *
 * Usage:
 * ```kotlin
 * @get:Rule(order = Int.MAX_VALUE)
 * val screenshotRule = ScreenshotRule()
 * ```
 *
 * Use a high `order` value so the screenshot is taken **after** the test
 * body completes but while the Compose host (from `createComposeRule()`)
 * is still alive.
 */
class ScreenshotRule : TestWatcher() {

    companion object {
        private const val TAG = "ScreenshotRule"
        private const val SCREENSHOTS_DIR = "screenshots"
    }

    override fun succeeded(description: Description) {
        takeScreenshot(description)
    }

    override fun failed(e: Throwable?, description: Description) {
        takeScreenshot(description)
    }

    private fun takeScreenshot(description: Description) {
        try {
            val device = UiDevice.getInstance(
                InstrumentationRegistry.getInstrumentation()
            )
            device.waitForIdle()

            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val screenshotDir = File(context.getExternalFilesDir(null), SCREENSHOTS_DIR)
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs()
            }

            val className = description.testClass?.simpleName ?: "UnknownClass"
            val methodName = description.methodName ?: "unknownMethod"
            val fileName = "$className.$methodName.png"
            val file = File(screenshotDir, fileName)

            val success = device.takeScreenshot(file)
            if (success) {
                Log.i(TAG, "Screenshot saved: ${file.absolutePath}")
            } else {
                Log.w(TAG, "UiDevice.takeScreenshot() returned false for $fileName")
            }
        } catch (e: Exception) {
            // Never fail a test because of a screenshot error
            Log.e(TAG, "Failed to capture screenshot: ${e.message}", e)
        }
    }
}


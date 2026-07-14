package es.pedrazamiguez.splittrip.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import es.pedrazamiguez.splittrip.core.designsystem.foundation.SplitTripTheme
import es.pedrazamiguez.splittrip.features.settings.R
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.OpenSourceScreen
import es.pedrazamiguez.splittrip.helpers.ScreenshotRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests for [OpenSourceScreen].
 */
@RunWith(AndroidJUnit4::class)
class OpenSourceScreenTest {

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @get:Rule(order = 2)
    val screenshotRule = ScreenshotRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun rendersOpenSource_showsLibraries() {
        val headerText = context.getString(R.string.open_source_libraries_header)

        composeRule.setContent {
            SplitTripTheme {
                OpenSourceScreen(onLibraryUrlClick = {})
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText(headerText).assertIsDisplayed()
        composeRule.onNodeWithText("Koin").assertIsDisplayed()
        composeRule.onNodeWithText("Room").assertIsDisplayed()
    }

    @Test
    fun clickingLibrary_triggersCallback() {
        var clickedUrl: String? = null

        composeRule.setContent {
            SplitTripTheme {
                OpenSourceScreen(onLibraryUrlClick = { url -> clickedUrl = url })
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Koin").performClick()
        composeRule.waitForIdle()

        assertEquals("https://github.com/InsertKoinIO/koin", clickedUrl)
    }
}

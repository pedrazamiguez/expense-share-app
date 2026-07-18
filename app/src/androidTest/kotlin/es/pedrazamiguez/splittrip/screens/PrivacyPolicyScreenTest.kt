package es.pedrazamiguez.splittrip.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import es.pedrazamiguez.splittrip.core.designsystem.foundation.SplitTripTheme
import es.pedrazamiguez.splittrip.features.settings.R
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.PrivacyPolicyScreen
import es.pedrazamiguez.splittrip.helpers.ScreenshotRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests for [PrivacyPolicyScreen].
 */
@RunWith(AndroidJUnit4::class)
class PrivacyPolicyScreenTest {

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @get:Rule(order = 2)
    val screenshotRule = ScreenshotRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun rendersPrivacyPolicy_showsSections() {
        val section1Title = context.getString(R.string.privacy_section_1_title)
        val section2Title = context.getString(R.string.privacy_section_2_title)

        composeRule.setContent {
            SplitTripTheme {
                PrivacyPolicyScreen()
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText(section1Title).assertIsDisplayed()
        composeRule.onNodeWithText(section2Title).assertIsDisplayed()
    }
}

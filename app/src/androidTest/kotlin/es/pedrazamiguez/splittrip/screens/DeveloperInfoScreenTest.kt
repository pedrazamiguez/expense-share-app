package es.pedrazamiguez.splittrip.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import es.pedrazamiguez.splittrip.core.designsystem.foundation.SplitTripTheme
import es.pedrazamiguez.splittrip.features.settings.R
import es.pedrazamiguez.splittrip.features.settings.presentation.model.DeveloperInfoUiState
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.DeveloperInfoScreen
import es.pedrazamiguez.splittrip.helpers.ScreenshotRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests for [DeveloperInfoScreen].
 */
@RunWith(AndroidJUnit4::class)
class DeveloperInfoScreenTest {

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @get:Rule(order = 2)
    val screenshotRule = ScreenshotRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun rendersDeveloperInfo_showsAuthorAndSections() {
        val linksHeader = context.getString(R.string.developer_info_section_links)
        val creditsHeader = context.getString(R.string.developer_info_section_credits)
        val githubText = context.getString(R.string.developer_info_link_github)

        val uiState = DeveloperInfoUiState(
            name = "Andrés Pedraza Míguez",
            role = "Lead Mobile & Systems Engineer",
            bio = "Passionate Android Engineer.",
            avatarUrl = "",
            githubUrl = "https://github.com/pedrazamiguez",
            splitTripRepoUrl = "https://github.com/pedrazamiguez/split-trip",
            linkedinUrl = "https://www.linkedin.com/in/andres-pedraza-miguez/",
            portfolioUrl = "https://pedrazamiguez.es",
            credits = "Built with open-source love.",
            copyright = "© 2026 Andrés Pedraza Míguez. All rights reserved."
        )

        composeRule.setContent {
            SplitTripTheme {
                DeveloperInfoScreen(
                    uiState = uiState,
                    onLinkClick = {}
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Andrés Pedraza Míguez").assertIsDisplayed()
        composeRule.onNodeWithText("Lead Mobile & Systems Engineer").assertIsDisplayed()
        composeRule.onNodeWithText("Passionate Android Engineer.").assertIsDisplayed()
        composeRule.onNodeWithText(linksHeader).assertIsDisplayed()
        composeRule.onNodeWithText(githubText).assertIsDisplayed()
        composeRule.onNodeWithText(creditsHeader).assertIsDisplayed()
        composeRule.onNodeWithText("Built with open-source love.").assertIsDisplayed()
        composeRule.onNodeWithText("© 2026 Andrés Pedraza Míguez. All rights reserved.").assertIsDisplayed()
    }

    @Test
    fun clickingLink_triggersCallbackWithUrl() {
        var clickedUrl: String? = null
        val githubText = context.getString(R.string.developer_info_link_github)

        val uiState = DeveloperInfoUiState(
            name = "Andrés Pedraza Míguez",
            githubUrl = "https://github.com/pedrazamiguez"
        )

        composeRule.setContent {
            SplitTripTheme {
                DeveloperInfoScreen(
                    uiState = uiState,
                    onLinkClick = { url -> clickedUrl = url }
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText(githubText).performClick()
        composeRule.waitForIdle()

        assertEquals("https://github.com/pedrazamiguez", clickedUrl)
    }
}

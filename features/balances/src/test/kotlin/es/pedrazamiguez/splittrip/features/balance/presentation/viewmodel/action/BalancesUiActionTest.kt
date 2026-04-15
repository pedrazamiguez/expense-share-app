package es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for [BalancesUiAction] subtypes that are not yet emitted by any ViewModel or
 * event handler.  Direct instantiation confirms the data classes are constructable and
 * correctly store their [UiText] payload via the sealed interface's [message] property.
 */
class BalancesUiActionTest {

    @Test
    fun `ShowContributionSuccess holds provided message`() {
        val message = UiText.DynamicString("Contribution recorded successfully")
        val action = BalancesUiAction.ShowContributionSuccess(message)

        assertEquals(message, action.message)
    }

    @Test
    fun `ShowContributionError holds provided message`() {
        val message = UiText.DynamicString("Failed to record contribution")
        val action = BalancesUiAction.ShowContributionError(message)

        assertEquals(message, action.message)
    }
}

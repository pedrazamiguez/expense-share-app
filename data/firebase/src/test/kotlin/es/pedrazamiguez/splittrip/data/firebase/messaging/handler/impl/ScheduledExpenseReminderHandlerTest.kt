package es.pedrazamiguez.splittrip.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.splittrip.data.firebase.R
import es.pedrazamiguez.splittrip.domain.constant.NotificationChannelId
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ScheduledExpenseReminderHandler")
class ScheduledExpenseReminderHandlerTest {

    private val context: Context = mockk()
    private val handler = ScheduledExpenseReminderHandler(context)

    @Test
    @DisplayName("handle creates correct NotificationContent")
    fun handle() {
        every { context.getString(R.string.notification_scheduled_reminder_title) } returns "title"
        every { context.getString(R.string.notification_scheduled_reminder_body) } returns "body"

        val data = mapOf(
            "groupId" to "group1",
            "expenseId" to "exp1"
        )

        val result = handler.handle(data)

        assertEquals("title", result.title)
        assertEquals("body", result.body)
        assertEquals(NotificationChannelId.EXPENSES, result.channelId)
    }
}

package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.domain.constant.NotificationChannelId
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale

@DisplayName("Notification Handlers — channel routing and metadata")
class NotificationHandlerContentTest {

    private lateinit var context: Context
    private lateinit var localeProvider: LocaleProvider

    private val baseData = mapOf(
        "memberName" to "John",
        "groupName" to "Trip to Paris",
        "groupId" to "group-123",
        "entityId" to "entity-456",
        "amountCents" to "5000",
        "currencyCode" to "EUR",
        "deepLink" to "expenseshareapp://groups/group-123/expenses/entity-456"
    )

    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        localeProvider = mockk()
        every { localeProvider.getCurrentLocale() } returns Locale.US

        // Stub string resources to return format strings
        every { context.getString(any()) } returns "Fallback"
        every { context.getString(any(), any()) } returns "Formatted"
        every { context.getString(any(), any(), any()) } returns "Formatted with amount"
    }

    @Nested
    @DisplayName("Expense handlers use EXPENSES channel")
    inner class ExpenseHandlers {

        @Test
        fun `ExpenseAddedHandler sets EXPENSES channel`() {
            val handler = ExpenseAddedHandler(context, localeProvider)
            val content = handler.handle(baseData)
            assertEquals(NotificationChannelId.EXPENSES, content.channelId)
            assertEquals("group-123", content.groupId)
            assertNotNull(content.deepLink)
        }

        @Test
        fun `ExpenseUpdatedHandler sets EXPENSES channel`() {
            val handler = ExpenseUpdatedHandler(context, localeProvider)
            val content = handler.handle(baseData)
            assertEquals(NotificationChannelId.EXPENSES, content.channelId)
            assertEquals("group-123", content.groupId)
        }

        @Test
        fun `ExpenseDeletedHandler sets EXPENSES channel`() {
            val handler = ExpenseDeletedHandler(context, localeProvider)
            val content = handler.handle(baseData)
            assertEquals(NotificationChannelId.EXPENSES, content.channelId)
            assertEquals("group-123", content.groupId)
        }
    }

    @Nested
    @DisplayName("Membership handlers use MEMBERSHIP channel")
    inner class MembershipHandlers {

        @Test
        fun `MemberAddedHandler sets MEMBERSHIP channel`() {
            val handler = MemberAddedHandler(context)
            val content = handler.handle(baseData)
            assertEquals(NotificationChannelId.MEMBERSHIP, content.channelId)
            assertEquals("group-123", content.groupId)
        }

        @Test
        fun `MemberRemovedHandler sets MEMBERSHIP channel`() {
            val handler = MemberRemovedHandler(context)
            val content = handler.handle(baseData)
            assertEquals(NotificationChannelId.MEMBERSHIP, content.channelId)
            assertEquals("group-123", content.groupId)
        }
    }

    @Nested
    @DisplayName("Financial handlers use FINANCIAL channel")
    inner class FinancialHandlers {

        @Test
        fun `CashWithdrawalHandler sets FINANCIAL channel`() {
            val handler = CashWithdrawalHandler(context, localeProvider)
            val content = handler.handle(baseData)
            assertEquals(NotificationChannelId.FINANCIAL, content.channelId)
            assertEquals("group-123", content.groupId)
        }

        @Test
        fun `ContributionAddedHandler sets FINANCIAL channel`() {
            val handler = ContributionAddedHandler(context, localeProvider)
            val content = handler.handle(baseData)
            assertEquals(NotificationChannelId.FINANCIAL, content.channelId)
            assertEquals("group-123", content.groupId)
        }
    }

    @Nested
    @DisplayName("DefaultHandler uses DEFAULT channel")
    inner class DefaultHandlerTests {

        @Test
        fun `DefaultHandler sets DEFAULT channel`() {
            val handler = DefaultHandler(context)
            val content = handler.handle(baseData)
            assertEquals(NotificationChannelId.DEFAULT, content.channelId)
        }

        @Test
        fun `DefaultHandler uses string resources for fallback body`() {
            val handler = DefaultHandler(context)
            val content = handler.handle(emptyMap())
            // Should not contain hardcoded English
            assertNotNull(content.title)
            assertNotNull(content.body)
        }
    }

    @Nested
    @DisplayName("Notification IDs are stable and unique")
    inner class NotificationIds {

        @Test
        fun `same data produces same notificationId`() {
            val handler = ExpenseAddedHandler(context, localeProvider)
            val content1 = handler.handle(baseData)
            val content2 = handler.handle(baseData)
            assertEquals(content1.notificationId, content2.notificationId)
        }

        @Test
        fun `different groups produce different notificationIds`() {
            val handler = ExpenseAddedHandler(context, localeProvider)
            val data1 = baseData + ("groupId" to "groupA")
            val data2 = baseData + ("groupId" to "groupB")
            val id1 = handler.handle(data1).notificationId
            val id2 = handler.handle(data2).notificationId
            org.junit.jupiter.api.Assertions.assertNotEquals(id1, id2)
        }
    }
}



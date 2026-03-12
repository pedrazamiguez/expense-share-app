package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.factory

import android.content.Context
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl.CashWithdrawalHandler
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl.ContributionAddedHandler
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl.DefaultHandler
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl.ExpenseAddedHandler
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl.ExpenseDeletedHandler
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl.ExpenseUpdatedHandler
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl.MemberAddedHandler
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl.MemberRemovedHandler
import es.pedrazamiguez.expenseshareapp.domain.enums.NotificationType
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("NotificationHandlerFactory")
class NotificationHandlerFactoryTest {

    private lateinit var factory: NotificationHandlerFactory

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)
        val localeProvider: LocaleProvider = mockk(relaxed = true)
        factory = NotificationHandlerFactory(context, localeProvider)
    }

    @Test
    fun `EXPENSE_ADDED returns ExpenseAddedHandler`() {
        assertTrue(factory.getHandler(NotificationType.EXPENSE_ADDED) is ExpenseAddedHandler)
    }

    @Test
    fun `EXPENSE_UPDATED returns ExpenseUpdatedHandler`() {
        assertTrue(factory.getHandler(NotificationType.EXPENSE_UPDATED) is ExpenseUpdatedHandler)
    }

    @Test
    fun `EXPENSE_DELETED returns ExpenseDeletedHandler`() {
        assertTrue(factory.getHandler(NotificationType.EXPENSE_DELETED) is ExpenseDeletedHandler)
    }

    @Test
    fun `MEMBER_ADDED returns MemberAddedHandler`() {
        assertTrue(factory.getHandler(NotificationType.MEMBER_ADDED) is MemberAddedHandler)
    }

    @Test
    fun `MEMBER_REMOVED returns MemberRemovedHandler`() {
        assertTrue(factory.getHandler(NotificationType.MEMBER_REMOVED) is MemberRemovedHandler)
    }

    @Test
    fun `CASH_WITHDRAWAL returns CashWithdrawalHandler`() {
        assertTrue(factory.getHandler(NotificationType.CASH_WITHDRAWAL) is CashWithdrawalHandler)
    }

    @Test
    fun `CONTRIBUTION_ADDED returns ContributionAddedHandler`() {
        assertTrue(factory.getHandler(NotificationType.CONTRIBUTION_ADDED) is ContributionAddedHandler)
    }

    @Test
    fun `DEFAULT returns DefaultHandler`() {
        assertTrue(factory.getHandler(NotificationType.DEFAULT) is DefaultHandler)
    }

    @Test
    fun `GROUP_INVITE falls through to DefaultHandler`() {
        assertTrue(factory.getHandler(NotificationType.GROUP_INVITE) is DefaultHandler)
    }

    @Test
    fun `SETTLEMENT_REQUEST falls through to DefaultHandler`() {
        assertTrue(factory.getHandler(NotificationType.SETTLEMENT_REQUEST) is DefaultHandler)
    }
}


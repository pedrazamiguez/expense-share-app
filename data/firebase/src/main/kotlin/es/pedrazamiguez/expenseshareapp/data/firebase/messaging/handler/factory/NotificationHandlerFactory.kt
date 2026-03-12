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
import es.pedrazamiguez.expenseshareapp.domain.handler.NotificationHandler

class NotificationHandlerFactory(
    private val context: Context,
    private val localeProvider: LocaleProvider
) {

    fun getHandler(type: NotificationType): NotificationHandler {
        return when (type) {
            NotificationType.EXPENSE_ADDED -> ExpenseAddedHandler(context, localeProvider)
            NotificationType.EXPENSE_UPDATED -> ExpenseUpdatedHandler(context, localeProvider)
            NotificationType.EXPENSE_DELETED -> ExpenseDeletedHandler(context, localeProvider)
            NotificationType.MEMBER_ADDED -> MemberAddedHandler(context)
            NotificationType.MEMBER_REMOVED -> MemberRemovedHandler(context)
            NotificationType.CASH_WITHDRAWAL -> CashWithdrawalHandler(context, localeProvider)
            NotificationType.CONTRIBUTION_ADDED -> ContributionAddedHandler(context, localeProvider)
            else -> DefaultHandler(context)
        }
    }
}

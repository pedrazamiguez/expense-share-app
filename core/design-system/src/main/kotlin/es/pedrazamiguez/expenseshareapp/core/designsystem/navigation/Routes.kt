package es.pedrazamiguez.expenseshareapp.core.designsystem.navigation

object Routes {
    const val LOGIN = "login"
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val PROFILE = "profile"
    const val GROUPS = "groups"
    const val CREATE_GROUP = "create_group"
    const val EXPENSES = "expenses"
    const val ADD_EXPENSE = "add_expense"
    const val BALANCES = "balances"
    const val ADD_CASH_WITHDRAWAL = "add_cash_withdrawal"
    const val SETTINGS = "settings"
    const val SETTINGS_DEFAULT_CURRENCY = "settings_default_currency"
    const val SETTINGS_NOTIFICATIONS = "settings_notifications"
    const val MANAGE_SUBUNITS = "manage_subunits/{groupId}"
    const val CREATE_EDIT_SUBUNIT = "create_edit_subunit/{groupId}?subunitId={subunitId}"

    fun manageSubunitsRoute(groupId: String) = "manage_subunits/$groupId"

    fun createEditSubunitRoute(groupId: String, subunitId: String? = null): String {
        val base = "create_edit_subunit/$groupId"
        return if (subunitId != null) "$base?subunitId=$subunitId" else base
    }
}

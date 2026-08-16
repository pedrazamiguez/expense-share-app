package es.pedrazamiguez.splittrip.core.performance

object PerformanceTraces {
    const val AUTH_SIGN_IN_EMAIL = "auth_sign_in_email"
    const val AUTH_SIGN_UP = "auth_sign_up"
    const val AUTH_SIGN_IN_GOOGLE = "auth_sign_in_google"
    const val AUTH_LOGIN_NAV_TRANSITION = "auth_login_navigation_transition"

    const val EXPENSE_ADD = "expense_add"
    const val EXPENSE_ADD_CASH = "expense_add_cash"
    const val GROUP_CREATE = "group_create"
    const val GROUP_CREATE_FIRESTORE_BATCH = "group_create_firestore_batch"
    const val GROUP_IMAGE_UPLOAD = "group_image_upload"
    const val CONTRIBUTION_ADD = "contribution_add"
    const val CONTRIBUTION_UPDATE = "contribution_update"
    const val WITHDRAWAL_ADD = "withdrawal_add"

    const val SYNC_CREATE_TO_CLOUD = "sync_create_to_cloud"
    const val SYNC_SUBSCRIBE_AND_RECONCILE = "sync_subscribe_and_reconcile"

    const val NAV_HOST_INIT = "nav_host_init"
    const val NAV_LOGIN_TO_MAIN = "nav_login_to_main"
    const val NAVIGATION_TRANSITION = "navigation_transition"

    const val GROUP_UPDATE = "group_update"
    const val IMAGE_UPLOAD = "image_upload"
}

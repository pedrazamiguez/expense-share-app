package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler

/**
 * Generates a stable notification ID from the notification type, group, and entity.
 *
 * Using a deterministic ID ensures that:
 * - Different logical notifications get distinct IDs (no collisions).
 * - Re-notifications for the same entity **update** the existing notification
 *   instead of stacking duplicates.
 */
fun stableNotificationId(type: String?, groupId: String?, entityId: String?): Int {
    return "${type.orEmpty()}_${groupId.orEmpty()}_${entityId.orEmpty()}".hashCode()
}


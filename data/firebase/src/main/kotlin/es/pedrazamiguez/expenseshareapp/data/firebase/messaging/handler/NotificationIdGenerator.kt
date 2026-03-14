package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler

/**
 * Generates a stable, deterministic notification ID from the notification type, group, and entity.
 *
 * Using a deterministic ID means that:
 * - The same logical notification (same type/group/entity) will consistently get the same ID,
 *   so re-notifications for that entity **update** the existing notification instead of
 *   stacking duplicates.
 * - Different logical notifications will usually get different IDs, but collisions are still
 *   possible because this is based on a 32-bit hash and does not guarantee uniqueness.
 */
fun stableNotificationId(type: String?, groupId: String?, entityId: String?): Int =
    "${type.orEmpty()}_${groupId.orEmpty()}_${entityId.orEmpty()}".hashCode()

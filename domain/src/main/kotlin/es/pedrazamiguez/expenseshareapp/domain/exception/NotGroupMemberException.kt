package es.pedrazamiguez.expenseshareapp.domain.exception

/**
 * Thrown when the authenticated user attempts a write operation on a group
 * they are not a member of.
 *
 * Contains the raw IDs so the presentation layer can log or format a
 * meaningful error message — never format here.
 *
 * @param groupId The ID of the group the user tried to write to.
 * @param userId  The ID of the authenticated user who was denied.
 */
class NotGroupMemberException(
    val groupId: String,
    val userId: String
) : Exception("User '$userId' is not a member of group '$groupId'.")


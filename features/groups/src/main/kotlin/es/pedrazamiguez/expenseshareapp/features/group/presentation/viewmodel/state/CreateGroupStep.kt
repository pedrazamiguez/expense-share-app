package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state

/**
 * Defines the wizard steps for the create-group flow.
 *
 * All four steps are always applicable (no conditional steps).
 *
 * INFO → CURRENCY → MEMBERS → REVIEW
 */
enum class CreateGroupStep {
    /** Group name + optional description. */
    INFO,

    /** Primary currency selection + optional extra currencies. */
    CURRENCY,

    /** Invite members by email (optional). */
    MEMBERS,

    /** Read-only summary — final confirmation before creation. */
    REVIEW
}

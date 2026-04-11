package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state

/**
 * Defines the wizard steps for the create-group flow.
 *
 * All four steps are always applicable (no conditional steps).
 *
 * INFO → CURRENCY → MEMBERS → REVIEW
 *
 * @property isOptional When `true` the step can be skipped via "Skip to Review".
 *                      Currently all group creation steps are required.
 */
enum class CreateGroupStep(val isOptional: Boolean = false) {
    /** Group name + optional description. */
    INFO,

    /** Primary currency selection + optional extra currencies. */
    CURRENCY,

    /** Invite members by email (optional). */
    MEMBERS,

    /** Read-only summary — final confirmation before creation. */
    REVIEW
}

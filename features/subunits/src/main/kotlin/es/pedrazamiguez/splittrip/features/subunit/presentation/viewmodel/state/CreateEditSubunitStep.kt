package es.pedrazamiguez.splittrip.features.subunit.presentation.viewmodel.state

/**
 * Defines the wizard steps for the create/edit subunit flow.
 *
 * All four steps are always applicable (no conditional steps).
 *
 * NAME → MEMBERS → SHARES → REVIEW
 */
enum class CreateEditSubunitStep {
    /** Subunit name input. */
    NAME,

    /** Select members from the group. */
    MEMBERS,

    /** Allocate percentage shares for selected members. */
    SHARES,

    /** Read-only summary — final confirmation before saving. */
    REVIEW
}

package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state

/**
 * Defines the wizard steps for the create/edit sub-unit flow.
 *
 * All four steps are always applicable (no conditional steps).
 *
 * NAME → MEMBERS → SHARES → REVIEW
 */
enum class CreateEditSubunitStep {
    /** Sub-unit name input. */
    NAME,

    /** Select members from the group. */
    MEMBERS,

    /** Allocate percentage shares for selected members. */
    SHARES,

    /** Read-only summary — final confirmation before saving. */
    REVIEW
}

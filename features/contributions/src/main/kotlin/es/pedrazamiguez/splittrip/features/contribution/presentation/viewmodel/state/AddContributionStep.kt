package es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.state

/**
 * Wizard steps for the Add Contribution flow.
 *
 * All steps are required — there are no optional steps in this short wizard.
 *
 * @property isOptional When `true` the step can be skipped via "Skip to Review".
 *                      Currently all contribution steps are required.
 */
enum class AddContributionStep(val isOptional: Boolean = false) {
    AMOUNT,
    SCOPE,
    REVIEW
}

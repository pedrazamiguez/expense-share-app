package es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.state

import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardStep

/**
 * Wizard steps for the Add Contribution flow.
 *
 * All steps are required — there are no optional steps in this short wizard.
 *
 * @property isOptional When `true` the step can be skipped via "Skip to Review".
 *                      Currently all contribution steps are required.
 * @property isReview When `true` this is the final read-only review/confirmation step.
 */
enum class AddContributionStep(
    override val isOptional: Boolean = false,
    override val isReview: Boolean = false
) : WizardStep {
    AMOUNT,
    SCOPE,
    REVIEW(isReview = true)
}

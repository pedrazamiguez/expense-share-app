package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard

/**
 * Shared contract for wizard step enums.
 *
 * Implemented by all feature-level step enums so [WizardNavigator]
 * can operate generically without importing feature-level types.
 *
 * Feature-level step enums already declare [isOptional] as a constructor
 * parameter; implementing this interface only requires adding `override`
 * and adding the [isReview] property.
 *
 * Note: [WizardSkipStrategy] documents the same REQUIRED/OPTIONAL vocabulary
 * for composable-level usage; it coexists with this interface without conflict.
 *
 * @see WizardNavigator for the navigator that consumes this interface.
 */
interface WizardStep {

    /** When `true` the step may be skipped via "Skip to Review". */
    val isOptional: Boolean

    /** When `true` this is the final read-only review/confirmation step. */
    val isReview: Boolean
}

package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard

/**
 * Semantic classification of wizard step skip behaviour.
 *
 * This enum models the two strategies a wizard step can follow. Feature-level
 * step enums express this concept through a Boolean `isOptional` constructor
 * parameter rather than referencing this type directly. The enum exists as a
 * shared vocabulary for documentation and for potential future use by wizard
 * infrastructure (e.g., composable parameters, strategy-based routing).
 *
 * | Strategy         | Behaviour                                             |
 * |------------------|-------------------------------------------------------|
 * | [REQUIRED]       | Must be completed before advancing.                   |
 * | [OPTIONAL]       | May be skipped — visual cue in the step indicator.    |
 *
 * @see WizardStepIndicator for the visual treatment of optional steps.
 */
enum class WizardSkipStrategy {
    /** Step must be completed — default for all steps. */
    REQUIRED,

    /** Step may be skipped; shown with a distinct visual style. */
    OPTIONAL
}
